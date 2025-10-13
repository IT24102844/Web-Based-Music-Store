package com.app.musicstore.repository;

import com.app.musicstore.model.InstrumentImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstrumentImgRepository extends JpaRepository<InstrumentImg, Long> {

    List<InstrumentImg> findByProductId(Long productId);

    @Query("SELECT i FROM InstrumentImg i WHERE i.product.id = :productId AND i.isPrimary = true")
    Optional<InstrumentImg> findPrimaryByProductId(@Param("productId") Long productId);

    @Modifying
    @Query("UPDATE InstrumentImg i SET i.isPrimary = false WHERE i.product.id = :productId")
    void clearPrimaryFlagsByProductId(@Param("productId") Long productId);

    @Modifying
    @Query("UPDATE InstrumentImg i SET i.isPrimary = true WHERE i.id = :imageId")
    void setPrimaryById(@Param("imageId") Long imageId);

    void deleteByProductId(Long productId);
}
