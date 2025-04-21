package com.dr.DavorRetreats.controller;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.dr.DavorRetreats.Service.AuthService;
import com.dr.DavorRetreats.Service.BookingService;
import com.dr.DavorRetreats.Service.DashService;
import com.dr.DavorRetreats.Service.HotelImageService;
import com.dr.DavorRetreats.Service.UserPass;
import com.dr.DavorRetreats.models.Booking;
import com.dr.DavorRetreats.models.Customer;
import com.dr.DavorRetreats.models.Hotel;
import com.dr.DavorRetreats.models.HotelImage;
import com.dr.DavorRetreats.models.Room;
import com.dr.DavorRetreats.repository.CustomerRepository;
import com.dr.DavorRetreats.repository.HotelRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private DashService dashService;
    
    @Autowired
    private HotelImageService hotelImageService;
    
    @Autowired
    private HotelRepository hotelRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private BookingService bookingService;
    
    @GetMapping("/dashboard")
    public String customerDashboard(Model model, HttpSession session) {
        // Check if user is logged in
        String username = (String) session.getAttribute("user");
        String role = (String) session.getAttribute("role");
        
        if (username == null || !"customer".equals(role)) {
            return "redirect:/auth/signup"; // Redirect to login if not logged in as customer
        }
        
        // Get all hotels
        List<Hotel> hotels = hotelRepository.findAll();
        List<Map<String, Object>> hotelList = new ArrayList<>();
        
        for (Hotel hotel : hotels) {
            Map<String, Object> hotelMap = new HashMap<>();
            hotelMap.put("id", hotel.getId());
            hotelMap.put("name", hotel.getHotelname());
            hotelMap.put("description", hotel.getHotelDescription());
            
            // Add hotel image if available
            if (hotel.getHotelimg() != null) {
                String base64Image = Base64.getEncoder().encodeToString(hotel.getHotelimg());
                hotelMap.put("image", base64Image);
            }
            
            // Get available rooms count
            List<Room> rooms = dashService.getRoomsByHotelId(hotel.getId());
            long availableRooms = rooms.stream().filter(r -> "AVAILABLE".equalsIgnoreCase(r.getStatus())).count();
            hotelMap.put("availableRooms", availableRooms);
            
            hotelList.add(hotelMap);
        }
        
        model.addAttribute("hotels", hotelList);
        return "auth/customer-dashboard";
    }
    
    @GetMapping("/hotel/{id}")
    public String viewHotel(@PathVariable("id") int hotelId, Model model, HttpSession session) {
        // Check if user is logged in
        String username = (String) session.getAttribute("user");
        String role = (String) session.getAttribute("role");
        
        if (username == null || !"customer".equals(role)) {
            return "redirect:/auth/signup"; // Redirect to login if not logged in as customer
        }
        
        // Get hotel details
        Hotel hotel = hotelRepository.findById(hotelId).orElse(null);
        if (hotel == null) {
            return "redirect:/customer/dashboard";
        }
        
        // Get hotel images
        List<HotelImage> hotelImages = hotelImageService.getHotelImages(hotelId);
        List<String> galleryImages = new ArrayList<>();
        
        for (HotelImage image : hotelImages) {
            galleryImages.add(hotelImageService.getBase64Image(image.getImageData()));
        }
        
        // Get available rooms
        List<Room> allRooms = dashService.getRoomsByHotelId(hotelId);
        List<Room> availableRooms = allRooms.stream()
                .filter(r -> "AVAILABLE".equalsIgnoreCase(r.getStatus()))
                .toList();
        
        // Add hotel image if available
        if (hotel.getHotelimg() != null) {
            String base64Image = Base64.getEncoder().encodeToString(hotel.getHotelimg());
            model.addAttribute("hotelImage", base64Image);
        }
        
        model.addAttribute("hotel", hotel);
        model.addAttribute("galleryImages", galleryImages);
        model.addAttribute("availableRooms", availableRooms);
        
        return "auth/hotel-details";
    }
    
    @PostMapping("/book/{hotelId}/{roomId}")
    public String bookRoom(
            @PathVariable("hotelId") int hotelId,
            @PathVariable("roomId") Long roomId,
            HttpSession session,
            Model model) {
        
        // Check if user is logged in
        String username = (String) session.getAttribute("user");
        String role = (String) session.getAttribute("role");
        
        if (username == null || !"customer".equals(role)) {
            return "redirect:/auth/signup"; // Redirect to login if not logged in as customer
        }
        
        // Get the room
        Room room = dashService.getRoomById(roomId);
        if (room == null || room.getHotel().getId() != hotelId || !"AVAILABLE".equalsIgnoreCase(room.getStatus())) {
            session.setAttribute("errorMessage", "Room is not available for booking");
            return "redirect:/customer/hotel/" + hotelId;
        }
        
        // Update room status to OCCUPIED
        room.setStatus("OCCUPIED");
        dashService.update(roomId, room);
        
        session.setAttribute("successMessage", "Room booked successfully!");
        return "redirect:/customer/bookings";
    }
    
    @GetMapping("/bookings")
    public String viewBookings(Model model, HttpSession session) {
        // Check if user is logged in
        String username = (String) session.getAttribute("user");
        String role = (String) session.getAttribute("role");
        
        if (username == null || !"customer".equals(role)) {
            return "redirect:/auth/signup"; // Redirect to login if not logged in as customer
        }
        
        // Encrypt the username to match the database
        String encryptedUsername = UserPass.encrypt(username);
        
        // Find customer with encrypted username
        List<Customer> allCustomers = customerRepository.findAll();
        Customer customer = null;
        
        for (Customer c : allCustomers) {
            if (encryptedUsername.equals(c.getUsername())) {
                customer = c;
                break;
            }
        }
        
        if (customer == null) {
            return "redirect:/auth/signup";
        }
        
        // Get customer's bookings
        List<Booking> bookings = bookingService.getCustomerBookings(customer.getId());
        model.addAttribute("bookings", bookings);
        
        return "auth/customer-bookings";
    }
} 