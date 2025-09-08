package com.Messenger.Controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Messenger.Dto.ChatHistoryDTO;
import com.Messenger.Dto.DeleteMessageDTO;
import com.Messenger.Dto.EditMessageDTO;
import com.Messenger.Dto.SendMessageDTO;
import com.Messenger.Dto.UsernameDTO;
import com.Messenger.Services.MessengerService;
import com.Messenger.Utility.CommonUtils;

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
	public ResponseEntity<HashMap<String, Object>> sendMessage(@RequestBody @Valid SendMessageDTO sendMessageDTO,
			@RequestHeader(value = "Authorization") String token) {
		CommonUtils.logMethodEntry(this);

		HashMap<String, Object> response = messengerService.sendMessage(sendMessageDTO, token);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/chat-history")
	public ResponseEntity<HashMap<String, Object>> getChatHistory(@RequestBody @Valid ChatHistoryDTO chatHistoryDTO) {
		CommonUtils.logMethodEntry(this);

		HashMap<String, Object> response = messengerService.getChatHistory(chatHistoryDTO);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/contacts")
	public ResponseEntity<HashMap<String, Object>> getContactList(@RequestBody @Valid UsernameDTO usernameDTO) {
		CommonUtils.logMethodEntry(this);

		HashMap<String, Object> response = messengerService.getContactList(usernameDTO);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/search-user")
	public ResponseEntity<HashMap<String, Object>> getUserForSearch(@RequestBody @Valid ChatHistoryDTO usernamesDTO) {
		CommonUtils.logMethodEntry(this);

		HashMap<String, Object> response = messengerService.getUserForSearch(usernamesDTO);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/message-delivered")
	public ResponseEntity<HashMap<String, Object>> updateStatusToDelivered(
			@RequestBody @Valid UsernameDTO usernameDTO) {
		CommonUtils.logMethodEntry(this);

		HashMap<String, Object> response = messengerService.updateStatusToDelivered(usernameDTO);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/message-seen")
	public ResponseEntity<HashMap<String, Object>> updateStatusToSeen(@RequestBody @Valid ChatHistoryDTO usernamesDTO) {
		CommonUtils.logMethodEntry(this);

		HashMap<String, Object> response = messengerService.updateStatusToSeen(usernamesDTO);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/message-delete")
	public ResponseEntity<HashMap<String, Object>> deleteMessage(
			@RequestBody @Valid DeleteMessageDTO deleteMessageDTO) {
		CommonUtils.logMethodEntry(this);

		HashMap<String, Object> response = messengerService.deleteMessage(deleteMessageDTO);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/message-edit")
	public ResponseEntity<HashMap<String, Object>> editMessage(@RequestBody @Valid EditMessageDTO editMessageDTO) {
		CommonUtils.logMethodEntry(this);

		HashMap<String, Object> response = messengerService.editMessage(editMessageDTO);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/ws/test")
	public String testWs() {
		return "WebSocket endpoint is mapped!";
	}

}
