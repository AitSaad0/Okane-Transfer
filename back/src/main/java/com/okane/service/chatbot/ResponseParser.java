package com.okane.service.chatbot;

import com.okane.dto.chatbot.ParserResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ResponseParser {

    public ParserResult parser(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return new ParserResult("", false, List.of(), 0);
        }

        String texte = rawResponse;
        boolean escalated = false;

        List<String> quickReplies = new ArrayList<>();
        int idx;
        while ((idx = texte.indexOf("[Suggestion:")) != -1) {
            int end = texte.indexOf("]", idx);
            if (end != -1) {
                quickReplies.add(texte.substring(idx + 12, end).trim());
                texte = texte.substring(0, idx) + texte.substring(end + 1);
            } else {
                break;
            }
        }

        return new ParserResult(texte.trim(), escalated, quickReplies, 0);
    }
}
