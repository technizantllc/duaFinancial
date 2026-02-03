package com.dua.finance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

public class Donor {

	public static final Logger logger = LogManager.getLogger(Donor.class);

	public Donor() {

	}

	public Donor(String fullName, String firstName, String lastName, String email, BigDecimal donationAmount, String phone,
			String address1, String city, String state, String zip, String mode, String project) {
		this.fullName = fullName;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.donationAmount = donationAmount;
		this.phone = phone;
		this.address1 = Utility.getCamelCase(address1);
		this.city = Utility.getCamelCase(city);
		this.state = state;
		this.zip = zip;
		this.project = project;
		this.mode = mode;

		if (city == null && address1 != null) {
			String[] temp = address1.split(",");
			if (temp.length > 0)
				this.address1 = temp[0].trim();
			if (temp.length > 1)
				this.city = Utility.getCamelCase(temp[1].trim());
			if (temp.length > 2)
				this.state = temp[2].trim();
			if (temp.length > 3 & this.zip == null)
				this.zip = temp[3].trim();
		}
		if (this.state != null) {
			this.state = this.state.toUpperCase();
			if ("TEXAS".equals(this.state))
				this.state = "TX";

		}
	}

	@CsvBindByName(column = "DonorId")
	@CsvBindByPosition(position = 0)
	public int donorId;

	@CsvBindByName(column = "Full Name")
	@CsvBindByPosition(position = 1)
	public String fullName;

	@CsvBindByName(column = "First Name")
	@CsvBindByPosition(position = 2)
	public String firstName;

	@CsvBindByName(column = "Last Name")
	@CsvBindByPosition(position = 3)
	public String lastName;

	@CsvBindByName(column = "Email")
	@CsvBindByPosition(position = 4)
	public String email;

	@CsvBindByName(column = "Phone")
	@CsvBindByPosition(position = 5)
	public String phone;

	@CsvBindByName(column = "Address Line1")
	@CsvBindByPosition(position = 6)
	public String address1;

	@CsvBindByName(column = "City")
	@CsvBindByPosition(position = 7)
	public String city;

	@CsvBindByName(column = "State")
	@CsvBindByPosition(position = 8)
	public String state;

	@CsvBindByName(column = "Zip")
	@CsvBindByPosition(position = 9)
	public String zip;

	@CsvBindByName(column = "Donation Amount")
	@CsvBindByPosition(position = 10)
	public BigDecimal donationAmount = BigDecimal.ZERO;

	@CsvBindByName(column = "Mode")
	@CsvBindByPosition(position = 11)
	public String mode;
	
	@CsvBindByName(column = "Project")
	@CsvBindByPosition(position = 12)
	public String project;
	
	@CsvBindByName(column = "Email Sent")
	@CsvBindByPosition(position = 13)
	public String emailSent;

	public String getFullName() {
		if (Utility.isBlank(fullName)) {
			if (!Utility.isBlank(firstName) && !Utility.isBlank(lastName)) {
				fullName = firstName + " " + lastName;
			} else {
				// Handle case where either firstName or lastName might be missing
				fullName = (firstName != null ? firstName : "") + (lastName != null ? " " + lastName : "");
			}
		}
		return Utility.getCamelCase(fullName.trim());
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getFirstName() {
		if (Utility.isBlank(firstName)) {
			// Directly extract firstName from fullName, avoiding recursive call to
			// getFullName()
			if (fullName != null && fullName.contains(" ")) {
				return Utility.getCamelCase(fullName.substring(0, fullName.indexOf(" ")));
			} else {
				return Utility.getCamelCase(fullName); // Use fullName if it doesn't contain a space
			}
		}
		return Utility.getCamelCase(firstName);
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		if (Utility.isBlank(lastName)) {
			// Directly extract lastName from fullName, avoiding recursive call to
			// getFullName()
			if (fullName != null && fullName.contains(" ")) {
				return Utility.getCamelCase(fullName.substring(fullName.indexOf(" ") + 1));
			}
		}
		return Utility.getCamelCase(lastName);
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public BigDecimal getDonationAmount() {
		return donationAmount;
	}

	public void setDonationAmount(BigDecimal donationAmount) {
		this.donationAmount = donationAmount;
	}

	public String getPhone() {
		return Utility.formatPhone(phone);
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAddress1() {
		return address1;
	}

	public void setAddress1(String address1) {
		this.address1 = address1;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public int getDonorId() {
		return donorId;
	}

	public void setDonorId(int donorId) {
		this.donorId = donorId;
	}

	public String getEmailSent() {
		return emailSent;
	}

	public void setEmailSent(String emailSent) {
		this.emailSent = emailSent;
	}

	@Override
	public String toString() {
		return "Donor [donorId=" + donorId + ", fullName=" + fullName + ", firstName=" + firstName + ", lastName="
				+ lastName + ", email=" + email + ", phone=" + phone + ", address1=" + address1 + ", city=" + city
				+ ", state=" + state + ", zip=" + zip + ", donationAmount=" + donationAmount + ", mode=" + mode
				+ ", project=" + project + ", emailSent=" + emailSent + "]";
	}

	public static String getHeader() {
		return "#, Name,	First Name,	Last Name,	Email,	Phone,	address,	City,	State,	Zip,	Donation, Mode, Project, Email Sent \n";
	}
}
