package com.dr.DavorRetreats.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dr.DavorRetreats.models.HotelImage;

@Repository
public interface HotelImageRepository extends JpaRepository<HotelImage, Integer> {
    List<HotelImage> findByHotelId(int hotelId);
} 