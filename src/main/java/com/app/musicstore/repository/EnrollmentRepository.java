package com.app.musicstore.repository;

import com.app.musicstore.model.Course;
import com.app.musicstore.model.Customer;
import com.app.musicstore.model.Enrollment;
import com.app.musicstore.model.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Optional<Enrollment> findByCustomerAndCourse(Customer customer, Course course);
    List<Enrollment> findByCustomer(Customer customer);
    List<Enrollment> findByCourse(Course course);

    @Query("SELECT e FROM Enrollment e WHERE e.customer.userId = :customerId")
    List<Enrollment> findByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT e FROM Enrollment e WHERE e.course.courseSeller.userId = :sellerId")
    List<Enrollment> findByCourseSellerId(@Param("sellerId") Long sellerId);

    boolean existsByCustomerAndCourse(Customer customer, Course course);

    boolean existsByCustomerAndCourseAndStatus(
            Customer customer, Course course, EnrollmentStatus status);

    List<Enrollment> findByCustomerAndStatus(
            Customer customer, EnrollmentStatus status);

    long countByCourseAndStatus(
            Course course, EnrollmentStatus status);
}