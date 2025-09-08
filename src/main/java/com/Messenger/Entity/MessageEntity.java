package com.Messenger.Entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "messages")
public class MessageEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long messageId;

	@Column(nullable = false)
	private String sender;

	@Column(nullable = false)
	private String receiver;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status;

	@Column(name = "sent_at", nullable = false)
	private Instant sentAt;

	@Column(name = "delivered_at")
	private Instant deliveredAt;

	@Column(name = "seen_at")
	private Instant seenAt;

	@Column(name = "is_deleted")
	private Boolean isDeleted;

	@Column(name = "is_edited")
	private Boolean isEdited;

	public enum Status {
		SENT, DELIVERED, SEEN
	}

	public MessageEntity(String sender, String receiver, String content) {
		super();
		this.sender = sender;
		this.receiver = receiver;
		this.content = content;
		this.status = Status.SENT;
		this.sentAt = Instant.now();
		this.isDeleted = false;
		this.isEdited = false;
	}
}
