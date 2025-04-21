package com.dr.DavorRetreats.Service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dr.DavorRetreats.models.Customer;
import com.dr.DavorRetreats.models.CustomerDTO;
import com.dr.DavorRetreats.models.Hotel;
import com.dr.DavorRetreats.models.HotelTO;
import com.dr.DavorRetreats.repository.CustomerRepository;
import com.dr.DavorRetreats.repository.HotelRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class AuthService {

	@Autowired
	HotelRepository repository;
	
	@Autowired
	CustomerRepository customerRepository;

	@Autowired
	JdbcTemplate jdbcTemplate;

	public boolean register(HotelTO hotelTO, MultipartFile file) {
		try {
			byte[] imagebytes = null;
			if (file != null && !file.isEmpty()) {
				imagebytes = file.getBytes();
			}
			String epassword = UserPass.encrypt(hotelTO.getPassword());
			String eusername = UserPass.encrypt(hotelTO.getUsername());
			Hotel hotel = new Hotel();
			BeanUtils.copyProperties(hotelTO, hotel);
			hotel.setHotelimg(imagebytes);
			hotel.setPassword(epassword);
			hotel.setUsername(eusername);
			
			// Create the hotels table if it doesn't exist
			try {
				jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS hotels (" +
					"id SERIAL PRIMARY KEY, " +
					"hotelname VARCHAR(255), " +
					"owner_name VARCHAR(255), " +
					"data BYTEA, " +
					"gstno VARCHAR(255), " +
					"mobile VARCHAR(255), " +
					"totalroom VARCHAR(255), " +
					"username VARCHAR(255) UNIQUE NOT NULL, " +
					"password VARCHAR(255), " +
					"hotel_description TEXT)");
			} catch (Exception e) {
				System.out.println("Error creating hotels table: " + e.getMessage());
				// Continue anyway, as the table might already exist
			}
			
			repository.save(hotel);
			return true;
		} catch (Exception e) {
			System.out.println("Error during registration: " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	public Map<String, String> login(String username, String password, HttpSession session) {
		Map<String, String> result = new HashMap<>();
		result.put("success", "false");
		
		String epassword = UserPass.encrypt(password);
		String eusername = UserPass.encrypt(username);
		
		// Try hotel login first
		Map<String, Object> hotelData = getUserFromTable("hotels", eusername, epassword);
		if (hotelData != null) {
			session.setAttribute("user", username);
			session.setAttribute("role", "hotel");
			result.put("success", "true");
			result.put("role", "hotel");
			return result;
		}
		
		// Try customer login
		Map<String, Object> customerData = getUserFromTable("customers", eusername, epassword);
		if (customerData != null) {
			session.setAttribute("user", username);
			session.setAttribute("role", "customer");
			result.put("success", "true");
			result.put("role", "customer");
			return result;
		}
		
		return result;
	}

	public Map<String, String> customerLogin(String username, String password, HttpSession session) {
		Map<String, String> result = new HashMap<>();
		result.put("success", "false");
		
		String epassword = UserPass.encrypt(password);
		String eusername = UserPass.encrypt(username);
		
		Map<String, Object> customerData = getUserFromTable("customers", eusername, epassword);
		if (customerData != null) {
			session.setAttribute("user", username);
			session.setAttribute("role", "customer");
			result.put("success", "true");
			return result;
		}
		
		return result;
	}

	public boolean registerCustomer(CustomerDTO customerDTO, MultipartFile file) {
		try {
			byte[] imageBytes = null;
			if (file != null && !file.isEmpty()) {
				imageBytes = file.getBytes();
			}
			
			String epassword = UserPass.encrypt(customerDTO.getPassword());
			String eusername = UserPass.encrypt(customerDTO.getUsername());
			
			Customer customer = new Customer();
			BeanUtils.copyProperties(customerDTO, customer);
			customer.setPassword(epassword);
			customer.setUsername(eusername);
			customer.setImage(imageBytes);
			
			// Create the customers table if it doesn't exist
			try {
				jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS customers (" +
					"id SERIAL PRIMARY KEY, " +
					"username VARCHAR(255) UNIQUE NOT NULL, " +
					"password VARCHAR(255), " +
					"cfpassword VARCHAR(255), " +
					"custname VARCHAR(255), " +
					"data BYTEA, " +
					"mobile BIGINT, " +
					"email VARCHAR(255))");
			} catch (Exception e) {
				System.out.println("Error creating customers table: " + e.getMessage());
				// Continue anyway, as the table might already exist
			}
			
			customerRepository.save(customer);
			return true;
		} catch (Exception e) {
			System.out.println("Error during customer registration: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	public void logout(HttpSession session) {
		session.invalidate();
	}

	public boolean isUserLoggedIn(HttpSession session) {
		return session.getAttribute("user") != null;
	}

	private Map<String, Object> getUserFromTable(String table, String username, String password) {
		String query = "SELECT * FROM " + table + " WHERE username = ? AND password = ?";
		try {
			return jdbcTemplate.queryForMap(query, username, password);
		} catch (Exception e) {
			return null;
		}
	}
	
	public Hotel getHotelByUsername(String username) {
		String encryptedUsername = UserPass.encrypt(username);
		
		// Create the hotels table if it doesn't exist
		try {
			jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS hotels (" +
				"id SERIAL PRIMARY KEY, " +
				"hotelname VARCHAR(255), " +
				"owner_name VARCHAR(255), " +
				"data BYTEA, " +
				"gstno VARCHAR(255), " +
				"mobile VARCHAR(255), " +
				"totalroom VARCHAR(255), " +
				"username VARCHAR(255) UNIQUE NOT NULL, " +
				"password VARCHAR(255), " +
				"hotel_description TEXT)");
		} catch (Exception e) {
			System.out.println("Error creating hotels table: " + e.getMessage());
			// Continue anyway, as the table might already exist
		}
		
		String query = "SELECT id FROM hotels WHERE username = ?";
		try {
			Integer hotelId = jdbcTemplate.queryForObject(query, Integer.class, encryptedUsername);
			if (hotelId != null) {
				return repository.findById(hotelId).orElse(null);
			}
		} catch (Exception e) {
			System.out.println("Error getting hotel by username: " + e.getMessage());
			// Handle exception or log it
		}
		return null;
	}

	public boolean updateHotelProfile(int hotelId, Hotel updatedHotel) {
		try {
			Hotel existingHotel = repository.findById(hotelId)
					.orElseThrow(() -> new RuntimeException("Hotel not found"));
			
			// Update fields but preserve sensitive data and relationships
			existingHotel.setHotelname(updatedHotel.getHotelname());
			existingHotel.setOwnerName(updatedHotel.getOwnerName());
			existingHotel.setMobile(updatedHotel.getMobile());
			existingHotel.setGstno(updatedHotel.getGstno());
			existingHotel.setTotalroom(updatedHotel.getTotalroom());
			existingHotel.setHotelDescription(updatedHotel.getHotelDescription());
			
			// Update password if provided
			if (updatedHotel.getPassword() != null && !updatedHotel.getPassword().isEmpty()) {
				String encryptedPassword = UserPass.encrypt(updatedHotel.getPassword());
				existingHotel.setPassword(encryptedPassword);
			}
			
			// Preserve the existing image if no new image is provided
			if (updatedHotel.getHotelimg() == null) {
				updatedHotel.setHotelimg(existingHotel.getHotelimg());
			}
			
			// Save the updated hotel
			Hotel savedHotel = repository.save(existingHotel);
			return savedHotel != null;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean updateHotelImage(int hotelId, MultipartFile file) {
		try {
			if (file.isEmpty()) {
				return false;
			}
			
			Hotel existingHotel = repository.findById(hotelId)
					.orElseThrow(() -> new RuntimeException("Hotel not found"));
			
			byte[] imageBytes = file.getBytes();
			existingHotel.setHotelimg(imageBytes);
			
			repository.save(existingHotel);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
