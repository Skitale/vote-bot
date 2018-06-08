package com.bin.consts;

import java.util.HashMap;
import java.util.Map;

public class ResourceMessages {
    private Map<String, String> messages;
    public static final String EMPTY_MESSAGE = " empty message";

    public ResourceMessages(Map<String, String> messages) {
        this.messages = messages;
    }

    public String getMessage(String key, String... param) {
        String res = messages.get(key) != null ? messages.get(key) : MessageConst.getDefaultMessage(key);
        if (res == null) {
            return EMPTY_MESSAGE;
        }
        return resolveParametersForMessages(res, param);
    }

    private String resolveParametersForMessages(String msg, String... param) {
        Map<String, String> mapOfReplacing = new HashMap<>();
        for (int i = 0; i < param.length; i++) {
            String item = "{" + (i + 1) + "}";
            mapOfReplacing.put(item, param[i]);
        }
        return replacingParamInString(msg, mapOfReplacing);
    }

    private String replacingParamInString(String msg, Map<String, String> map) {
        for (Map.Entry<String, String> e : map.entrySet()) {
            msg = msg.replace(e.getKey(), e.getValue());
        }
        return msg;
    }
}
