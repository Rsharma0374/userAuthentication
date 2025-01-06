package com.userAuthentication.model.user;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.userAuthentication.constant.ProductName;
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

    @JsonProperty("sFullName")
    private String fullName;

    @JsonProperty("sGender")
    private String gender;

    @JsonProperty("sDateOfBirth")
    private String dateOfBirth;

    @JsonProperty("bAccountActive")
    private boolean accountActive;

    @JsonProperty("dtCreatedDate")
    private Date createdDate;

    @JsonProperty("dtLastUpdatedDate")
    private Date lastUpdatedDate;

    @JsonProperty("sProductName")
    private ProductName productName;

    public UserRegistry() {

    }

    public UserRegistry(String userName, String email, String fullName, String gender, String dateOfBirth, ProductName productName, boolean isAccountActive, Date createdDate, Date lastUpdatedDate) {
        this.userName = userName;
        this.emailId = email;
        this.fullName = fullName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.productName = productName;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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
