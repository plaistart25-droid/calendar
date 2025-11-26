package dev.kuklin.kworkcalendar.library.tgutils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Command {
    ASSISTANT_START("/start"),
    ASSISTANT_VOICE("voicenotcommand"),
    ASSISTANT_TODAY("/today"),
    ASSISTANT_SET_CALENDARID("/set"),
    ASSISTANT_DELETE("/delete"),
    ASSISTANT_HELP("/help"),
    ASSISTANT_AUTH("/auth"),
    ASSISTANT_AUTH_STATUS("/auth_status"),
    ASSISTANT_CHOOSE_CALENDAR("/choosecalendar"),
    ASSISTANT_TABLE("/table"),
    ASSISTANT_TZ("/tz"),
    ASSISTANT_DAILY_TIME("/notify_time"),
    ASSISTANT_GET_CALENDAR("/getcalendar"),
    ASSISTANT_SETTINGS("/settings"),
    ASSISTANT_CLOSE("/close");

    private final String commandText;

}
