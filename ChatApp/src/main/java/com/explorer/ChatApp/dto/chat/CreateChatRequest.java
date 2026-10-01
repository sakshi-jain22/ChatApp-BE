package com.explorer.ChatApp.dto.chat;

import com.explorer.ChatApp.enums.ChatType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateChatRequest {
    private ChatType type;
    private String name;
    private List<Long> participantIds;
}
