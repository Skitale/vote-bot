package com.bin.consts;

import java.util.Map;

public class ResourceMessages {
    private Map<String, String> messages;
    public static final String EMPTY_MESSAGE =  " empty message.";

    public ResourceMessages(Map<String, String> messages) {
        this.messages = messages;
    }

    public String getMessage(String key){
        String res = messages.get(key) != null ? messages.get(key) : MessageConst.getDefaultMessage(key);
        return res != null ? res : EMPTY_MESSAGE;
    }
}
