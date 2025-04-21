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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.dr.DavorRetreats.Service.AuthService;
import com.dr.DavorRetreats.Service.DashService;
import com.dr.DavorRetreats.Service.HotelImageService;
import com.dr.DavorRetreats.models.CustomerDTO;
import com.dr.DavorRetreats.models.Hotel;
import com.dr.DavorRetreats.models.HotelImage;
import com.dr.DavorRetreats.models.HotelTO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/auth")
public class AdminController {

	@Autowired
	AuthService authService;
	
	@Autowired
	DashService dashService;
	
	@Autowired
	HotelImageService hotelImageService;
	
	@GetMapping("/signup")
	public String RegisterCustomer(Model model) {
		model.addAttribute("customer",new CustomerDTO());
		return "auth/Cust_register";
	}
	@GetMapping("/davor")
	public String RegisterHotel(Model model) {
		model.addAttribute("hotel", new HotelTO());
		return "auth/hotel_register";
	}
	
	@PostMapping("/davor")
	public String RegistrationData(
			@RequestParam("ownerName") String ownerName,
			@RequestParam("hotelname") String hotelname,
			@RequestParam("gstno") String gstno,
			@RequestParam("mobile") String mobile,
			@RequestParam("totalroom") String totalroom,
			@RequestParam("username") String username,
			@RequestParam("password") String password,
			@RequestParam(value="cfpassword", required=false) String cfpassword,
			@RequestParam("hotelDescription") String hotelDescription,
			@RequestParam("manager_photo") MultipartFile file,
			HttpSession session) {
		
		// Validate that password and confirm password match
		if (cfpassword != null && !password.equals(cfpassword)) {
			session.setAttribute("errorMessage", "Passwords do not match");
			return "redirect:/auth/davor";
		}
		
		// Create a HotelTO object manually
		HotelTO hotelTO = new HotelTO();
		hotelTO.setOwnerName(ownerName);
		hotelTO.setHotelname(hotelname);
		hotelTO.setGstno(gstno);
		hotelTO.setMobile(mobile);
		hotelTO.setTotalroom(totalroom);
		hotelTO.setUsername(username);
		hotelTO.setPassword(password);
		hotelTO.setCfpassword(cfpassword);
		hotelTO.setHotelDescription(hotelDescription);
		
		boolean register = authService.register(hotelTO, file);
		
		if (register) {
			// Set the session attributes for the newly registered hotel
			session.setAttribute("user", username);
			session.setAttribute("role", "hotel");
			session.setAttribute("successMessage", "Registration successful! Welcome to DavorRetreats.");
			return "redirect:/auth/dash";
		} else {
			session.setAttribute("errorMessage", "Registration failed. Please try again.");
			return "redirect:/auth/davor";
		}
	}

	@PostMapping("/login")
	public String login(@RequestParam String username, @RequestParam String password, HttpSession session,
			Model model) {
		Map<String, String> loginResult = authService.login(username, password, session);
		boolean isValidUser = Boolean.parseBoolean(loginResult.get("success"));
		String role = loginResult.get("role");

		if (isValidUser) {
			session.setAttribute("successMessage", "Welcome back, " + username + "!");
			if ("customer".equals(role)) {
				return "redirect:/customer/dashboard";
			} else {
				return "redirect:/auth/dash";
			}
		}

		session.setAttribute("errorMessage", "Invalid credentials");
		return "redirect:/auth/davor";
	}
	
	@PostMapping("/customer-login")
	public String customerLogin(@RequestParam String username, @RequestParam String password, HttpSession session,
			Model model) {
		Map<String, String> loginResult = authService.customerLogin(username, password, session);
		boolean isValidUser = Boolean.parseBoolean(loginResult.get("success"));

		if (isValidUser) {
			session.setAttribute("successMessage", "Welcome back, " + username + "!");
			return "redirect:/customer/dashboard";
		}

		session.setAttribute("errorMessage", "Invalid credentials");
		return "redirect:/auth/signup";
	}
	
	@PostMapping("/customer-register")
	public String registerCustomer(@ModelAttribute CustomerDTO customerDTO, @RequestParam("customer_photo") MultipartFile file,
			HttpSession session, Model model) {
		
		// Validate that password and confirm password match
		if (!customerDTO.getPassword().equals(customerDTO.getCfpassword())) {
			model.addAttribute("error", "Passwords do not match");
			model.addAttribute("customer", customerDTO);
			return "auth/Cust_register";
		}
		
		boolean registered = authService.registerCustomer(customerDTO, file);
		
		if (registered) {
			session.setAttribute("user", customerDTO.getUsername());
			session.setAttribute("role", "customer");
			return "redirect:/customer/dashboard";
		} else {
			model.addAttribute("error", "Registration failed. Please try again.");
			model.addAttribute("customer", customerDTO);
			return "auth/Cust_register";
		}
	}

	@PostMapping("/logout")
	public String logout(HttpSession session, Model model) {
		authService.logout(session);
		return "redirect:/auth/davor";
	}

	@GetMapping("/logout")
	public String logoutGet(HttpSession session) {
		authService.logout(session);
		return "redirect:/auth/davor";
	}

	@GetMapping("/profile")
	public String showProfile(Model model, HttpSession session) {
		// Get the logged-in hotel
		String username = (String) session.getAttribute("user");
		if (username == null) {
			return "redirect:/auth/davor"; // Redirect to login if not logged in
		}
		
		Hotel hotel = authService.getHotelByUsername(username);
		if (hotel == null) {
			return "redirect:/auth/davor"; // Redirect to login if hotel not found
		}
		
		// Add Base64 encoded image if available
		if (hotel.getHotelimg() != null) {
			String base64Image = Base64.getEncoder().encodeToString(hotel.getHotelimg());
			model.addAttribute("hotelImageBase64", base64Image);
		}
		
		model.addAttribute("hotel", hotel);
		return "auth/profile";
	}

