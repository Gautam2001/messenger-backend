package com.Messenger.Utility.Security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.Messenger.Entity.MessengerUsersEntity;

public class CustomUserDetails implements UserDetails {

	private static final long serialVersionUID = 1L;

	private final MessengerUsersEntity messengerUsersEntity;

	public CustomUserDetails(MessengerUsersEntity messengerUsersEntity) {
		super();
		this.messengerUsersEntity = messengerUsersEntity;
	}

	@Override
	public String getUsername() {
		return messengerUsersEntity.getUsername();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return null;
	}

}
