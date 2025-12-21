package com.app.musicstore.repository;

import com.app.musicstore.model.Course;
import com.app.musicstore.model.CourseFeedback;
import com.app.musicstore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseFeedbackRepository extends JpaRepository<CourseFeedback, Long> {

    // Find feedback by course
    List<CourseFeedback> findByCourseOrderByFeedbackDateDesc(Course course);

    // Find feedback by student
    List<CourseFeedback> findByStudentOrderByFeedbackDateDesc(User student);

    // Find specific feedback by course and student
    Optional<CourseFeedback> findByCourseAndStudent(Course course, User student);

    // Find feedback for courses taught by a specific seller
    @Query("SELECT cf FROM CourseFeedback cf WHERE cf.course.courseSeller.userId = :sellerId ORDER BY cf.feedbackDate DESC")
    List<CourseFeedback> findByCourseSellerOrderByFeedbackDateDesc(Long sellerId);

    // Calculate average rating for a course
    @Query("SELECT AVG(cf.rating) FROM CourseFeedback cf WHERE cf.course = :course")
    Double findAverageRatingByCourse(Course course);

    // Count feedback for a course
    @Query("SELECT COUNT(cf) FROM CourseFeedback cf WHERE cf.course = :course")
    Long countByCourse(Course course);
}
