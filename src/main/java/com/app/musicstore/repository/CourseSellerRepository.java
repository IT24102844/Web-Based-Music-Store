package com.app.musicstore.repository;

import com.app.musicstore.model.CourseSeller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CourseSellerRepository extends JpaRepository<CourseSeller, Long> {

    Optional<CourseSeller> findByUserId(Long userId);

    @Query("SELECT cs FROM CourseSeller cs JOIN User u ON cs.userId = u.userId WHERE u.email = :email")
    Optional<CourseSeller> findByEmail(@Param("email") String email);

    boolean existsByUserId(Long userId);
}