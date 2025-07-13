package com.Messenger.Dto;

import java.time.Instant;

import com.Messenger.Entity.MessageEntity.Status;

import lombok.Data;

@Data
public class UserContactDTO {

	private String contactName;
	private String contactUsername;
	private String latestMessage;
	private Instant timestamp;
	private Status status;

	public UserContactDTO(String contactName, String contactUsername, String latestMessage, Instant timestamp,
			Status status) {
		super();
		this.contactName = contactName;
		this.contactUsername = contactUsername;
		this.latestMessage = latestMessage;
		this.timestamp = timestamp;
		this.status = status;
	}

}
