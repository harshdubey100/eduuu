# What was added / fixed for the interview

## Bugs fixed in existing code
1. **`GlobalExceptionHandler` only caught 2 exception types.** `EnrollmentService`/`UserService`
   threw plain `RuntimeException` for business errors ("already enrolled", "invalid password"),
   which fell through to Spring's default handler and returned a raw 500. Added specific
   exception types (`DuplicateResourceException`, `InvalidCredentialsException`,
   `CapacityExceededException`, `UnauthorizedActionException`, `ResourceNotFoundException`)
   mapped to correct HTTP status codes, plus a generic `RuntimeException` fallback so nothing
   slips through as an unhandled 500 again.
2. **`DELETE /courses/{id}` was dead code.** `CourseService.deleteCourse()` existed but no
   controller endpoint called it. Wired it up.
3. **Security rule that silently never matched.** `/enrollments/admin/force-assign/**` (with
   trailing `/**`) does not match the literal path `/enrollments/admin/force-assign` under
   Ant-style path matching — so admins were being blocked from their own endpoint. Fixed to
   match the exact path.
4. **Security rule ordering bug.** The broad `/enrollments/**` → `ROLE_STUDENT` rule was
   listed *before* the specific admin override, so (once bug #3 was also fixed) the broader
   rule would've matched first and still blocked admins. Spring Security matches top-to-bottom;
   reordered so specific rules precede general ones throughout the whole config.

## New features (all real code, not stubs)
- **Course structure**: `Module` and `Lesson` entities, ordered curriculum, publish/draft
  status, capacity + limited-seat enforcement on enrollment.
- **Orders & payment**: `Order` entity (PENDING → PAID/FAILED), a simulated payment webhook
  endpoint that auto-creates the enrollment once payment succeeds. No real gateway (Stripe/etc.)
  is wired in — see "Honest limitations" below.
- **Progress tracking**: per-lesson completion, course completion %, `checkpoint`/`dueDate`
  fields on lessons for assessment-style content.
- **Course-specific RAG**: chunks lesson content, builds a TF-IDF vector space per course, and
  ranks chunks by cosine similarity to a question. This is a genuine, working retrieval engine
  with zero external dependencies — see "Honest limitations" below for what it isn't.
- **Caching**: `@Cacheable`/`@CacheEvict` on the course catalog and course-detail reads.
- **Popularity & evaluation**: view counts, live enrollment counts, seats-remaining, and a
  course review/rating system (enrolled students only, one review per student per course).
- **Structured logging**: key=value log lines (`action=... studentId=... courseId=...`) added
  across the service layer.

## Honest limitations (know these before your interview)
- **RAG**: this implements the *retrieval* half of Retrieval-Augmented Generation — chunking,
  vectorization, similarity ranking. It does not call an LLM to *generate* an answer from the
  retrieved chunks (that step needs an API key / network access this sandbox doesn't have).
  If asked "where's the generation step," the honest answer is: "the retrieval pipeline is
  real; swapping in an LLM call over the top-k chunks — e.g. the Claude or OpenAI API — is the
  natural next step, and the architecture already returns exactly the context that call would need."
- **Payments**: the webhook is a *simulated* gateway callback, not a real Stripe/Razorpay
  integration. It faithfully models the order → webhook → enrollment flow, but there's no real
  charge happening. Say so directly if asked.
- **Not compiled in this sandbox**: this environment has no access to Maven Central, so I
  couldn't run `mvn compile` end-to-end here. I ran a full Java syntax check (javac, no
  classpath) across all 66 files and found zero syntax errors, and manually reviewed every
  cross-file reference (Spring Data derived query names, Lombok-generated getter/setter names,
  imports). **Run `mvn clean compile` yourself as the very first step** before you do anything
  else with this — see the checklist below.

## Before you touch anything else: verification checklist
1. `cd backend && mvn clean compile` — fix any real compile errors first.
2. Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (env vars or `.env` — `dotenv-java` is already wired in).
3. `mvn spring-boot:run`, then smoke-test in this order with Postman/curl:
   - `POST /users` (register an INSTRUCTOR) → `POST /users/login` → save the JWT
   - `POST /courses` (as instructor) → `PATCH /courses/{id}/publish?published=true`
   - `POST /courses/{id}/modules` → `POST /modules/{id}/lessons`
   - Register + login a STUDENT → `POST /orders` → `POST /orders/payment-webhook` with
     `{"orderId": ..., "status": "SUCCESS"}` → check `GET /enrollments/my` shows the course
   - `POST /lessons/{id}/complete` (as student) → `GET /courses/{id}/progress`
   - `POST /courses/{id}/ask` with `{"question": "..."}` → check it returns ranked chunks
   - `POST /courses/{id}/reviews` (as enrolled student) → `GET /courses/{id}` and confirm
     `averageRating` reflects it
4. Watch the console logs during this run — the `action=...` log lines should show up as you go.
