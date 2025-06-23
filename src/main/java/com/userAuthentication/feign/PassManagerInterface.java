package com.userAuthentication.feign;

import com.userAuthentication.request.UserCreation;
import com.userAuthentication.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("PASS-MANAGER-SERVICE")
public interface PassManagerInterface {

    @PostMapping("/password-manager/create-user")
    public ResponseEntity<BaseResponse> createUser(@RequestBody UserCreation userCreation);
}
