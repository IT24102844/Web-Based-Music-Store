package com.app.musicstore.controller;

import com.app.musicstore.model.*;
import com.app.musicstore.security.CustomUserDetails;
import com.app.musicstore.service.CourseService;
import com.app.musicstore.service.CustomerService;
import com.app.musicstore.service.EnrollmentService;
import com.app.musicstore.service.CourseFeedbackService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/customer/courses")
public class CustomerCourseController {

    private final CourseService courseService;
    private final CustomerService customerService;
    private final EnrollmentService enrollmentService;
    private final CourseFeedbackService courseFeedbackService;

    public CustomerCourseController(CourseService courseService,
            CustomerService customerService,
            EnrollmentService enrollmentService,
            CourseFeedbackService courseFeedbackService) {
        this.courseService = courseService;
        this.customerService = customerService;
        this.enrollmentService = enrollmentService;
        this.courseFeedbackService = courseFeedbackService;
    }

    @GetMapping
    public String browseCourses(Model model,
            @RequestParam(required = false) String search) {
        List<Course> courses;

        if (search != null && !search.trim().isEmpty()) {
            courses = courseService.searchByTitle(search);
            model.addAttribute("search", search);
        } else {
            courses = courseService.findAll();
        }

        model.addAttribute("courses", courses);

        Customer customer = getAuthenticatedCustomer();
        if (customer != null) {
            List<Enrollment> enrollments = enrollmentService.getCustomerEnrollments(customer.getUserId());
            model.addAttribute("enrollments", enrollments);

            // ✅ Extract courseIds in backend instead of Thymeleaf
            List<Long> enrolledCourseIds = enrollments.stream()
                    .map(e -> e.getCourse().getCourseId())
                    .toList();
            model.addAttribute("enrolledCourseIds", enrolledCourseIds);
        }

        return "course-browse";
    }

    @GetMapping("/enroll/{courseId}")
    public String enrollInCourse(@PathVariable Long courseId, RedirectAttributes redirectAttributes) {
        Customer customer = getAuthenticatedCustomer();
        if (customer == null) {
            return "redirect:/users/login";
        }

        // Redirect to unified checkout; after payment, perform enrollment
        var courseOpt = courseService.findById(courseId);
        if (courseOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Course not found");
            return "redirect:/customer/courses";
        }
        var course = courseOpt.get();
        String successRedirect = "/customer/courses/after-unified-enroll?courseId=" + courseId;
        String url = String.format(
                "redirect:/payments/checkout?itemType=%s&itemId=%d&itemName=%s&amount=%s&successRedirect=%s",
                java.net.URLEncoder.encode("COURSE", java.nio.charset.StandardCharsets.UTF_8),
                courseId,
                java.net.URLEncoder.encode(course.getTitle(), java.nio.charset.StandardCharsets.UTF_8),
                java.net.URLEncoder.encode(String.valueOf(course.getPrice()), java.nio.charset.StandardCharsets.UTF_8),
                java.net.URLEncoder.encode(successRedirect, java.nio.charset.StandardCharsets.UTF_8));
        return url;
    }

