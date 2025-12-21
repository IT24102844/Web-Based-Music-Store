package com.app.musicstore.repository;

import com.app.musicstore.model.Course;
import com.app.musicstore.model.CourseSeller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByCourseSeller(CourseSeller courseSeller);
    List<Course> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT c FROM Course c WHERE c.courseSeller.userId = :sellerId")
    List<Course> findByCourseSellerId(@Param("sellerId") Long sellerId);
}