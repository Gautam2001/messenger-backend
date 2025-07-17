package com.Messenger.Controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Messenger.Dto.ChatHistoryDTO;
import com.Messenger.Dto.SendMessageDTO;
import com.Messenger.Dto.UsernameDTO;
import com.Messenger.Utility.CommonUtils;
import com.Messenger.service.MessengerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/messenger")
public class MessengerController {

	@Autowired
	MessengerService messengerService;

	@GetMapping("/ping")
	public ResponseEntity<HashMap<String, Object>> ping() {
		CommonUtils.logMethodEntry(this);

		HashMap<String, Object> response = new HashMap<>();

		return ResponseEntity.ok(CommonUtils.prepareResponse(response, "pong", true));
	}
	
	@PostMapping("/exists")
	public ResponseEntity<HashMap<String, Object>> userExistsCheck(@RequestBody @Valid UsernameDTO usernameDTO) {
		CommonUtils.logMethodEntry(this);

		HashMap<String, Object> response = messengerService.userExistsCheck(usernameDTO);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/join")
	public ResponseEntity<HashMap<String, Object>> joinMessengerApp(@RequestBody @Valid UsernameDTO usernameDTO) {
		CommonUtils.logMethodEntry(this);

		HashMap<String, Object> response = messengerService.joinMessengerApp(usernameDTO);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/send-message")
	public ResponseEntity<HashMap<String, Object>> sendMessage(@RequestBody @Valid SendMessageDTO sendMessageDTO) {
		CommonUtils.logMethodEntry(this);

		HashMap<String, Object> response = messengerService.sendMessage(sendMessageDTO);

		return ResponseEntity.ok(response);
	}
	
	//bring in pagination in this
	@GetMapping("/chat-history")
	public ResponseEntity<HashMap<String, Object>> getChatHistory(@RequestBody @Valid ChatHistoryDTO chatHistoryDTO) {
		CommonUtils.logMethodEntry(this);

		HashMap<String, Object> response = messengerService.getChatHistory(chatHistoryDTO);

		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/contacts")
	public ResponseEntity<HashMap<String, Object>> getContactList(@RequestBody @Valid UsernameDTO usernameDTO) {
		CommonUtils.logMethodEntry(this);

		HashMap<String, Object> response = messengerService.getContactList(usernameDTO);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/ws/test")
	public String testWs() {
		return "WebSocket endpoint is mapped!";
	}

}
