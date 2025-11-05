package dev.kuklin.kworkcalendar.models;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ActionKnot {
    private Action action;
    private CalendarEventAiResponse calendarEventAiResponse;

    public enum Action {
        EVENT_DELETE,
        EVENT_ADD,
        EVENT_EDIT,
        ERROR
        ;
    }
}
