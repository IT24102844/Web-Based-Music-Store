package com.app.musicstore.service;

import com.app.musicstore.model.InstrumentSeller;
import com.app.musicstore.repository.InstrumentSellerRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class InstrumentSellerService {

    private final InstrumentSellerRepository instrumentSellerRepository;

    public InstrumentSellerService(InstrumentSellerRepository instrumentSellerRepository) {
        this.instrumentSellerRepository = instrumentSellerRepository;
    }

    public InstrumentSeller save(InstrumentSeller seller) {
        return instrumentSellerRepository.save(seller);
    }

    public Optional<InstrumentSeller> findByUserId(Long userId) {
        return instrumentSellerRepository.findByUserId(userId);
    }

    public InstrumentSeller updateStoreDetails(Long userId, String storeName, String location) {
        InstrumentSeller seller = instrumentSellerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Instrument seller profile not found"));
        seller.setStoreName(storeName);
        seller.setLocation(location);
        return instrumentSellerRepository.save(seller);
    }
}
