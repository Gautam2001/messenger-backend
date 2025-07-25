package com.Messenger.service.impl;

import java.security.Principal;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.Messenger.Dao.MessageDao;
import com.Messenger.Dao.MessengerUsersDao;
import com.Messenger.Dto.ChatHistoryDTO;
import com.Messenger.Dto.SendMessageDTO;
import com.Messenger.Dto.StatusUpdateDTO;
import com.Messenger.Dto.UserContactDTO;
import com.Messenger.Dto.UsernameDTO;
import com.Messenger.Entity.MessageEntity;
import com.Messenger.Entity.MessageEntity.Status;
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
	public HashMap<String, Object> userExistsCheck(@Valid UsernameDTO usernameDTO) {
		String username = CommonUtils.normalizeUsername(usernameDTO.getUsername());
		CommonUtils.logMethodEntry(this, "User Exists Check Request for: " + username);
		HashMap<String, Object> response = new HashMap<>();

		MessengerUsersEntity user = CommonUtils.fetchUserIfExists(messengerUsersDao, username,
				"User does not exist. Join Messenger.");

		response.put("userId", user.getUserId());
		return CommonUtils.prepareResponse(response, "User exists in Messenger.", true);
	}

	@Override
	public HashMap<String, Object> joinMessengerApp(@Valid UsernameDTO usernameDTO) {
		String username = CommonUtils.normalizeUsername(usernameDTO.getUsername());
		CommonUtils.logMethodEntry(this, "Join Messenger Request for: " + username);
		HashMap<String, Object> response = new HashMap<>();

		Optional<String> nameOpt = callLoginService.checkUserExistsInLoginService(username);

		if (nameOpt.isEmpty()) {
			return CommonUtils.prepareResponse(response, "User does not exist, Please Signup.", false);
		}
		String name = nameOpt.get();

		CommonUtils.ensureUserDoesNotExist(messengerUsersDao, username);

		MessengerUsersEntity user = new MessengerUsersEntity();
		user.setUsername(username);
		user.setName(name);
		MessengerUsersEntity savedUser = messengerUsersDao.save(user);
		if (savedUser == null || savedUser.getUserId() == null) {
			throw new AppException("Failed to Join. Please try again.", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return CommonUtils.prepareResponse(response, "User successfully joined Messenger.", true);
	}

	@Override
	public HashMap<String, Object> sendMessage(@Valid SendMessageDTO sendMessageDTO) {
		String senderUsername = CommonUtils.normalizeUsername(sendMessageDTO.getSender());
		CommonUtils.ValidateUserWithToken(senderUsername);

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
		messagePayload.put("messageId", savedMessage.getMessageId().toString());
		messagePayload.put("content", savedMessage.getContent());
		messagePayload.put("sentAt", savedMessage.getSentAt().toString());
		messagePayload.put("status", savedMessage.getStatus().toString());

		String receiverTopic = "/topic/messages/" + receiver.getUserId();
		String senderTopic = "/topic/messages/" + sender.getUserId();
		CommonUtils.logMethodEntry(this, "Sending to WebSocket topic: " + receiverTopic + " and " + senderTopic);

		messagingTemplate.convertAndSend(receiverTopic, messagePayload);
		messagingTemplate.convertAndSend(senderTopic, messagePayload);

		HashMap<String, Object> response = new HashMap<>();
		response.put("messageId", savedMessage.getMessageId());
		response.put("sentAt", savedMessage.getSentAt());
		return CommonUtils.prepareResponse(response, "message sent successfully via Messenger.", true);
	}

	@Override
	public HashMap<String, Object> getChatHistory(@Valid ChatHistoryDTO chatHistoryDTO) {
		String username = CommonUtils.normalizeUsername(chatHistoryDTO.getUsername());
		CommonUtils.ValidateUserWithToken(username);

		String contactUsername = CommonUtils.normalizeUsername(chatHistoryDTO.getContactUsername());
		CommonUtils.logMethodEntry(this, "Get chat history between: " + username + " and " + contactUsername);

		CommonUtils.fetchUserIfExists(messengerUsersDao, username, "User does not exist, signup first.");
		CommonUtils.fetchUserIfExists(messengerUsersDao, contactUsername,
				contactUsername + " does not have an account yet.");

		List<MessageEntity> messages = messageDao.getConversationBetweenUsers(username, contactUsername,
				chatHistoryDTO.getCursorId(), PageRequest.of(0, 25));
		Collections.reverse(messages);

		Long nextCursorId = -1L;
		if (!messages.isEmpty()) {
			nextCursorId = messages.get(0).getMessageId();
		}

		HashMap<String, Object> response = new HashMap<>();
		response.put("chatHistory", messages);
		response.put("nextCursorId", nextCursorId);
		return CommonUtils.prepareResponse(response, "Chat history fetched successfully", true);
	}

	@Override
	public HashMap<String, Object> getContactList(@Valid UsernameDTO usernameDTO) {
		String username = CommonUtils.normalizeUsername(usernameDTO.getUsername());
		CommonUtils.ValidateUserWithToken(username);

		CommonUtils.logMethodEntry(this, "Get Contact List Request for: " + username);
		CommonUtils.fetchUserIfExists(messengerUsersDao, username, "User does not exist, signup first.");

		List<MessageEntity> messages = messageDao.findLatestMessagesPerConversation(username);

		Set<String> contactUsernames = messages.stream()
				.map(m -> m.getSender().equals(username) ? m.getReceiver() : m.getSender()).collect(Collectors.toSet());

		List<MessengerUsersEntity> users = messengerUsersDao.findAllByUsernameIn(contactUsernames);
		Map<String, String> usernameToName = users.stream()
				.collect(Collectors.toMap(MessengerUsersEntity::getUsername, MessengerUsersEntity::getName));

		List<Object[]> unseenBySenderRaw = messageDao.findUnseenCountsByReceiver(username);
		Map<String, Long> senderToUnread = new HashMap<>();
		for (Object[] row : unseenBySenderRaw) {
			String sender = (String) row[0];
			Long count = (Long) row[1];
			senderToUnread.put(sender, count);
		}

		List<UserContactDTO> contactList = new ArrayList<>();
		for (MessageEntity message : messages) {
			String contactUsername = message.getSender().equals(username) ? message.getReceiver() : message.getSender();
			String name = usernameToName.getOrDefault(contactUsername, contactUsername);

			long unread = senderToUnread.getOrDefault(contactUsername, 0L);

			UserContactDTO contactDTO = new UserContactDTO(name, contactUsername, message.getContent(),
					message.getSender(), message.getSentAt(), message.getStatus(), unread);

			contactList.add(contactDTO);
		}

		contactList.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

		HashMap<String, Object> response = new HashMap<>();
		response.put("contactList", contactList);
		return CommonUtils.prepareResponse(response, "Contact List fetched successfully", true);
	}

	public HashMap<String, Object> getUserForSearch(@Valid ChatHistoryDTO searchDTO) {
		String username = CommonUtils.normalizeUsername(searchDTO.getUsername());
		CommonUtils.ValidateUserWithToken(username);

		String searchTerm = CommonUtils.normalizeUsername(searchDTO.getContactUsername());

		CommonUtils.logMethodEntry(this, "Global user search for: " + searchTerm);
		CommonUtils.fetchUserIfExists(messengerUsersDao, username, "User does not exist, signup first.");

		List<MessengerUsersEntity> matchedUsers = messengerUsersDao.findByUsernameContainingIgnoreCase(searchTerm);

		matchedUsers.removeIf(u -> u.getUsername().equalsIgnoreCase(username));

		List<Map<String, Object>> userResults = matchedUsers.stream().map(u -> {
			Map<String, Object> user = new HashMap<>();
			user.put("userId", u.getUserId());
			user.put("username", u.getUsername());
			user.put("name", u.getName());
			return user;
		}).collect(Collectors.toList());

		HashMap<String, Object> response = new HashMap<>();
		response.put("users", userResults);
		return CommonUtils.prepareResponse(response, "Matching users found.", true);
	}

	@Override
	public HashMap<String, Object> updateStatusToDelivered(@Valid UsernameDTO usernameDTO) {
		String username = CommonUtils.normalizeUsername(usernameDTO.getUsername());
		CommonUtils.ValidateUserWithToken(username);

		CommonUtils.logMethodEntry(this, "Update message status to delivered on login for user: " + username);
		CommonUtils.fetchUserIfExists(messengerUsersDao, username, "User does not exist, signup first.");

		int updatedCount = messageDao.updateStatusToDelivered(Status.DELIVERED, Instant.now(), username, Status.SENT);

		HashMap<String, Object> response = new HashMap<>();
		if (updatedCount > 0) {
			return CommonUtils.prepareResponse(response, "Message status updated successfully to Delivered.", true);
		} else {
			return CommonUtils.prepareResponse(response, "No messages needed updating.", true);
		}
	}

	@Override
	public HashMap<String, Object> updateStatusToSeen(@Valid ChatHistoryDTO usernamesDTO) {
		String accountHolder = CommonUtils.normalizeUsername(usernamesDTO.getUsername());
		CommonUtils.ValidateUserWithToken(accountHolder);

		String contactUsername = CommonUtils.normalizeUsername(usernamesDTO.getContactUsername());
		CommonUtils.logMethodEntry(this,
				"Update message status to seen between: " + accountHolder + " and " + contactUsername);

		CommonUtils.fetchUserIfExists(messengerUsersDao, accountHolder, "User does not exist, signup first.");
		CommonUtils.fetchUserIfExists(messengerUsersDao, contactUsername,
				contactUsername + " does not have an account yet.");

		int updatedCount = messageDao.updateStatusToSeen(Status.SEEN, Instant.now(), contactUsername, accountHolder);

		HashMap<String, Object> response = new HashMap<>();
		if (updatedCount > 0) {
			return CommonUtils.prepareResponse(response, "Message status updated successfully to Seen.", true);
		} else {
			return CommonUtils.prepareResponse(response, "No messages needed updating.", true);
		}
	}

	@Override
	public void handleStatusUpdate(StatusUpdateDTO payload, Principal principal) {
		CommonUtils.logMethodEntry(this, "Handling WebSocket status update");
		String requestUsername = CommonUtils.normalizeUsername(payload.getUsername());
		CommonUtils.ValidateUserWithToken(requestUsername);

		CommonUtils.fetchUserIfExists(messengerUsersDao, requestUsername, "User does not exist, signup first.");

		int totalSeenUpdated = 0;
		int totalDeliveredUpdated = 0;

		Instant payloadTimestamp;
		try {
			payloadTimestamp = Instant.parse(payload.getTimestamp());
		} catch (DateTimeParseException e) {
			payloadTimestamp = Instant.now(); // as a fallback (anyways difference will be seconds)
		}

		if (payload.getDelivered() != null && !payload.getDelivered().isEmpty()) {
			totalDeliveredUpdated = messageDao.updateDeliveredStatusesByIds(payload.getDelivered(), Status.DELIVERED,
					payloadTimestamp);
		}

		if (payload.getSeen() != null && !payload.getSeen().isEmpty()) {
			totalSeenUpdated = messageDao.updateSeenStatusesByIds(payload.getSeen(), Status.SEEN, payloadTimestamp);
		}

		CommonUtils.logMethodEntry(this,
				"Delivered updated: " + totalDeliveredUpdated + ", Seen updated: " + totalSeenUpdated);
	}

}
