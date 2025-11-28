package dev.kuklin.kworkcalendar.services.scheduler;

import dev.kuklin.kworkcalendar.entities.UserAuthNotification;
import dev.kuklin.kworkcalendar.library.ScheduleProcessor;
import dev.kuklin.kworkcalendar.services.UserAuthNotificationService;
import dev.kuklin.kworkcalendar.telegram.AssistantTelegramBot;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class UserAuthNotificationSchedulerProcessor implements ScheduleProcessor {
    private final UserAuthNotificationService userAuthNotificationService;
    private final AssistantTelegramBot assistantTelegramBot;
    @Override
    public void process() {
        List<UserAuthNotification> list = userAuthNotificationService
                .getPendingUserAuthNotificationAndExpired();

        for (UserAuthNotification notification: list) {
            Message returnedMessage = assistantTelegramBot.sendReturnedMessage(
                    notification.getTelegramId(), notification.getText()
            );

            if (returnedMessage != null) {
                notification.setStatus(UserAuthNotification.Status.NOTIFICATED);
                userAuthNotificationService.save(notification);
            } else {
                log.error("User auth notification error!");
            }
        }
    }


    @Override
    public String getSchedulerName() {
        return getClass().getSimpleName();
    }
}
