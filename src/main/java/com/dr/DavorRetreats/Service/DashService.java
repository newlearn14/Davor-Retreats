package com.dr.DavorRetreats.Service;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dr.DavorRetreats.models.Hotel;
import com.dr.DavorRetreats.models.Room;
import com.dr.DavorRetreats.models.RoomTO;
import com.dr.DavorRetreats.repository.HotelRepository;
import com.dr.DavorRetreats.repository.RoomRepository;
import com.dr.DavorRetreats.models.Booking;

@Service
public class DashService {

	@Autowired
	RoomRepository roomrepository;
	
	@Autowired
	HotelRepository hotelRepository;

	@Autowired
	BookingService bookingService;

	public boolean register(RoomTO roomTO, int hotelId) {
		Room r = new Room();
		BeanUtils.copyProperties(roomTO, r);
		
		// Get the hotel and set it in the room
		Hotel hotel = hotelRepository.findById(hotelId)
				.orElseThrow(() -> new RuntimeException("Hotel not found"));
		r.setHotel(hotel);
		
		roomrepository.save(r);
		return true;
	}

	public List<Room> getAllRooms() {
		return roomrepository.findAll();
	}
	
	public List<Room> getRoomsByHotelId(int hotelId) {
		return roomrepository.findByHotelId(hotelId);
	}

	public Room getRoomById(Long id) {
		return roomrepository.findById(id).orElseThrow(() -> new RuntimeException("Room not found"));
	}

	public boolean update(long id, Room room) {
		try {
			Room existingRoom = roomrepository.findById(id).orElseThrow(() -> new RuntimeException("Room not found"));
			
			// Preserve the hotel relationship
			Hotel hotel = existingRoom.getHotel();
			
			BeanUtils.copyProperties(room, existingRoom);
			existingRoom.setHotel(hotel); // Ensure hotel relationship is maintained

			roomrepository.save(existingRoom);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean deleteRoom(Long id) {
		try {
			Room room = roomrepository.findById(id)
					.orElseThrow(() -> new RuntimeException("Room not found"));
			
			// Check if room has any active bookings
			List<Booking> activeBookings = bookingService.getActiveBookingsByRoomId(id);
			if (!activeBookings.isEmpty()) {
				throw new RuntimeException("Cannot delete room with active bookings");
			}
			
			// Delete the room
			roomrepository.delete(room);
			return true;
		} catch (Exception e) {
			e.printStackTrace(); // Log the error for debugging
			return false;
		}
	}
}
