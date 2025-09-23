package com.app.musicstore.service;

import com.app.musicstore.model.CourseSeller;
import com.app.musicstore.repository.CourseSellerRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class CourseSellerService {

    private final CourseSellerRepository courseSellerRepository;

    public CourseSellerService(CourseSellerRepository courseSellerRepository) {
        this.courseSellerRepository = courseSellerRepository;
    }

    public CourseSeller save(CourseSeller courseSeller) {
        return courseSellerRepository.save(courseSeller);
    }

    public Optional<CourseSeller> findByUserId(Long userId) {
        return courseSellerRepository.findByUserId(userId);
    }

    public CourseSeller updateExpertise(Long userId, String expertise, int expYears) {
        CourseSeller seller = courseSellerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Course seller profile not found"));
        seller.setExpertise(expertise);
        seller.setExpYears(expYears);
        return courseSellerRepository.save(seller);
    }
}
