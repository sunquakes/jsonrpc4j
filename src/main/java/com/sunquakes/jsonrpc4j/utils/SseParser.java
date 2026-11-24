package com.sunquakes.jsonrpc4j.utils;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * An incremental parser for the Server-Sent Events wire format.
 *
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 3.1.0
 * @since 3.1.0
 **/
public class SseParser {

    private final StringBuilder buffer = new StringBuilder();

    private final StringBuilder data = new StringBuilder();

    private String lastEventId;

    private String id;

    private String event = "message";

    public List<Event> feed(String chunk) {
        List<Event> events = new ArrayList<>();
        if (chunk == null || chunk.isEmpty()) {
            return events;
        }
        buffer.append(chunk);
        int index;
        while ((index = buffer.indexOf("\n")) >= 0) {
            String line = buffer.substring(0, index);
            buffer.delete(0, index + 1);
            process(stripCarriageReturn(line), events);
        }
        return events;
    }

    public List<Event> end() {
        List<Event> events = new ArrayList<>();
        if (buffer.length() > 0) {
            String line = buffer.toString();
            buffer.setLength(0);
            process(stripCarriageReturn(line), events);
        }
        dispatch(events);
        return events;
    }

    private void process(String line, List<Event> events) {
        if (line.isEmpty()) {
            dispatch(events);
            return;
        }
        if (line.startsWith(":")) {
            return;
        }
        int colon = line.indexOf(':');
        String field;
        String value;
        if (colon < 0) {
            field = line;
            value = "";
        } else {
            field = line.substring(0, colon);
            value = line.substring(colon + 1);
            if (value.startsWith(" ")) {
                value = value.substring(1);
            }
        }
        switch (field) {
            case "id":
                id = value;
                lastEventId = value;
                break;
            case "event":
                event = value;
                break;
            case "data":
                data.append(value).append('\n');
                break;
            default:
                break;
        }
    }

    private void dispatch(List<Event> events) {
        if (data.length() == 0) {
            event = "message";
            return;
        }
        String payload = data.substring(0, data.length() - 1);
        data.setLength(0);
        events.add(new Event(id != null ? id : lastEventId, event, payload));
        id = null;
        event = "message";
    }

    private String stripCarriageReturn(String line) {
        return line.endsWith("\r") ? line.substring(0, line.length() - 1) : line;
    }

    @Getter
    public static class Event {

        private final String id;

        private final String event;

        private final String data;

        public Event(String id, String event, String data) {
            this.id = id;
            this.event = event;
            this.data = data;
        }
    }
}
