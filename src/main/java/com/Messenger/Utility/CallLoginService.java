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
import org.springframework.web.client.RestTemplate;

import com.Messenger.Dto.UsernameDTO;

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

				if (body != null && (Boolean) body.get("exists") && body.get("name") != null) {
					return Optional.of(body.get("name").toString());
				}
			}
		} catch (Exception e) {
			CommonUtils.logError(e);
			throw new AppException("Unable to validate user from Login service", HttpStatus.BAD_GATEWAY);
		}

		return Optional.empty();
	}

}
