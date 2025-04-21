package com.dr.DavorRetreats.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dr.DavorRetreats.Service.AuthService;
import com.dr.DavorRetreats.Service.DashService;
import com.dr.DavorRetreats.Service.BookingService;
import com.dr.DavorRetreats.models.Hotel;
import com.dr.DavorRetreats.models.Room;
import com.dr.DavorRetreats.models.RoomDTO;
import com.dr.DavorRetreats.models.RoomTO;
import com.dr.DavorRetreats.models.Booking;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/auth")
public class DashboardController {
	@Autowired
	DashService service;
	
	@Autowired
	AuthService authService;
	
	@Autowired
	BookingService bookingService;

	@GetMapping("/dash")
	public String DashControl(Model model, HttpSession session) {
		// Get the logged-in hotel
		String username = (String) session.getAttribute("user");
		if (username == null) {
			return "redirect:/auth/davor"; // Redirect to login if not logged in
		}
		
		Hotel hotel = authService.getHotelByUsername(username);
		if (hotel == null) {
			return "redirect:/auth/davor"; // Redirect to login if hotel not found
		}
		
		// Get rooms for this hotel
		List<Room> rooms = service.getRoomsByHotelId(hotel.getId());
		
		// Add room statistics
		long availableRooms = rooms.stream().filter(r -> "AVAILABLE".equalsIgnoreCase(r.getStatus())).count();
		long occupiedRooms = rooms.stream().filter(r -> "OCCUPIED".equalsIgnoreCase(r.getStatus())).count();
		long maintenanceRooms = rooms.stream().filter(r -> "MAINTENANCE".equalsIgnoreCase(r.getStatus())).count();
		
		// Check for success or error messages
		String successMessage = (String) session.getAttribute("successMessage");
		String errorMessage = (String) session.getAttribute("errorMessage");
		
		if (successMessage != null) {
			model.addAttribute("successMessage", successMessage);
			session.removeAttribute("successMessage");
		}
		
		if (errorMessage != null) {
			model.addAttribute("errorMessage", errorMessage);
			session.removeAttribute("errorMessage");
		}
		
		model.addAttribute("hotel", hotel);
		model.addAttribute("rooms", rooms);
		model.addAttribute("availableRooms", availableRooms);
		model.addAttribute("occupiedRooms", occupiedRooms);
		model.addAttribute("maintenanceRooms", maintenanceRooms);
		
		return "auth/DashHotel";
	}

	@GetMapping("/room-management")
	public String roomManagement(Model model, HttpSession session) {
		// Get the logged-in hotel
		String username = (String) session.getAttribute("user");
		if (username == null) {
			return "redirect:/auth/davor"; // Redirect to login if not logged in
		}
		
		Hotel hotel = authService.getHotelByUsername(username);
		if (hotel == null) {
			return "redirect:/auth/davor"; // Redirect to login if hotel not found
		}
		
		// Get rooms for this hotel
		List<Room> rooms = service.getRoomsByHotelId(hotel.getId());
		
		// Check for success or error messages
		String successMessage = (String) session.getAttribute("successMessage");
		String errorMessage = (String) session.getAttribute("errorMessage");
		
		if (successMessage != null) {
			model.addAttribute("successMessage", successMessage);
			session.removeAttribute("successMessage");
		}
		
		if (errorMessage != null) {
			model.addAttribute("errorMessage", errorMessage);
			session.removeAttribute("errorMessage");
		}
		
		model.addAttribute("hotel", hotel);
		model.addAttribute("rooms", rooms);
		model.addAttribute("room", new RoomDTO());
		
		return "auth/Dash";
	}

	@PostMapping("/register")
	public String RegisterRoom(@ModelAttribute RoomTO roomTO, HttpSession session, Model model) {
		// Get the logged-in hotel
		String username = (String) session.getAttribute("user");
		if (username == null) {
			return "redirect:/auth/davor"; // Redirect to login if not logged in
		}
		
		Hotel hotel = authService.getHotelByUsername(username);
		if (hotel == null) {
			return "redirect:/auth/davor"; // Redirect to login if hotel not found
		}
		
		boolean register = service.register(roomTO, hotel.getId());
		
		if (register) {
			// Add success message to session
			session.setAttribute("successMessage", "Room added successfully!");
		} else {
			// Add error message to session
			session.setAttribute("errorMessage", "Failed to add room. Please try again.");
		}
		
		// Redirect to the main dashboard
		return "redirect:/auth/dash";
	}

