package com.Messenger.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.Messenger.Dao.MessageDao;
import com.Messenger.Dao.MessengerUsersDao;
import com.Messenger.Dto.SendMessageDTO;
import com.Messenger.Dto.UsernameDTO;
import com.Messenger.Entity.MessageEntity;
import com.Messenger.Entity.MessengerUsersEntity;
import com.Messenger.Utility.AppException;
import com.Messenger.Utility.CallLoginService;
import com.Messenger.Utility.CommonUtils;
import com.Messenger.service.MessengerService;

import jakarta.validation.Valid;

@Service
public class MessengerServiceImpl implements MessengerService {

	@Autowired
	CallLoginService callLoginService;

	@Autowired
	MessengerUsersDao messengerUsersDao;

	@Autowired
	MessageDao messageDao;

	@Autowired
	SimpMessagingTemplate messagingTemplate;

	@Override
	public HashMap<String, Object> joinMessengerApp(@Valid UsernameDTO usernameDTO) {
		String username = CommonUtils.normalizeUsername(usernameDTO.getUsername());
		CommonUtils.logMethodEntry(this, "Join Messenger Request for: " + username);

		String name = callLoginService.checkUserExistsInLoginService(username).orElseThrow(
				() -> new AppException("Unable to validate user from Login service", HttpStatus.BAD_GATEWAY));

		CommonUtils.ensureUserDoesNotExist(messengerUsersDao, username);

		MessengerUsersEntity user = new MessengerUsersEntity();
		user.setUsername(username);
		user.setName(name);
		MessengerUsersEntity savedUser = messengerUsersDao.save(user);
		if (savedUser == null || savedUser.getUserId() == null) {
			throw new AppException("Failed to Join. Please try again.", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		HashMap<String, Object> response = new HashMap<>();
		return CommonUtils.prepareResponse(response, "User successfully joined Messenger", true);
	}

	@Override
	public HashMap<String, Object> sendMessage(@Valid SendMessageDTO sendMessageDTO) {
		String senderUsername = CommonUtils.normalizeUsername(sendMessageDTO.getSender());
		String receiverUsername = CommonUtils.normalizeUsername(sendMessageDTO.getReceiver());
		CommonUtils.logMethodEntry(this, "Send Message Request from: " + senderUsername + " to: " + receiverUsername);

		MessengerUsersEntity sender = CommonUtils.fetchUserIfExists(messengerUsersDao, senderUsername,
				"User does not exist, signup first.");
		MessengerUsersEntity receiver = CommonUtils.fetchUserIfExists(messengerUsersDao, receiverUsername,
				receiverUsername + " does not have an account yet.");

		MessageEntity message = new MessageEntity(senderUsername, receiverUsername, sendMessageDTO.getContent());
		MessageEntity savedMessage = messageDao.save(message);
		if (savedMessage == null || savedMessage.getMessageId() == null) {
			throw new AppException("Failed to save message in Database. Please try again.",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		CommonUtils.logMethodEntry(this, "Message saved to Database.");

		Map<String, String> messagePayload = new HashMap<>();
		messagePayload.put("sender", senderUsername);
		messagePayload.put("senderName", sender.getName());
		messagePayload.put("receiver", receiverUsername);
		messagePayload.put("content", savedMessage.getContent());

		String topic = "/topic/messages/" + receiver.getUserId();
		CommonUtils.logMethodEntry(this, "Sending to WebSocket topic: " + topic);
		
		messagingTemplate.convertAndSend(topic, messagePayload);

		HashMap<String, Object> response = new HashMap<>();
		response.put("messageId", savedMessage.getMessageId());
		response.put("sentAt", savedMessage.getSentAt());
		return CommonUtils.prepareResponse(response, "message sent successfully via Messenger.", true);
	}

}
