package com.Messenger.Utility;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.Messenger.Dao.MessengerUsersDao;
import com.Messenger.Entity.MessengerUsersEntity;

public class CommonUtils {

	private static final Logger log = LoggerFactory.getLogger(CommonUtils.class);

	public static String normalizeUsername(String username) {
		if (username == null || username.trim().isEmpty()) {
			throw new AppException("Username is required", HttpStatus.BAD_REQUEST);
		}

		return username.trim().toLowerCase();
	}

	public static void ensureUserDoesNotExist(MessengerUsersDao messengerUserDao, String username) {
		Optional<MessengerUsersEntity> userOptional = messengerUserDao.getUserByUsername(username);
		if (userOptional.isPresent()) {
			throw new AppException("User already exists. please Login.", HttpStatus.CONFLICT);
		}
	}

	public static MessengerUsersEntity fetchUserIfExists(MessengerUsersDao messengerUserDao, String username,
			String message) {
		return messengerUserDao.getUserByUsername(username)
				.orElseThrow(() -> new AppException(message, HttpStatus.BAD_REQUEST));
	}

	public static void logMethodEntry(Object caller) {
		String className = caller.getClass().getSimpleName();
		log.info("Inside {}.{}", className, getCallingMethodName());
	}

	public static void logMethodEntry(Object caller, String message) {
		String className = caller.getClass().getSimpleName();
		log.info("Inside {}.{}() → {}", className, getCallingMethodName(), message);
	}

	private static String getCallingMethodName() {
		return Thread.currentThread().getStackTrace()[3].getMethodName();
	}

	public static HashMap<String, Object> prepareResponse(HashMap<String, Object> response, String message,
			boolean success) {
		response.put("status", success ? "0" : "1");
		response.put("message", message);
		return response;
	}

	public static void logError(Exception ex) {
		StackTraceElement origin = ex.getStackTrace()[0];
		log.error("Exception in {}.{}() → {}", origin.getClassName(), origin.getMethodName(), ex.getMessage(), ex);
	}

	public static ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message,
			Object errors) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", Instant.now());
		body.put("status", status.value());
		body.put("error", status.getReasonPhrase());
		body.put("message", message);
		if (errors != null)
			body.put("details", errors);

		return new ResponseEntity<>(body, status);
	}

}
