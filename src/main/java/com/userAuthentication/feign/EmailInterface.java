package com.userAuthentication.feign;

import com.userAuthentication.model.email.MailRequest;
import com.userAuthentication.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "email-connector-service",
        url = "${email.service.url}" // Read from application.yml
)
public interface EmailInterface {

    @GetMapping("/email-connector/welcome")
    public String welcome();

    @PostMapping("/email-connector/send-mail")
    public ResponseEntity<BaseResponse> sendEmail(
            @RequestBody MailRequest emailRequest);

    @GetMapping("/get-current-day-statistics")
    public ResponseEntity<BaseResponse> getCurrentDayStatistics();

}
