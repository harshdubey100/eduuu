package com.eduverse.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Simulates the payload a real payment gateway (Stripe/Razorpay) would POST
// to a webhook endpoint once a charge succeeds or fails.
@Getter
@Setter
@NoArgsConstructor
public class PaymentWebhookRequestDTO {

    @NotNull(message = "Order reference ID is required")
    private Long orderId;

    @NotBlank(message = "Payment status is required")
    private String status; // expected: "SUCCESS" or "FAILED"
}
