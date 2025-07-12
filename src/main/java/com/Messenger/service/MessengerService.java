package com.Messenger.service;

import java.util.HashMap;

import org.springframework.stereotype.Component;

import com.Messenger.Dto.SendMessageDTO;
import com.Messenger.Dto.UsernameDTO;

import jakarta.validation.Valid;

@Component
public interface MessengerService {

	HashMap<String, Object> joinMessengerApp(@Valid UsernameDTO usernameDTO);

	HashMap<String, Object> sendMessage(@Valid SendMessageDTO sendMessageDTO);

}