    @GetMapping("/after-unified-enroll")
    public String afterUnifiedEnroll(@RequestParam Long courseId,
            @RequestParam(value = "unifiedPaymentId", required = false) Long unifiedPaymentId,
            RedirectAttributes redirectAttributes) {
        Customer customer = getAuthenticatedCustomer();
        if (customer == null) {
            return "redirect:/users/login";
        }
        try {
            enrollmentService.enrollCustomer(customer.getUserId(), courseId);
            redirectAttributes.addFlashAttribute("success", "Enrollment successful");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customer/courses";
    }

    @GetMapping("/my-courses")
    public String viewMyCourses(Model model) {
        Customer customer = getAuthenticatedCustomer();
        if (customer == null) {
            return "redirect:/users/login";
        }

        List<Enrollment> enrollments = enrollmentService.getCustomerEnrollments(customer.getUserId());

        // Calculate statistics in controller
        long activeCount = enrollments.stream()
                .filter(e -> e != null && e.getStatus() == EnrollmentStatus.ACTIVE)
                .count();
        long completedCount = enrollments.stream()
                .filter(e -> e != null && e.getStatus() == EnrollmentStatus.COMPLETED)
                .count();
        long cancelledCount = enrollments.stream()
                .filter(e -> e != null && e.getStatus() == EnrollmentStatus.CANCELLED)
                .count();

        model.addAttribute("enrollments", enrollments);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("cancelledCount", cancelledCount);

        return "my-courses";
    }

    @GetMapping("/cancel-enrollment/{enrollmentId}")
    public String cancelEnrollment(@PathVariable Long enrollmentId, RedirectAttributes redirectAttributes) {
        Customer customer = getAuthenticatedCustomer();
        if (customer == null) {
            return "redirect:/users/login";
        }

        Enrollment enrollment = enrollmentService.getCustomerEnrollments(customer.getUserId())
                .stream()
                .filter(e -> e.getEnrollmentId().equals(enrollmentId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        enrollmentService.cancelEnrollment(enrollmentId);
        redirectAttributes.addFlashAttribute("success", "Enrollment cancelled successfully");
        return "redirect:/customer/courses/my-courses";
    }

    @GetMapping("/reenroll/{enrollmentId}")
    public String reenrollEnrollment(@PathVariable Long enrollmentId, RedirectAttributes redirectAttributes) {
        Customer customer = getAuthenticatedCustomer();
        if (customer == null) {
            return "redirect:/users/login";
        }

        // Find the specific enrollment for this customer
        Enrollment enrollment = enrollmentService.getCustomerEnrollments(customer.getUserId())
                .stream()
                .filter(e -> e.getEnrollmentId().equals(enrollmentId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        // Call service to reenroll
        enrollmentService.reenrollEnrollment(enrollmentId);

        redirectAttributes.addFlashAttribute("success", "You have successfully re-enrolled in the course!");
        return "redirect:/customer/courses/my-courses";
    }

    @GetMapping("/complete-enrollment/{enrollmentId}")
    public String completeEnrollment(@PathVariable Long enrollmentId,
            RedirectAttributes redirectAttributes) {
        Customer customer = getAuthenticatedCustomer();
        if (customer == null) {
            return "redirect:/users/login";
        }

        try {
            // Verify the enrollment belongs to this customer
            Enrollment enrollment = enrollmentService.getCustomerEnrollments(customer.getUserId())
                    .stream()
                    .filter(e -> e.getEnrollmentId().equals(enrollmentId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Enrollment not found"));

            enrollmentService.completeEnrollment(enrollmentId);
            redirectAttributes.addFlashAttribute("success",
                    "Congratulations! You have completed the course.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/customer/courses/my-courses";
    }

    // Feedback endpoints
    @GetMapping("/feedback/{courseId}")
    public String showFeedbackForm(@PathVariable Long courseId, Model model) {
        Customer customer = getAuthenticatedCustomer();
        if (customer == null) {
            return "redirect:/users/login";
        }

        var courseOpt = courseService.findById(courseId);
        if (courseOpt.isEmpty()) {
            return "redirect:/customer/courses?error=Course not found";
        }
        Course course = courseOpt.get();

        // Check if student can submit feedback
        User currentUser = getCurrentUser();
        boolean canSubmitFeedback = courseFeedbackService.canSubmitFeedback(course, currentUser);

        model.addAttribute("course", course);
        model.addAttribute("canSubmitFeedback", canSubmitFeedback);
        return "course-feedback-form";
    }

    @PostMapping("/feedback/{courseId}")
    public String submitFeedback(@PathVariable Long courseId,
            @RequestParam String feedback,
            @RequestParam Integer rating,
            RedirectAttributes redirectAttributes) {
        Customer customer = getAuthenticatedCustomer();
        if (customer == null) {
            return "redirect:/users/login";
        }

        try {
            var courseOpt = courseService.findById(courseId);
            if (courseOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Course not found");
                return "redirect:/customer/courses";
            }
            Course course = courseOpt.get();

            User currentUser = getCurrentUser();
            courseFeedbackService.submitFeedback(course, currentUser, feedback, rating);

            redirectAttributes.addFlashAttribute("success", "Thank you for your feedback!");
            return "redirect:/customer/courses/my-courses";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/customer/courses/feedback/" + courseId;
        }
    }

    @GetMapping("/my-feedback")
    public String myFeedback(Model model) {
        Customer customer = getAuthenticatedCustomer();
        if (customer == null) {
            return "redirect:/users/login";
        }

        User currentUser = getCurrentUser();
        List<CourseFeedback> feedbacks = courseFeedbackService.getStudentFeedback(currentUser);

        model.addAttribute("feedbacks", feedbacks);
        return "my-course-feedback";
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null &&
                authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getUser();
        }
        return null;
    }

    private Customer getAuthenticatedCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null &&
                authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof CustomUserDetails) {

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            if (user.getRole() == com.app.musicstore.model.Role.CUSTOMER) {
                return customerService.findByUserId(user.getUserId())
                        .orElseThrow(() -> new RuntimeException("Customer profile not found"));
            }
        }

        return null;
    }
}