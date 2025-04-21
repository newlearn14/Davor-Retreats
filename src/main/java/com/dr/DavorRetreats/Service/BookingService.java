package com.dr.DavorRetreats.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dr.DavorRetreats.models.Booking;
import com.dr.DavorRetreats.models.Customer;
import com.dr.DavorRetreats.models.Hotel;
import com.dr.DavorRetreats.models.Room;
import com.dr.DavorRetreats.repository.BookingRepository;
import com.dr.DavorRetreats.repository.CustomerRepository;
import com.dr.DavorRetreats.repository.HotelRepository;
import com.dr.DavorRetreats.repository.RoomRepository;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private HotelRepository hotelRepository;

    public Booking createBooking(Long roomId, int customerId, int hotelId, String guestName, 
                               String guestEmail, String guestPhone, LocalDate checkInDate, 
                               LocalDate checkOutDate, String specialRequests) {
        
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        // Calculate total price based on number of nights and room price
        long numberOfNights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        double totalPrice = numberOfNights * room.getPrice();

        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setCustomer(customer);
        booking.setHotel(hotel);
        booking.setGuestName(guestName);
        booking.setGuestEmail(guestEmail);
        booking.setGuestPhone(guestPhone);
        booking.setCheckInDate(checkInDate);
        booking.setCheckOutDate(checkOutDate);
        booking.setSpecialRequests(specialRequests);
        booking.setTotalPrice(totalPrice);
        booking.setStatus("CONFIRMED");
        
        // Update room status to OCCUPIED
        room.setStatus("OCCUPIED");
        roomRepository.save(room);

        return bookingRepository.save(booking);
    }

    public List<Booking> getCustomerBookings(int customerId) {
        return bookingRepository.findByCustomerId(customerId);
    }

    public List<Booking> getHotelBookings(int hotelId) {
        return bookingRepository.findByHotelId(hotelId);
    }

    public Booking updateBookingStatus(Long bookingId, String status) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        // Get the room associated with this booking
        Room room = booking.getRoom();
        
        // Update room status based on booking status
        if ("CONFIRMED".equals(status)) {
            room.setStatus("OCCUPIED");
            roomRepository.save(room);
        } else if ("CANCELLED".equals(status) && "OCCUPIED".equals(room.getStatus())) {
            // Only change room status if it was previously occupied by this booking
            room.setStatus("AVAILABLE");
            roomRepository.save(room);
        }
        
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    public boolean isRoomAvailable(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        
        // First check if room status is AVAILABLE
        if (!"AVAILABLE".equalsIgnoreCase(room.getStatus())) {
            return false;
        }
        
        // Then check for existing bookings
        List<Booking> existingBookings = bookingRepository.findByRoomId(roomId);
        
        for (Booking booking : existingBookings) {
            if (booking.getStatus().equals("CONFIRMED") || booking.getStatus().equals("PENDING")) {
                if ((checkInDate.isBefore(booking.getCheckOutDate()) && 
                     checkOutDate.isAfter(booking.getCheckInDate()))) {
                    return false;
                }
            }
        }
        
        return true;
    }

    public List<Booking> getActiveBookingsByRoomId(Long roomId) {
        List<Booking> allBookings = bookingRepository.findByRoomId(roomId);
        return allBookings.stream()
                .filter(booking -> "CONFIRMED".equals(booking.getStatus()) || "PENDING".equals(booking.getStatus()))
                .toList();
    }
} 