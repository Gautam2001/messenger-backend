package com.Messenger.ServiceExt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

import com.Messenger.Dto.BotRequestDTO;
import com.Messenger.Dto.MessageDTO;
import com.Messenger.Entity.MessageEntity;
import com.Messenger.Utility.AppException;
import com.Messenger.Utility.CommonUtils;

@Service
public class CallAIBotService {
	
	public static final String BOT_USERNAME = "aibot@messenger-chats.com";

	@Autowired
	private RestTemplate restTemplate;

	@Value("${aibot.service.url}")
	private String aiBotServiceBaseUrl;

	@SuppressWarnings("rawtypes")
	public String getGenericBotReply(List<MessageEntity> lastNMessages, String currentMessage) {
		CommonUtils.logMethodEntry(this, "Calling AIBot_microservice");

		String aiBotServiceUrl = aiBotServiceBaseUrl + "/generic";

		List<MessageDTO> history = lastNMessages.stream().map(msg -> {
		    String role = msg.getSender().equalsIgnoreCase(BOT_USERNAME) ? "bot" : "user";
		    return new MessageDTO(role, msg.getContent(), msg.getSentAt().toString());
		}).collect(Collectors.toCollection(ArrayList::new));


		if (!history.isEmpty()) {
		    MessageDTO firstMessage = history.get(0);

		    if (firstMessage.getContent().equals(currentMessage)) {
		        history.remove(0);
		    }
		}

		BotRequestDTO requestBody = new BotRequestDTO(history, currentMessage);

		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<BotRequestDTO> request = new HttpEntity<>(requestBody, headers);

			ResponseEntity<Map> response = restTemplate.postForEntity(aiBotServiceUrl, request, Map.class);

			if (response.getStatusCode().is2xxSuccessful()) {
				Map body = response.getBody();
				if (body != null && body.containsKey("reply")) {
					return (String) body.get("reply");
				}
				throw new AppException("Bot service response missing 'reply'", HttpStatus.BAD_GATEWAY);
			} else {
				throw new AppException("Unexpected response from bot service: " + response.getStatusCode(),
						HttpStatus.BAD_GATEWAY);
			}

		} catch (HttpClientErrorException e) {
			CommonUtils.logError(e);
			throw new AppException("Bot service returned error: " + e.getResponseBodyAsString(),
					HttpStatus.BAD_GATEWAY);
		} catch (Exception e) {
			CommonUtils.logError(e);
			throw new AppException("Unable to connect to Bot service: " + e.getMessage(), HttpStatus.BAD_GATEWAY);
		}
	}

}
