package com.Messenger.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendMessageDTO {

	@NotBlank(message = "Sender is required")
	@Email(message = "Invalid Email format")
	private String sender;

	@NotBlank(message = "Receiver is required")
	@Email(message = "Invalid Email format")
	private String receiver;

	@NotBlank(message = "Message is required")
	private String content;

}
