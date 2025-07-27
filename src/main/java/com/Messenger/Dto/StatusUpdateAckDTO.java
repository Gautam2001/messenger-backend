package com.Messenger.Dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class StatusUpdateAckDTO {

	private List<Long> delivered = new ArrayList<>();
	private List<Long> seen = new ArrayList<>();

	public void addDelivered(Long id) {
		delivered.add(id);
	}

	public void addSeen(Long id) {
		seen.add(id);
	}

}
