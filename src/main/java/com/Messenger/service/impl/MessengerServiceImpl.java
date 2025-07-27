package com.Messenger.service.impl;

import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import com.Messenger.Dto.StatusUpdateAckDTO;
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

		String receiverTopic = "/topic/messages/" + receiver.getUserId();
		String senderTopic = "/topic/messages/" + sender.getUserId();
		CommonUtils.logMethodEntry(this, "Sending to WebSocket topic: " + receiverTopic + " and " + senderTopic);

		messagingTemplate.convertAndSend(receiverTopic, savedMessage);
		messagingTemplate.convertAndSend(senderTopic, savedMessage);

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
		MessengerUsersEntity user = CommonUtils.fetchUserIfExists(messengerUsersDao, username,
				"User does not exist, signup first.");

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

			UserContactDTO contactDTO = new UserContactDTO(name, contactUsername, message.getMessageId(),
					message.getContent(), message.getSender(), message.getSentAt(), message.getStatus(), unread);

			contactList.add(contactDTO);
		}
		contactList.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
		
		boolean hasSelfContact = contactList.stream().anyMatch(c -> c.getContactUsername().equals(username));

		if (!hasSelfContact) {
			UserContactDTO selfContact = new UserContactDTO(user.getName(), user.getUsername(), null,
					"No conversations yet.", username, null, Status.SENT, 0L);

			contactList.add(selfContact);
		}

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
		String tokenUser = principal.getName();
		if (!tokenUser.equals(payload.getUsername())) {
			throw new AppException("Access denied: Token does not match requested user.", HttpStatus.FORBIDDEN);
		}

		MessengerUsersEntity receiver = CommonUtils.fetchUserIfExists(messengerUsersDao, requestUsername,
				"User does not exist, signup first.");

		Set<Long> delivered = payload.getDelivered() != null ? new HashSet<>(payload.getDelivered()) : Set.of();
		Set<Long> seen = payload.getSeen() != null ? new HashSet<>(payload.getSeen()) : Set.of();
		Set<Long> allIds = new HashSet<>();
		allIds.addAll(delivered);
		allIds.addAll(seen);

		Instant now = Instant.now();

		if (!delivered.isEmpty()) {
			messageDao.updateDeliveredStatusesByIds(delivered, Status.DELIVERED, now);
		}
		if (!seen.isEmpty()) {
			messageDao.updateSeenStatusesByIds(seen, Status.SEEN, now);
		}

		CommonUtils.logMethodEntry(this, "Database Updated.");

		List<Object[]> results = messageDao.findMessageIdAndSenderByIds(allIds);

		Map<String, StatusUpdateAckDTO> ackMap = new HashMap<>();
		for (Object[] row : results) {
			Long id = (Long) row[0];
			String senderUsername = (String) row[1];

			ackMap.computeIfAbsent(senderUsername, k -> new StatusUpdateAckDTO());

			if (delivered.contains(id))
				ackMap.get(senderUsername).addDelivered(id);
			if (seen.contains(id))
				ackMap.get(senderUsername).addSeen(id);
		}

		Set<String> senderUsernames = ackMap.keySet();
		List<MessengerUsersEntity> senders = messengerUsersDao.findAllByUsernameIn(senderUsernames);
		Map<String, Long> usernameToId = senders.stream()
				.collect(Collectors.toMap(MessengerUsersEntity::getUsername, MessengerUsersEntity::getUserId));

		for (Map.Entry<String, StatusUpdateAckDTO> entry : ackMap.entrySet()) {
			Long senderId = usernameToId.get(entry.getKey());
			if (senderId != null) {
				String topic = "/topic/messages/" + senderId;
				CommonUtils.logMethodEntry(this,
						"Sending message via websocket to: " + senderId + " with data: " + entry.getValue());
				messagingTemplate.convertAndSend(topic, entry.getValue());
			}
		}
	}

}
