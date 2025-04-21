package com.dr.DavorRetreats.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dr.DavorRetreats.Service.AuthService;
import com.dr.DavorRetreats.Service.BookingService;
import com.dr.DavorRetreats.Service.UserPass;
import com.dr.DavorRetreats.models.Booking;
import com.dr.DavorRetreats.models.Customer;
import com.dr.DavorRetreats.models.Hotel;
import com.dr.DavorRetreats.repository.CustomerRepository;
import com.dr.DavorRetreats.repository.HotelRepository;
import com.dr.DavorRetreats.repository.RoomRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private AuthService authService;

    @GetMapping("/customer")
    public String showCustomerBookings(HttpSession session, Model model) {
        String username = (String) session.getAttribute("user");
        String role = (String) session.getAttribute("role");
        
        if (username == null || !"customer".equals(role)) {
            return "redirect:/auth/signup";
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
        
        List<Booking> bookings = bookingService.getCustomerBookings(customer.getId());
        model.addAttribute("bookings", bookings);
        return "customer/bookings";
    }

    @GetMapping("/hotel")
    public String showHotelBookings(HttpSession session, Model model) {
        String username = (String) session.getAttribute("user");
        String role = (String) session.getAttribute("role");
        
        if (username == null || !"hotel".equals(role)) {
            return "redirect:/auth/davor";
        }
        
        // Get hotel by username
        Hotel hotel = authService.getHotelByUsername(username);
        if (hotel == null) {
            return "redirect:/auth/davor";
        }

        List<Booking> bookings = bookingService.getHotelBookings(hotel.getId());
        model.addAttribute("bookings", bookings);
        return "hotel/bookings";
    }

    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<?> createBooking(@RequestBody Map<String, Object> bookingData, HttpSession session) {
        try {
            // Debug session attributes
            System.out.println("Session attributes: ");
            System.out.println("User: " + session.getAttribute("user"));
            System.out.println("Role: " + session.getAttribute("role"));
            
            String username = (String) session.getAttribute("user");
            String role = (String) session.getAttribute("role");
            
            if (username == null || !"customer".equals(role)) {
                System.out.println("User not logged in or not a customer. Username: " + username + ", Role: " + role);
                return ResponseEntity.badRequest().body(Map.of("error", "User not logged in or not a customer"));
            }
            
            // Encrypt the username to match the database
            String encryptedUsername = UserPass.encrypt(username);
            System.out.println("Original username: " + username);
            System.out.println("Encrypted username: " + encryptedUsername);
            
            // Find customer with encrypted username
            List<Customer> allCustomers = customerRepository.findAll();
            Customer customer = null;
            
            System.out.println("Looking for customer with encrypted username: " + encryptedUsername);
            System.out.println("Total customers in database: " + allCustomers.size());
            
            for (Customer c : allCustomers) {
                System.out.println("Checking customer: " + c.getId() + " - Username: '" + c.getUsername() + "'");
                if (encryptedUsername.equals(c.getUsername())) {
                    customer = c;
                    System.out.println("Found matching customer: " + c.getId() + " - " + c.getUsername());
                    System.out.println("Decrypted username: " + UserPass.decrypt(c.getUsername()));
                    break;
                }
            }
            
            if (customer == null) {
                System.out.println("No matching customer found for encrypted username: " + encryptedUsername);
                return ResponseEntity.badRequest().body(Map.of("error", "Customer not found. Please log in again."));
            }
            
            int customerId = customer.getId();
            
            // Get booking data
            Long roomId = Long.parseLong(bookingData.get("roomId").toString());
            Integer hotelId = Integer.parseInt(bookingData.get("hotelId").toString());
            String guestName = (String) bookingData.get("guestName");
            String guestEmail = (String) bookingData.get("guestEmail");
            String guestPhone = (String) bookingData.get("guestPhone");
            LocalDate checkInDate = LocalDate.parse(bookingData.get("checkInDate").toString());
            LocalDate checkOutDate = LocalDate.parse(bookingData.get("checkOutDate").toString());
            String specialRequests = (String) bookingData.get("specialRequests");

            // Check if room is available
            if (!bookingService.isRoomAvailable(roomId, checkInDate, checkOutDate)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Room is not available for selected dates"));
            }

            Booking booking = bookingService.createBooking(
                roomId, customerId, hotelId, guestName, guestEmail, guestPhone,
                checkInDate, checkOutDate, specialRequests
            );

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Booking created successfully",
                "bookingId", booking.getId()
            ));
        } catch (Exception e) {
            e.printStackTrace(); // Add this for debugging
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/update-status")
    @ResponseBody
    public ResponseEntity<?> updateBookingStatus(@RequestBody Map<String, Object> statusData) {
        try {
            Long bookingId = Long.parseLong(statusData.get("bookingId").toString());
            String status = (String) statusData.get("status");

            Booking booking = bookingService.updateBookingStatus(bookingId, status);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Booking status updated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 