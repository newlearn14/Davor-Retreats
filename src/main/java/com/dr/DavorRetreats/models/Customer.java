package com.dr.DavorRetreats.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "Customers",uniqueConstraints = {@UniqueConstraint(columnNames = "username")})
public class Customer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
	@Column(name = "username",unique = true,nullable = false)
	private String username;
	private String password;
	private String cfpassword;
	private String custname;
	@Column(name="data",columnDefinition = "bytea")
	private byte[] image;
	private long mobile;
	private String email;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
	public String getCfpassword() {
		return cfpassword;
	}
	public void setCfpassword(String cfpassword) {
		this.cfpassword = cfpassword;
	}
	public String getCustname() {
		return custname;
	}
	public void setCustname(String custname) {
		this.custname = custname;
	}
	public byte[] getImage() {
		return image;
	}
	public void setImage(byte[] image) {
		this.image = image;
	}
	public long getMobile() {
		return mobile;
	}
	public void setMobile(long mobile) {
		this.mobile = mobile;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	
}
