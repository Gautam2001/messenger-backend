package com.Messenger.Utility.Security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.Messenger.Dao.MessengerUsersDao;
import com.Messenger.Entity.MessengerUsersEntity;
import com.Messenger.Utility.CommonUtils;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final MessengerUsersDao messengerUsersDao;

	public CustomUserDetailsService(MessengerUsersDao messengerUsersDao) {
		super();
		this.messengerUsersDao = messengerUsersDao;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		CommonUtils.logMethodEntry(this);
		MessengerUsersEntity user = messengerUsersDao.getUserByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

		return new CustomUserDetails(user);
	}

}
