package com.dr.DavorRetreats.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "hotels",uniqueConstraints = {@UniqueConstraint(columnNames = "username")})
public class Hotel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private int id;
	private String hotelname;
	private String ownerName;
	@Column(name="data",columnDefinition = "bytea")
	private byte[] hotelimg;
	private String gstno;
	private String mobile;
	private String totalroom;
	@Column(name = "username",unique = true,nullable = false)
	private String username;
	private String password;
	private String hotelDescription;
	
	@OneToMany(mappedBy = "hotel")
	private List<Room> rooms = new ArrayList<>();
	
	@OneToMany(mappedBy = "hotel")
	private List<HotelImage> images = new ArrayList<>();
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getHotelname() {
		return hotelname;
	}
	public void setHotelname(String hotelname) {
		this.hotelname = hotelname;
	}
	public String getOwnerName() {
		return ownerName;
	}
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}
	public byte[] getHotelimg() {
		return hotelimg;
	}
	public void setHotelimg(byte[] hotelimg) {
		this.hotelimg = hotelimg;
	}
	public String getGstno() {
		return gstno;
	}
	public void setGstno(String gstno) {
		this.gstno = gstno;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getTotalroom() {
		return totalroom;
	}
	public void setTotalroom(String totalroom) {
		this.totalroom = totalroom;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getHotelDescription() {
		return hotelDescription;
	}
	public void setHotelDescription(String hotelDescription) {
		this.hotelDescription = hotelDescription;
	}
	
	public List<Room> getRooms() {
		return rooms;
	}
	
	public void setRooms(List<Room> rooms) {
		this.rooms = rooms;
	}
	
	public List<HotelImage> getImages() {
		return images;
	}
	
	public void setImages(List<HotelImage> images) {
		this.images = images;
	}
}
