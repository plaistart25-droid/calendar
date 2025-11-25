package dev.kuklin.kworkcalendar.services.scheduler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class AssistantSchedulerService {

    private final EventNotificationSchedulerProcessor eventNotificationSchedulerProcessor;
    private final EventNotificationNoteCleanTableScheduleProcessor eventNotificationNoteCleanTableScheduleProcessor;
    private final DailyNotificationSchedulerProcessor dailyNotificationSchedulerProcessor;

    @Scheduled(cron = "0 */5 * * * *")
    public void eventNotificationSchedulerProcess() {
        getInfo(eventNotificationSchedulerProcessor.getSchedulerName());
        eventNotificationSchedulerProcessor.process();
    }

    @Scheduled(cron = "0 0 */6 * * *")
    public void eventNotificationNoteCleanTableScheduleProcess() {
        getInfo(eventNotificationNoteCleanTableScheduleProcessor.getSchedulerName());
        eventNotificationNoteCleanTableScheduleProcessor.process();
    }

    // каждые 10 минут проверяем, кому пора слать "доброе утро"
    @Scheduled(cron = "0 */10 * * * *")
    public void dailyNotificationSchedulerProcessor() {
        getInfo(dailyNotificationSchedulerProcessor.getSchedulerName());
        dailyNotificationSchedulerProcessor.process();
    }

    private void getInfo(String name) {
        log.info(name + " started working");
    }
}
