package com.Messenger.Dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Messenger.Entity.MessageEntity;

public interface MessageDao extends JpaRepository<MessageEntity, Long> {

}
