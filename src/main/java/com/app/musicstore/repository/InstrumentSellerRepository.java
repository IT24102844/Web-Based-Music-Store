package com.app.musicstore.repository;

import com.app.musicstore.model.InstrumentSeller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InstrumentSellerRepository extends JpaRepository<InstrumentSeller, Long> {

    Optional<InstrumentSeller> findByUserId(Long userId);

    @Query("SELECT is FROM InstrumentSeller is JOIN User u ON is.userId = u.userId WHERE u.email = :email")
    Optional<InstrumentSeller> findByEmail(@Param("email") String email);

    boolean existsByUserId(Long userId);
}