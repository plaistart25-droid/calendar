package dev.kuklin.kworkcalendar.services.scheduler;

import dev.kuklin.kworkcalendar.library.ScheduleProcessor;
import dev.kuklin.kworkcalendar.services.NotifiedEventService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class EventNotificationNoteCleanTableScheduleProcessor implements ScheduleProcessor {
    private final NotifiedEventService notifiedEventService;
    private static final Integer HOUR = 2;
    @Override
    public void process() {
        notifiedEventService.cleanOlderThanHours(HOUR);
    }

    @Override
    public String getSchedulerName() {
        return getClass().getSimpleName();
    }
}
