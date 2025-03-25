package com.userAuthentication.constant;

import lombok.Getter;

@Getter
public enum ProductName {

    PASSWORD_MANAGER("PASSWORD_MANAGER"),
    URL_SHORTENER("URL_SHORTENER");


    private final String name;

    ProductName(String name) {
        this.name = name;
    }

}
