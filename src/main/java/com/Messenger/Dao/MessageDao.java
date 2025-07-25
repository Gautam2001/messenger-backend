package com.Messenger.Dao;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Messenger.Entity.MessageEntity;
import com.Messenger.Entity.MessageEntity.Status;

import jakarta.transaction.Transactional;

public interface MessageDao extends JpaRepository<MessageEntity, Long> {

	@Query(value = "SELECT DISTINCT ON (LEAST(m.sender, m.receiver), GREATEST(m.sender, m.receiver)) * FROM messages m WHERE m.sender = :username OR receiver = :username ORDER BY LEAST(m.sender, m.receiver), GREATEST(m.sender, m.receiver), m.sent_at DESC", nativeQuery = true)
	List<MessageEntity> findLatestMessagesPerConversation(@Param("username") String username);

	@Query("SELECT m FROM MessageEntity m WHERE ((m.sender = :username AND m.receiver = :contactUsername) "
			+ "OR (m.sender = :contactUsername AND m.receiver = :username)) AND (:cursorId IS NULL "
			+ "OR m.messageId < :cursorId) ORDER BY m.sentAt DESC")
	List<MessageEntity> getConversationBetweenUsers(@Param("username") String username,
			@Param("contactUsername") String contactUsername, @Param("cursorId") Long cursorId, Pageable pageable);

	@Transactional
	@Modifying
	@Query("UPDATE MessageEntity m SET m.status = :delivered, m.deliveredAt = :now WHERE m.receiver = :username AND m.status = :sent")
	int updateStatusToDelivered(@Param("delivered") Status delivered, @Param("now") Instant now,
			@Param("username") String username, @Param("sent") Status sent);

	@Transactional
	@Modifying
	@Query("UPDATE MessageEntity m SET m.status = :seen, m.seenAt = :now WHERE m.sender = :sender AND m.receiver = :receiver AND m.status != :seen")
	int updateStatusToSeen(@Param("seen") Status seen, @Param("now") Instant now, @Param("sender") String sender,
			@Param("receiver") String receiver);

	@Query("SELECT m.sender, COUNT(m) FROM MessageEntity m WHERE m.receiver = :receiver AND m.status IN ('SENT', 'DELIVERED') GROUP BY m.sender")
	List<Object[]> findUnseenCountsByReceiver(@Param("receiver") String receiver);

	@Modifying
	@Transactional
	@Query("UPDATE MessageEntity m SET m.status = :status, m.deliveredAt = :updatedAt WHERE m.messageId IN :ids")
	int updateDeliveredStatusesByIds(@Param("ids") List<Long> ids, @Param("status") MessageEntity.Status status,
			@Param("updatedAt") Instant updatedAt);

	@Modifying
	@Transactional
	@Query("UPDATE MessageEntity m SET m.status = :status, m.seenAt = :updatedAt WHERE m.messageId IN :ids")
	int updateSeenStatusesByIds(@Param("ids") List<Long> ids, @Param("status") MessageEntity.Status status,
			@Param("updatedAt") Instant updatedAt);

}
