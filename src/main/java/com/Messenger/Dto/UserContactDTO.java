package com.Messenger.Dto;

import java.time.Instant;

import com.Messenger.Entity.MessageEntity.Status;

import lombok.Data;

@Data
public class UserContactDTO {

	private String contactName;
	private String contactUsername;
	private Long latestMessageId;
	private String latestMessage;
	private String latestMessageSender;
	private Instant timestamp;
	private Status status;
	private Long unread;

	public UserContactDTO(String contactName, String contactUsername, Long latestMessageId, String latestMessage,
			String latestMessageSender, Instant timestamp, Status status, Long unread) {
		super();
		this.contactName = contactName;
		this.contactUsername = contactUsername;
		this.latestMessageId = latestMessageId;
		this.latestMessage = latestMessage;
		this.latestMessageSender = latestMessageSender;
		this.timestamp = timestamp;
		this.status = status;
		this.unread = unread;
	}

}
