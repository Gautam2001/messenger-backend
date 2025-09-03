package com.Messenger.Services;

import java.security.Principal;
import java.util.HashMap;

import org.springframework.stereotype.Component;

import com.Messenger.Dto.ChatHistoryDTO;
import com.Messenger.Dto.DeleteMessageDTO;
import com.Messenger.Dto.EditMessageDTO;
import com.Messenger.Dto.SendMessageDTO;
import com.Messenger.Dto.StatusUpdateDTO;
import com.Messenger.Dto.UsernameDTO;

import jakarta.validation.Valid;

@Component
public interface MessengerService {

	HashMap<String, Object> userExistsCheck(@Valid UsernameDTO usernameDTO);

	HashMap<String, Object> joinMessengerApp(@Valid UsernameDTO usernameDTO);

	HashMap<String, Object> sendMessage(@Valid SendMessageDTO sendMessageDTO);

	HashMap<String, Object> getChatHistory(@Valid ChatHistoryDTO chatHistoryDTO);

	HashMap<String, Object> getContactList(@Valid UsernameDTO usernameDTO);

	HashMap<String, Object> getUserForSearch(@Valid ChatHistoryDTO usernamesDTO);

	HashMap<String, Object> updateStatusToDelivered(@Valid UsernameDTO usernameDTO);

	HashMap<String, Object> updateStatusToSeen(@Valid ChatHistoryDTO usernamesDTO);

	void handleStatusUpdate(StatusUpdateDTO payload, Principal principal);

	HashMap<String, Object> deleteMessage(@Valid DeleteMessageDTO deleteMessageDTO);

	HashMap<String, Object> editMessage(@Valid EditMessageDTO editMessageDTO);

}
