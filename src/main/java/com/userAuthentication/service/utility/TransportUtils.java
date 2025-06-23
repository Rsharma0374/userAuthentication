package com.userAuthentication.service.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.userAuthentication.feign.EmailInterface;
import com.userAuthentication.feign.PassManagerInterface;
import com.userAuthentication.model.email.MailRequest;
import com.userAuthentication.request.UserCreation;
import com.userAuthentication.response.BaseResponse;
import com.userAuthentication.utility.ResponseUtility;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

@Component
public class TransportUtils {
    private static final Logger logger = LoggerFactory.getLogger(TransportUtils.class);


    @Autowired
    EmailInterface emailInterface;

    @Autowired
    PassManagerInterface passManagerInterface;

    public void createUser(UserCreation userCreation) {
        try {
            ResponseEntity<BaseResponse> responseEntity = passManagerInterface.createUser(userCreation);
            logger.info(null != responseEntity && null != responseEntity.getBody() ? responseEntity.getBody().toString() : "Response is null");
        } catch (Exception e) {
            logger.error("Exception cause due to ", e);
        }
    }

    public BaseResponse sendEmail(MailRequest mailRequest) {
        try {
            ResponseEntity<BaseResponse> responseEntity = emailInterface.sendEmail(mailRequest);
            return responseEntity.getBody();
        } catch (Exception e) {
            logger.error("Exception cause due to ", e);
            return null;
        }

    }

    public static Object postJsonRequest(Object jsonRequest, String url, Class<?> name) throws Exception {

        logger.info("postJsonRequest  started for url {} with input request {} ", url, jsonRequest);

        try {

            if (StringUtils.isBlank(url)) {


                logger.error("postJsonRequest config not setup for service fetching response for {}", name.getSimpleName());

                throw new Exception(String.format("postJsonRequest config not setup for service fetching response for {} ", name.getSimpleName()));
            }
            String inputJson = ResponseUtility.ObjectToString(jsonRequest);

            // Create an instance of HttpClient
            HttpClient httpClient = HttpClient.newHttpClient();

            // Create a POST request with JSON payload
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(inputJson))
                    .build();

            // Send the POST request and receive the response
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

            if (name == BaseResponse.class) {
                return ResponseUtility.StringToObject(response.body(), name);
            }
            String payLoadString = "";
            JSONObject jsonObject = new JSONObject(response.body());
            if (jsonObject.has("oBody")) {
                JSONObject oBody = jsonObject.getJSONObject("oBody");
                if (null != oBody && oBody.has("payLoad")) {
                    JSONObject payLoad = oBody.getJSONObject("payLoad");
                    payLoadString = payLoad.toString();
                    logger.info(payLoadString);

                }
            }
            return ResponseUtility.StringToObject(payLoadString, name);

        } catch (JsonProcessingException e) {

            logger.error("Error occurred during postJsonRequest JsonProcessingException :: " + e);

            throw new Exception(e);

        } catch (Exception e) {

            logger.error("Error occurred during postJsonRequest Exception :: " + e);

            throw new Exception(e);
        }
    }

}
