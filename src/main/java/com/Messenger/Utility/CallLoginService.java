package com.Messenger.Utility;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.Messenger.Dto.UsernameDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CallLoginService {

	@Autowired
	private RestTemplate restTemplate;

	@Value("${login.service.url}")
	private String loginServiceBaseUrl;

	@SuppressWarnings("rawtypes")
	public Optional<String> checkUserExistsInLoginService(String username) {
	    CommonUtils.logMethodEntry(this, "Calling login_microservice");

	    String loginServiceUrl = loginServiceBaseUrl + "/check-user-exists";
	    UsernameDTO usernameDTO = new UsernameDTO();
	    usernameDTO.setUsername(username);

	    try {
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);
	        HttpEntity<UsernameDTO> request = new HttpEntity<>(usernameDTO, headers);

	        ResponseEntity<Map> response = restTemplate.postForEntity(loginServiceUrl, request, Map.class);

	        if (response.getStatusCode().is2xxSuccessful()) {
	            Map body = response.getBody();
	            if (body != null) {
	                Boolean exists = (Boolean) body.get("exists");
	                String name = (String) body.get("name");

	                if (Boolean.TRUE.equals(exists) && name != null) {
	                    return Optional.of(name);
	                }
	            }
	            return Optional.empty();
	        } else {
	            throw new AppException("Unexpected response from login service: " + response.getStatusCode(),
	                                   HttpStatus.BAD_GATEWAY);
	        }

	    } catch (HttpClientErrorException e) {
	        String responseBody = e.getResponseBodyAsString();
	        try {
	            boolean exists = new ObjectMapper().readTree(responseBody).get("exists").asBoolean();
	            if (!exists) {
	                CommonUtils.logMethodEntry(this, "User not found in login service.");
	                return Optional.empty(); 
	            }
	        } catch (Exception parseEx) {
	            CommonUtils.logError(parseEx);
	            throw new AppException("Failed to parse login service response: " + parseEx.getMessage(),
	                                   HttpStatus.BAD_REQUEST);
	        }
	        throw new AppException("Login service error: " + e.getMessage(), HttpStatus.BAD_GATEWAY);
	    } catch (Exception e) {
	        CommonUtils.logError(e);
	        throw new AppException("Unable to connect to Login service", HttpStatus.BAD_GATEWAY);
	    }
	}

}
