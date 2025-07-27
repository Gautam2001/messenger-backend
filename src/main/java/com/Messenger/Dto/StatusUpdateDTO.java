package com.Messenger.Dto;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StatusUpdateDTO {
	@NotBlank(message = "Username is required")
	@Email(message = "Invalid Email format")
	private String username;

	@NotBlank(message = "Delivered message list is required")
	private List<Long> delivered;

	private List<Long> seen;
}
