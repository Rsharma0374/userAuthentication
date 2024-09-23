package com.userAuthentication.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;

import java.util.List;

@ToString
public class Error {

    private static final long serialVersionUID = 1L;


    @JsonProperty("sId")
    private String id;

    @JsonProperty("sFieldName")
    private String fieldName;

    @JsonProperty("sErrorType")
    private String errorType;

    @JsonProperty("sErrorCode")
    private String errorCode;

    @JsonProperty("sMessage")
    private String message;

    @JsonProperty("sLevel")
    private String level;

    @JsonProperty("oUser")
    private Object user;

    @JsonProperty("dFieldValue")
    private double fieldValue;

    @JsonProperty("aWhatsNew")
    private List<String> whatsNewList;

    public Object getUser() {
        return user;
    }

    public void setUser(Object user) {
        this.user = user;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public List<String> getWhatsNewList() {
        return whatsNewList;
    }

    public void setWhatsNewList(List<String> whatsNewList) {
        this.whatsNewList = whatsNewList;
    }

    public double getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(double fieldValue) {
        this.fieldValue = fieldValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((errorCode == null) ? 0 : errorCode.hashCode());
        result = prime * result
                + ((errorType == null) ? 0 : errorType.hashCode());
        result = prime * result
                + ((fieldName == null) ? 0 : fieldName.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((level == null) ? 0 : level.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Error other = (Error) obj;
        if (errorCode == null) {
            if (other.errorCode != null)
                return false;
        } else if (!errorCode.equals(other.errorCode))
            return false;
        if (errorType == null) {
            if (other.errorType != null)
                return false;
        } else if (!errorType.equals(other.errorType))
            return false;
        if (fieldName == null) {
            if (other.fieldName != null)
                return false;
        } else if (!fieldName.equals(other.fieldName))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (level == null) {
            if (other.level != null)
                return false;
        } else if (!level.equals(other.level))
            return false;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        return true;
    }


    public enum SEVERITY {
        CRITICAL,
        HIGH,
        MEDIUM,
        LOW
    }

    public enum ERROR_TYPE {

        SYSTEM("SYSTEM", 1000),
        BUSINESS("BUSINESS", 2000),
        DATABASE("DATABASE", 3000),
        ASSET_COST_ERROR("ASSET_COST_ERROR", 4000),
        BAD_REQUEST("BAD_REQUEST" , 400);

        private int code;
        private String value;

        private ERROR_TYPE(final String value, final int code) {
            this.code = code;
            this.value = value;
        }

        public String toValue() {
            return value;
        }

        public int toCode() {
            return code;
        }


    }

    public static class Builder {
        private Error error = new Error();

        public Error build() {
            return error;
        }

        public Builder id(String id) {
            this.error.id = id;
            return this;
        }

        public Builder fieldName(String fieldName) {

            this.error.setFieldName(fieldName);

            return this;
        }

        public Builder errorType(String errorType) {

            this.error.setErrorType(errorType);

            return this;
        }

        public Builder errorCode(String errorCode) {

            this.error.setErrorCode(errorCode);

            return this;
        }

        public Builder message(String message) {

            this.error.setMessage(message);

            return this;
        }

        public Builder level(String level) {

            this.error.setLevel(level);
            return this;
        }


        public Builder user(Object user) {

            this.error.setUser(user);
            return this;
        }

        public Builder whatsNewList (List<String> whatsNewList) {

            this.error.setWhatsNewList(whatsNewList);

            return this;
        }

        public Builder fieldValue (double fieldValue) {

            this.error.setFieldValue(fieldValue);

            return this;
        }


    }

}
