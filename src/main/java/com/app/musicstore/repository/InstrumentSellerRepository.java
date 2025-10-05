package com.app.musicstore.repository;

import com.app.musicstore.model.InstrumentSeller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface InstrumentSellerRepository extends JpaRepository<InstrumentSeller, Long> {

    Optional<InstrumentSeller> findByUserId(Long userId);

    Optional<InstrumentSeller> findByEmail(String email);

    boolean existsByUserId(Long userId);

}