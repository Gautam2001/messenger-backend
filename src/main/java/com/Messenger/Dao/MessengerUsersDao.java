package com.Messenger.Dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Messenger.Entity.MessengerUsersEntity;

@Repository
public interface MessengerUsersDao extends JpaRepository<MessengerUsersEntity, Long> {

	Optional<MessengerUsersEntity> getUserByUsername(String username);

}
