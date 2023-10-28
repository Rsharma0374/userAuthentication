package com.chat.userAuthentication.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;

@ToString
public class Status {

    @JsonProperty("iStatus")
    private int statusCode;

    @JsonProperty("sStatus")
    private String statusValue;

    public static Builder builder() {
        return new Builder();
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setStatusValue(String statusValue) {
        this.statusValue = statusValue;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusValue() {
        return statusValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((statusValue == null) ? 0 : statusValue.hashCode());
        result = prime * result + statusCode;
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
        Status other = (Status) obj;
        if (statusValue == null) {
            if (other.statusValue != null)
                return false;
        } else if (!statusValue.equals(other.statusValue))
            return false;
        if (statusCode != other.statusCode)
            return false;
        return true;
    }

    public static class Builder {

        private Status status = new Status();

        public Status build() {
            return this.status;
        }

        public Builder statusValue(String statusValue) {
            this.status.setStatusValue(statusValue);
            return this;
        }

        public Builder statusCode(int statusCode) {
            this.status.setStatusCode(statusCode);
            return this;
        }
    }


}
