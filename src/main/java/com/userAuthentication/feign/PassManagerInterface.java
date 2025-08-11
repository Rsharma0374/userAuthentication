package com.userAuthentication.feign;

import com.userAuthentication.request.UserCreation;
import com.userAuthentication.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "pass-manager-service", url = "http://pass-manager-service:8080")
public interface PassManagerInterface {

    @PostMapping("/password-manager/create-user")
    ResponseEntity<BaseResponse> createUser(@RequestBody UserCreation userCreation);
}
