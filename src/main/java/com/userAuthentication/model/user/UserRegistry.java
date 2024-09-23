package com.userAuthentication.model.user;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "userRegistry")

@ToString
public class UserRegistry {

    @JsonProperty("sUserName")
    private String userName;

    @JsonProperty("sEmailId")
    private String emailId;

    @JsonProperty("sPassword")
    private String password;

    @JsonProperty("sFirstName")
    private String firstName;

    @JsonProperty("sLastname")
    private String lastName;

    @JsonProperty("sGender")
    private String gender;

    @JsonProperty("sDateOfBirth")
    private String dateOfBirth;

    @JsonProperty("sPhoneNumber")
    private String phoneNumber;

    @JsonProperty("bAccountActive")
    private boolean accountActive;

    @JsonProperty("dtCreatedDate")
    private Date createdDate;

    @JsonProperty("dtLastUpdatedDate")
    private Date lastUpdatedDate;

    public UserRegistry() {

    }

    public UserRegistry(String userName, String email, String firstName, String lastName, String gender, String dateOfBirth, String phoneNumber, boolean isAccountActive, Date createdDate, Date lastUpdatedDate) {
        this.userName = userName;
        this.emailId = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
        this.accountActive = isAccountActive;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;

    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isAccountActive() {
        return accountActive;
    }

    public void setAccountActive(boolean accountActive) {
        this.accountActive = accountActive;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(Date lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }
}
