package com.app.musicstore.service;

import com.app.musicstore.model.Course;
import com.app.musicstore.model.CourseFeedback;
import com.app.musicstore.model.Customer;
import com.app.musicstore.model.Enrollment;
import com.app.musicstore.model.User;
import com.app.musicstore.repository.CourseFeedbackRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CourseFeedbackService {

    private final CourseFeedbackRepository courseFeedbackRepository;
    private final EnrollmentService enrollmentService;

    public CourseFeedbackService(CourseFeedbackRepository courseFeedbackRepository,
            EnrollmentService enrollmentService) {
        this.courseFeedbackRepository = courseFeedbackRepository;
        this.enrollmentService = enrollmentService;
    }

    // Submit feedback for a course
    public CourseFeedback submitFeedback(Course course, User student, String feedback, Integer rating) {
        // Check if student is enrolled in the course
        Customer customer = getCustomerFromUser(student);
        if (customer == null) {
            throw new IllegalArgumentException("Student profile not found");
        }

        List<Enrollment> enrollments = enrollmentService.getCustomerEnrollments(customer.getUserId());
        boolean isEnrolled = enrollments.stream()
                .anyMatch(e -> e.getCourse().getCourseId().equals(course.getCourseId()) &&
                        e.getStatus().name().equals("ACTIVE"));

        if (!isEnrolled) {
            throw new IllegalArgumentException("You must be enrolled in this course to leave feedback");
        }

        // Check if feedback already exists
        Optional<CourseFeedback> existingFeedback = courseFeedbackRepository.findByCourseAndStudent(course, student);
        if (existingFeedback.isPresent()) {
            throw new IllegalArgumentException("You have already submitted feedback for this course");
        }

        // Validate rating
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5 stars");
        }

        CourseFeedback courseFeedback = new CourseFeedback(course, student, feedback, rating);
        return courseFeedbackRepository.save(courseFeedback);
    }

    // Get feedback for a specific course
    public List<CourseFeedback> getCourseFeedback(Course course) {
        return courseFeedbackRepository.findByCourseOrderByFeedbackDateDesc(course);
    }

    // Get feedback submitted by a student
    public List<CourseFeedback> getStudentFeedback(User student) {
        return courseFeedbackRepository.findByStudentOrderByFeedbackDateDesc(student);
    }

    // Get feedback for courses taught by a seller
    public List<CourseFeedback> getSellerFeedback(Long sellerId) {
        return courseFeedbackRepository.findByCourseSellerOrderByFeedbackDateDesc(sellerId);
    }

    // Reply to feedback as a seller
    public CourseFeedback replyToFeedback(Long feedbackId, String sellerReply, Long sellerId) {
        CourseFeedback feedback = courseFeedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found"));

        // Verify the seller owns the course
        if (!feedback.getCourse().getCourseSeller().getUserId().equals(sellerId)) {
            throw new IllegalArgumentException("You can only reply to feedback for your own courses");
        }

        feedback.setSellerReply(sellerReply);
        feedback.setReplyDate(LocalDateTime.now());
        return courseFeedbackRepository.save(feedback);
    }

    // Get average rating for a course
    public Double getAverageRating(Course course) {
        Double average = courseFeedbackRepository.findAverageRatingByCourse(course);
        return average != null ? Math.round(average * 10.0) / 10.0 : 0.0;
    }

    // Get feedback count for a course
    public Long getFeedbackCount(Course course) {
        return courseFeedbackRepository.countByCourse(course);
    }

    // Check if student can submit feedback (is enrolled and hasn't submitted
    // before)
    public boolean canSubmitFeedback(Course course, User student) {
        Customer customer = getCustomerFromUser(student);
        if (customer == null)
            return false;

        // Check if already submitted feedback
        Optional<CourseFeedback> existingFeedback = courseFeedbackRepository.findByCourseAndStudent(course, student);
        if (existingFeedback.isPresent())
            return false;

        // Check if enrolled
        List<Enrollment> enrollments = enrollmentService.getCustomerEnrollments(customer.getUserId());
        return enrollments.stream()
                .anyMatch(e -> e.getCourse().getCourseId().equals(course.getCourseId()) &&
                        e.getStatus().name().equals("ACTIVE"));
    }

    private Customer getCustomerFromUser(User user) {
        // This would typically be done through a CustomerService
        // For now, we'll assume the user is a customer
        if (user.getRole().name().equals("CUSTOMER")) {
            Customer customer = new Customer();
            customer.setUserId(user.getUserId());
            customer.setName(user.getName());
            customer.setEmail(user.getEmail());
            return customer;
        }
        return null;
    }
}
