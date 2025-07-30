package com.Messenger.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EditMessageDTO {

	@NotBlank(message = "Username is required")
	@Email(message = "Invalid Email format")
	private String username;
	
	@NotNull(message = "MessageId cannot be empty.")
	private Long messageId;
	
	@NotBlank(message = "Message is required.")
	private String content;

}
