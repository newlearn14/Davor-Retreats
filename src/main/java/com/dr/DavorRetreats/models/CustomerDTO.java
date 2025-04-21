package com.dr.DavorRetreats.models;

public class CustomerDTO {

	private String username;
	private String password;
	private String cfpassword;
	private String custname;
	private long mobile;
	private String email;

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
