package com.app.musicstore.service;

import com.app.musicstore.model.*;
import com.app.musicstore.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseService courseService;
    private final CustomerService customerService;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             CourseService courseService,
                             CustomerService customerService) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseService = courseService;
        this.customerService = customerService;
    }

    public Enrollment enrollCustomer(Long customerId, Long courseId) {
        Customer customer = customerService.findByUserId(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Course course = courseService.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Check if already enrolled
        if (enrollmentRepository.existsByCustomerAndCourseAndStatus(
                customer, course, EnrollmentStatus.ACTIVE)) {
            throw new RuntimeException("Customer is already actively enrolled in this course");
        }
        Enrollment enrollment = new Enrollment(customer, course);
        return enrollmentRepository.save(enrollment);
    }

    public List<Enrollment> getCustomerEnrollments(Long customerId) {
        return enrollmentRepository.findByCustomerId(customerId);
    }

    public List<Enrollment> getCourseEnrollments(Long courseId) {
        Course course = courseService.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return enrollmentRepository.findByCourse(course);
    }

    public List<Enrollment> getSellerEnrollments(Long sellerId) {
        return enrollmentRepository.findByCourseSellerId(sellerId);
    }

    public void cancelEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        enrollment.setStatus(EnrollmentStatus.CANCELLED);
        enrollmentRepository.save(enrollment);
    }

    public Optional<Enrollment> findByCustomerAndCourse(Long customerId, Long courseId) {
        Customer customer = customerService.findByUserId(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Course course = courseService.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        return enrollmentRepository.findByCustomerAndCourse(customer, course);
    }
    public List<Enrollment> getActiveEnrollments(Long customerId) {
        Customer customer = customerService.findByUserId(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        return enrollmentRepository.findByCustomerAndStatus(customer, EnrollmentStatus.ACTIVE);
    }

    // NEW METHOD: Complete an enrollment
    public void completeEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        enrollment.setStatus(EnrollmentStatus.COMPLETED);
        // Uncomment if you added completionDate to Enrollment entity:
        // enrollment.setCompletionDate(LocalDateTime.now());
        enrollmentRepository.save(enrollment);
    }
    public void reenrollEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        // Only allow reenrolling if it was cancelled
        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            enrollment.setStatus(EnrollmentStatus.ACTIVE);
            enrollmentRepository.save(enrollment);
        } else {
            throw new RuntimeException("Only cancelled enrollments can be re-enrolled");
        }
    }

}