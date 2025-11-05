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
    ;

    private final String commandText;

}