	@GetMapping("/update")
	public String UpdateRoom(@RequestParam Long id, Model model, HttpSession session) {
		// Get the logged-in hotel
		String username = (String) session.getAttribute("user");
		if (username == null) {
			return "redirect:/auth/davor"; // Redirect to login if not logged in
		}
		
		Hotel hotel = authService.getHotelByUsername(username);
		if (hotel == null) {
			return "redirect:/auth/davor"; // Redirect to login if hotel not found
		}
		
		Room room = service.getRoomById(id);
		
		// Verify that the room belongs to this hotel
		if (room.getHotel().getId() != hotel.getId()) {
			return "redirect:/auth/dash"; // Redirect if room doesn't belong to this hotel
		}
		
		model.addAttribute("user", room);
		return "auth/DashEdit";
	}

	@PostMapping("/edit")
	private String update(@ModelAttribute Room room, HttpSession session) {
		// Get the logged-in hotel
		String username = (String) session.getAttribute("user");
		if (username == null) {
			return "redirect:/auth/davor"; // Redirect to login if not logged in
		}
		
		Hotel hotel = authService.getHotelByUsername(username);
		if (hotel == null) {
			return "redirect:/auth/davor"; // Redirect to login if hotel not found
		}
		
		// Verify that the room belongs to this hotel
		Room existingRoom = service.getRoomById(room.getId());
		if (existingRoom.getHotel().getId() != hotel.getId()) {
			return "redirect:/auth/dash"; // Redirect if room doesn't belong to this hotel
		}
		
		boolean updated = service.update(room.getId(), room);
		
		if (updated) {
			// Add success message to session
			session.setAttribute("successMessage", "Room updated successfully!");
		} else {
			// Add error message to session
			session.setAttribute("errorMessage", "Failed to update room. Please try again.");
		}
		
		return "redirect:/auth/dash";
	}

	@GetMapping("/delete")
	public String deleteRoom(@RequestParam Long id, HttpSession session) {
		// Get the logged-in hotel
		String username = (String) session.getAttribute("user");
		if (username == null) {
			return "redirect:/auth/davor"; // Redirect to login if not logged in
		}
		
		Hotel hotel = authService.getHotelByUsername(username);
		if (hotel == null) {
			return "redirect:/auth/davor"; // Redirect to login if hotel not found
		}
		
		// Verify that the room belongs to this hotel
		Room room = service.getRoomById(id);
		if (room == null || room.getHotel().getId() != hotel.getId()) {
			session.setAttribute("errorMessage", "Room not found or you don't have permission to delete it");
			return "redirect:/auth/dash"; // Redirect if room doesn't belong to this hotel
		}
		
		boolean deleted = service.deleteRoom(id);
		
		if (deleted) {
			session.setAttribute("successMessage", "Room deleted successfully!");
		} else {
			session.setAttribute("errorMessage", "Failed to delete room. Please try again.");
		}
		
		return "redirect:/auth/dash";
	}

	// New method to handle hotel bookings
	@GetMapping("/hotel-bookings")
	public String hotelBookings(Model model, HttpSession session) {
		// Get the logged-in hotel
		String username = (String) session.getAttribute("user");
		if (username == null) {
			return "redirect:/auth/davor"; // Redirect to login if not logged in
		}
		
		Hotel hotel = authService.getHotelByUsername(username);
		if (hotel == null) {
			return "redirect:/auth/davor"; // Redirect to login if hotel not found
		}
		
		// Get bookings for this hotel
		List<Booking> bookings = bookingService.getHotelBookings(hotel.getId());
		
		// Check for success or error messages
		String successMessage = (String) session.getAttribute("successMessage");
		String errorMessage = (String) session.getAttribute("errorMessage");
		
		if (successMessage != null) {
			model.addAttribute("successMessage", successMessage);
			session.removeAttribute("successMessage");
		}
		
		if (errorMessage != null) {
			model.addAttribute("errorMessage", errorMessage);
			session.removeAttribute("errorMessage");
		}
		
		model.addAttribute("hotel", hotel);
		model.addAttribute("bookings", bookings);
		
		return "auth/hotel-bookings";
	}

	@GetMapping("/hotel-guests")
	public String showHotelGuests(Model model, HttpSession session) {
		// Get the logged-in hotel
		String username = (String) session.getAttribute("user");
		if (username == null) {
			return "redirect:/auth/davor"; // Redirect to login if not logged in
		}
		
		Hotel hotel = authService.getHotelByUsername(username);
		if (hotel == null) {
			return "redirect:/auth/davor"; // Redirect to login if hotel not found
		}
		
		// Get bookings for this hotel
		List<Booking> bookings = bookingService.getHotelBookings(hotel.getId());
		
		// Add bookings to model - will be used to display guests
		model.addAttribute("bookings", bookings);
		model.addAttribute("hotel", hotel);
		
		return "auth/hotel-guests";
	}
}
