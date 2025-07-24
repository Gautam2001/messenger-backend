package com.Messenger.Dao;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.Messenger.Entity.MessengerUsersEntity;

@Repository
public interface MessengerUsersDao extends JpaRepository<MessengerUsersEntity, Long> {

	Optional<MessengerUsersEntity> getUserByUsername(String username);

	@Query("SELECT u FROM MessengerUsersEntity u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
	List<MessengerUsersEntity> findByUsernameContainingIgnoreCase(String searchTerm);

	List<MessengerUsersEntity> findAllByUsernameIn(Set<String> contactUsernames);

}
