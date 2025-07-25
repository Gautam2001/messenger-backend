package com.Messenger.Controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import com.Messenger.Dto.StatusUpdateDTO;
import com.Messenger.Utility.CommonUtils;
import com.Messenger.service.MessengerService;

@Controller
@MessageMapping("/webSocket")
public class WebSocketController {

	@Autowired
	private MessengerService messengerService;

	@MessageMapping("/status-update")
	public void handleStatusUpdate(StatusUpdateDTO payload, Principal principal) {
		CommonUtils.logMethodEntry(this);

		messengerService.handleStatusUpdate(payload, principal);
	}

}
