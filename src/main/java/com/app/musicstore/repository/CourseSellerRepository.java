package com.app.musicstore.repository;

import com.app.musicstore.model.CourseSeller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface CourseSellerRepository extends JpaRepository<CourseSeller, Long> {

    Optional<CourseSeller> findByUserId(Long userId);

    Optional<CourseSeller> findByEmail(String email);

    boolean existsByUserId(Long userId);

}