package com.app.musicstore.controller;

import com.app.musicstore.model.Course;
import com.app.musicstore.model.CourseSeller;
import com.app.musicstore.model.Enrollment;
import com.app.musicstore.model.User;
import com.app.musicstore.security.CustomUserDetails;
import com.app.musicstore.service.CourseService;
import com.app.musicstore.service.CourseSellerService;
import com.app.musicstore.service.EnrollmentService;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/course-seller")
public class CourseSellerCourseController {

    private final CourseService courseService;
    private final CourseSellerService courseSellerService;
    private final EnrollmentService enrollmentService;

    public CourseSellerCourseController(CourseService courseService,
                                        CourseSellerService courseSellerService,
                                        EnrollmentService enrollmentService) {
        this.courseService = courseService;
        this.courseSellerService = courseSellerService;
        this.enrollmentService = enrollmentService;
    }


    @GetMapping("/courses")
    public String listCourses(Model model) {
        CourseSeller seller = getAuthenticatedCourseSeller();
        if (seller == null) {
            return "redirect:/users/login";
        }

        List<Course> courses = courseService.findByCourseSellerId(seller.getUserId());

        // Extract file name from file path
        for (Course course : courses) {
            if (course.getFilePath() != null) {
                String fileName = Paths.get(course.getFilePath()).getFileName().toString();
                course.setFileName(fileName);
            }
        }

        model.addAttribute("courses", courses);
        return "course-list"; // matches course-list.html
    }

    // Show create course form
    @GetMapping("/courses/create")
    public String showCreateForm(Model model) {
        CourseSeller seller = getAuthenticatedCourseSeller();
        if (seller == null) {
            return "redirect:/users/login";
        }

        model.addAttribute("course", new Course());
        return "course-form";
    }

    // Process create course form
    @PostMapping("/courses/create")
    public String createCourse(
            @ModelAttribute Course course,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {

        CourseSeller seller = getAuthenticatedCourseSeller();
        if (seller == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to create a course.");
            return "redirect:/users/login";
        }

        try {
            // Assign the course seller
            course.setCourseSeller(seller);

            // Handle file upload
            if (file != null && !file.isEmpty()) {
                // Example: store the file and set the path in course
                String filename = courseService.storeFile(file);
                course.setFilePath(filename); // Make sure Course entity has a field like 'filePath'
            } else {
                redirectAttributes.addFlashAttribute("error", "Please upload a course file.");
                return "redirect:/course-seller/courses/create";
            }

            // Save the course
            courseService.save(course);

            redirectAttributes.addFlashAttribute("success", "Course created successfully!");
            return "redirect:/course-seller/courses";

        } catch (Exception e) {
            // Log the exception (optional)
            e.printStackTrace();

            redirectAttributes.addFlashAttribute("error", "Error creating course: " + e.getMessage());
            return "redirect:/course-seller/courses/create";
        }
    }

    // Show edit course form
    @GetMapping("/courses/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        CourseSeller seller = getAuthenticatedCourseSeller();
        if (seller == null) {
            return "redirect:/users/login";
        }

        Course course = courseService.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Verify the course belongs to the authenticated seller
        if (!course.getCourseSeller().getUserId().equals(seller.getUserId())) {
            return "redirect:/course-seller/courses?error=Unauthorized";
        }

        model.addAttribute("course", course);
        return "course-form";
    }

    // Process update course form
    @PostMapping("/courses/update/{id}")
    public String updateCourse(@PathVariable Long id, @ModelAttribute Course course, RedirectAttributes redirectAttributes) {
        CourseSeller seller = getAuthenticatedCourseSeller();
        if (seller == null) {
            return "redirect:/users/login";
        }

        try {
            Course existingCourse = courseService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Course not found"));

            // Verify the course belongs to the authenticated seller
            if (!existingCourse.getCourseSeller().getUserId().equals(seller.getUserId())) {
                return "redirect:/course-seller/courses?error=Unauthorized";
            }

            courseService.updateCourse(id, course);
            redirectAttributes.addFlashAttribute("success", "Course updated successfully");
            return "redirect:/course-seller/courses";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating course: " + e.getMessage());
            return "redirect:/course-seller/courses/edit/" + id;
        }
    }

    // Delete course
    @GetMapping("/courses/delete/{id}")
    public String deleteCourse(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        CourseSeller seller = getAuthenticatedCourseSeller();
        if (seller == null) {
            return "redirect:/users/login";
        }

        try {
            Course course = courseService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Course not found"));

            // Verify the course belongs to the authenticated seller
            if (!course.getCourseSeller().getUserId().equals(seller.getUserId())) {
                return "redirect:/course-seller/courses?error=Unauthorized";
            }

            courseService.deleteCourse(id);
            redirectAttributes.addFlashAttribute("success", "Course deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting course: " + e.getMessage());
        }

        return "redirect:/course-seller/courses";
    }

    // View enrollments for seller's courses
    @GetMapping("/courses/enrollments")
    public String viewEnrollments(Model model) {
        CourseSeller seller = getAuthenticatedCourseSeller();
        if (seller == null) {
            return "redirect:/users/login";
        }

        List<Enrollment> enrollments = enrollmentService.getSellerEnrollments(seller.getUserId());
        model.addAttribute("enrollments", enrollments);
        return "enrollment-list";
    }

    private CourseSeller getAuthenticatedCourseSeller() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null &&
                authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof CustomUserDetails) {

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            if (user.getRole() == com.app.musicstore.model.Role.COURSE_SELLER) {
                return courseSellerService.findByUserId(user.getUserId())
                        .orElseThrow(() -> new RuntimeException("Course seller profile not found"));
            }
        }

        return null;
    }
}