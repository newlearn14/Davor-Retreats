package com.dr.DavorRetreats.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dr.DavorRetreats.models.Hotel;
import com.dr.DavorRetreats.models.HotelImage;
import com.dr.DavorRetreats.repository.HotelImageRepository;
import com.dr.DavorRetreats.repository.HotelRepository;

@Service
public class HotelImageService {

    @Autowired
    private HotelImageRepository hotelImageRepository;
    
    @Autowired
    private HotelRepository hotelRepository;
    
    /**
     * Upload multiple images for a hotel
     * 
     * @param hotelId The ID of the hotel
     * @param files Array of image files to upload
     * @param titles Array of image titles (optional)
     * @param descriptions Array of image descriptions (optional)
     * @return List of saved hotel images
     */
    public List<HotelImage> uploadHotelImages(int hotelId, MultipartFile[] files, String[] titles, String[] descriptions) {
        Hotel hotel = hotelRepository.findById(hotelId).orElse(null);
        if (hotel == null) {
            return null;
        }
        
        List<HotelImage> savedImages = new ArrayList<>();
        
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            if (file != null && !file.isEmpty()) {
                try {
                    HotelImage image = new HotelImage();
                    image.setHotel(hotel);
                    image.setImageData(file.getBytes());
                    
                    // Set title if available
                    if (titles != null && i < titles.length) {
                        image.setImageTitle(titles[i]);
                    } else {
                        image.setImageTitle("Hotel Image " + (i + 1));
                    }
                    
                    // Set description if available
                    if (descriptions != null && i < descriptions.length) {
                        image.setImageDescription(descriptions[i]);
                    }
                    
                    savedImages.add(hotelImageRepository.save(image));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return savedImages;
    }
    
    /**
     * Get all images for a hotel
     * 
     * @param hotelId The ID of the hotel
     * @return List of hotel images
     */
    public List<HotelImage> getHotelImages(int hotelId) {
        return hotelImageRepository.findByHotelId(hotelId);
    }
    
    /**
     * Delete a hotel image
     * 
     * @param imageId The ID of the image to delete
     * @param hotelId The ID of the hotel (for security check)
     * @return true if deleted successfully, false otherwise
     */
    public boolean deleteHotelImage(int imageId, int hotelId) {
        HotelImage image = hotelImageRepository.findById(imageId).orElse(null);
        if (image != null && image.getHotel().getId() == hotelId) {
            hotelImageRepository.delete(image);
            return true;
        }
        return false;
    }
    
    /**
     * Convert image data to Base64 for display in HTML
     * 
     * @param imageData The binary image data
     * @return Base64 encoded string
     */
    public String getBase64Image(byte[] imageData) {
        return Base64.getEncoder().encodeToString(imageData);
    }
} 