	@PostMapping("/updateProfile")
	@ResponseBody
	public Map<String, Object> updateProfile(@ModelAttribute Hotel updatedHotel, HttpSession session) {
		Map<String, Object> response = new HashMap<>();
		
		// Get the logged-in hotel
		String username = (String) session.getAttribute("user");
		if (username == null) {
			response.put("success", false);
			response.put("message", "Session expired. Please login again.");
			return response;
		}
		
		Hotel existingHotel = authService.getHotelByUsername(username);
		if (existingHotel == null) {
			response.put("success", false);
			response.put("message", "Hotel not found. Please login again.");
			return response;
		}
		
		// Set the ID and username of the updated hotel to match the existing one
		updatedHotel.setId(existingHotel.getId());
		updatedHotel.setUsername(existingHotel.getUsername());
		
		// Update hotel details
		boolean updated = authService.updateHotelProfile(existingHotel.getId(), updatedHotel);
		
		if (updated) {
			response.put("success", true);
			response.put("message", "Profile updated successfully!");
			// Update the session user name if hotel name was changed
			session.setAttribute("user", updatedHotel.getUsername());
		} else {
			response.put("success", false);
			response.put("message", "Failed to update profile. Please try again.");
		}
		
		return response;
	}

	@PostMapping("/updateProfileImage")
	public String updateProfileImage(@RequestParam("profileImage") MultipartFile file, HttpSession session) {
		// Get the logged-in hotel
		String username = (String) session.getAttribute("user");
		if (username == null) {
			return "redirect:/auth/davor"; // Redirect to login if not logged in
		}
		
		Hotel hotel = authService.getHotelByUsername(username);
		if (hotel == null) {
			return "redirect:/auth/davor"; // Redirect to login if hotel not found
		}
		
		// Update profile image
		boolean updated = authService.updateHotelImage(hotel.getId(), file);
		
		if (updated) {
			session.setAttribute("successMessage", "Profile picture updated successfully!");
		} else {
			session.setAttribute("errorMessage", "Failed to update profile picture. Please try again.");
		}
		
		return "redirect:/auth/profile";
	}

	@GetMapping("/hotel-gallery")
	public String showHotelGallery(Model model, HttpSession session) {
		// Get the logged-in hotel
		String username = (String) session.getAttribute("user");
		if (username == null) {
			return "redirect:/auth/davor"; // Redirect to login if not logged in
		}
		
		Hotel hotel = authService.getHotelByUsername(username);
		if (hotel == null) {
			return "redirect:/auth/davor"; // Redirect to login if hotel not found
		}
		
		// Get all images for this hotel
		List<HotelImage> hotelImages = hotelImageService.getHotelImages(hotel.getId());
		
		// Convert image data to Base64 for display
		List<Map<String, Object>> galleryImages = new ArrayList<>();
		for (HotelImage image : hotelImages) {
			Map<String, Object> imageMap = new HashMap<>();
			imageMap.put("id", image.getId());
			imageMap.put("title", image.getImageTitle());
			imageMap.put("description", image.getImageDescription());
			imageMap.put("base64", hotelImageService.getBase64Image(image.getImageData()));
			galleryImages.add(imageMap);
		}
		
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
		model.addAttribute("galleryImages", galleryImages);
		
		return "auth/hotel-gallery";
	}
	
	@PostMapping("/upload-gallery-images")
	public String uploadGalleryImages(
			@RequestParam("galleryImages") MultipartFile[] files,
			@RequestParam(value = "imageTitles", required = false) String[] titles,
			@RequestParam(value = "imageDescriptions", required = false) String[] descriptions,
			HttpSession session) {
		
		// Get the logged-in hotel
		String username = (String) session.getAttribute("user");
		if (username == null) {
			return "redirect:/auth/davor"; // Redirect to login if not logged in
		}
		
		Hotel hotel = authService.getHotelByUsername(username);
		if (hotel == null) {
			return "redirect:/auth/davor"; // Redirect to login if hotel not found
		}
		
		// Upload the images
		List<HotelImage> savedImages = hotelImageService.uploadHotelImages(hotel.getId(), files, titles, descriptions);
		
		if (savedImages != null && !savedImages.isEmpty()) {
			session.setAttribute("successMessage", savedImages.size() + " images uploaded successfully!");
		} else {
			session.setAttribute("errorMessage", "Failed to upload images. Please try again.");
		}
		
		return "redirect:/auth/hotel-gallery";
	}
	
	@PostMapping("/delete-gallery-image/{imageId}")
	public String deleteGalleryImage(@PathVariable("imageId") int imageId, HttpSession session) {
		// Get the logged-in hotel
		String username = (String) session.getAttribute("user");
		if (username == null) {
			return "redirect:/auth/davor"; // Redirect to login if not logged in
		}
		
		Hotel hotel = authService.getHotelByUsername(username);
		if (hotel == null) {
			return "redirect:/auth/davor"; // Redirect to login if hotel not found
		}
		
		// Delete the image
		boolean deleted = hotelImageService.deleteHotelImage(imageId, hotel.getId());
		
		if (deleted) {
			session.setAttribute("successMessage", "Image deleted successfully!");
		} else {
			session.setAttribute("errorMessage", "Failed to delete image. Please try again.");
		}
		
		return "redirect:/auth/hotel-gallery";
	}
}
