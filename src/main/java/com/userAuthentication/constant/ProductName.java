package com.userAuthentication.constant;

import lombok.Getter;

@Getter
public enum ProductName {

    PASSWORD_MANAGER("PASSWORD_MANAGER");

    private final String name;

    ProductName(String name) {
        this.name = name;
    }

}
