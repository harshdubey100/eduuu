package com.eduverse.backend.repository;



import com.eduverse.backend.Entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {


    // 1. NATIVE SEARCH: Finds courses by title keyword using SQL's LOWER and LIKE
    // Note: 'courses' is the literal database table name
    @Query(value = "SELECT * FROM courses WHERE LOWER(title) LIKE LOWER(CONCAT('%', :keyword, '%'))", nativeQuery = true)
    List<Course> searchByTitleKeywordSQL(@Param("keyword") String keyword);


    // 2. NATIVE MULTI-COLUMN FILTER: Filters by category and difficulty level using raw SQL
    @Query(value = "SELECT * FROM courses WHERE category = :category AND level = :level", nativeQuery = true)
    List<Course> findByCategoryAndLevelSQL(@Param("category") String category, @Param("level") String level);


    // 3. NATIVE PRICE CHECK: Your query finding courses under a maximum budget
    @Query(value = "SELECT * FROM courses WHERE price <= :maxPrice", nativeQuery = true)
    List<Course> findAffordableCoursesSQL(@Param("maxPrice") Double maxPrice);


    // 4. NATIVE MODIFICATION QUERY: Applies a bulk category discount using raw SQL UPDATE
    @Transactional
    @Modifying
    @Query(value = "UPDATE courses SET price = price * :discountFactor WHERE category = :category", nativeQuery = true)
    int applyCategoryDiscountSQL(@Param("category") String category, @Param("discountFactor") Double discountFactor);
}
