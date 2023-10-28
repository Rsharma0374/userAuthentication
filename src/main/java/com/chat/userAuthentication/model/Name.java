package com.chat.userAuthentication.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author rahul
 */

@ToString
@Data
public class Name implements Serializable {

    @JsonProperty("sFirstName")
    private String firstName;

    @JsonProperty("sMiddleName")
    private String middleName;

    @JsonProperty("sLastName")
    private String lastName;

    @JsonProperty("sPrefix")
    private String prefix;

    @JsonProperty("sSuffix")
    private String suffix;

}
