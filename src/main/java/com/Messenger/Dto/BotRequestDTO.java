package com.Messenger.Dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BotRequestDTO {
	private List<MessageDTO> history;
	private String message;
}
