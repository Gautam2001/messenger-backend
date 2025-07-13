package com.Messenger.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatHistoryDTO {
	
	@NotBlank(message = "Username is required")
	@Email(message = "Invalid Email format")
	private String username; //Account Holder
	
	@NotBlank(message = "Contact Username is required")
	@Email(message = "Invalid Email format")
	private String contactUsername;
	
	private Long cursorId; //smallest messageId to fetch earlier messages

}
