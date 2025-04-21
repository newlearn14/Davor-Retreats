package com.dr.DavorRetreats.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dr.DavorRetreats.models.Room;

public interface RoomRepository extends JpaRepository<Room, Long>{
    List<Room> findByHotelId(int hotelId);
}
