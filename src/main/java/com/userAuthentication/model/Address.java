package com.userAuthentication.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author rahul
 *         The basic residential details of customer. like city, pin state country,state.
 *         Line 1, Line 2, City, Pin code,  state and country are mandatory  fields.
 */

@Data
@ToString
public class Address implements Serializable {

    @JsonProperty("sLine1")
    private String addressLine1;

    @JsonProperty("sLine2")
    private String addressLine2;

    @JsonProperty("sCity")
    private String city;

    @JsonProperty("iPinCode")
    private long pin;

    @JsonProperty("sState")
    private String state;

    @JsonProperty("sCountry")
    private String country;

    @JsonProperty("sLine3")
    private String line3;

    @JsonProperty("sLine4")
    private String line4;

    @JsonProperty("sVillage")
    private String village;

    @JsonProperty("sDistrict")
    private String district;

    @JsonProperty("sLandMark")
    private String landMark;

    @JsonProperty("sArea")
    private String area;

    @JsonProperty("sLandmark")
    private  String landmark;

}
