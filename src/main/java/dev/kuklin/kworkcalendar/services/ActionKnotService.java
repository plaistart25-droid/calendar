package dev.kuklin.kworkcalendar.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.calendar.model.Event;
import dev.kuklin.kworkcalendar.ai.OpenAiIntegrationService;
import dev.kuklin.kworkcalendar.configurations.TelegramAiAssistantCalendarBotKeyComponents;
import dev.kuklin.kworkcalendar.models.ActionKnot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActionKnotService {
    private final OpenAiIntegrationService aiService;
    private final ObjectMapper objectMapper;
    private final TelegramAiAssistantCalendarBotKeyComponents components;
    private final AiMessageLogService aiMessageLogService;

    private static final String AI_EDIT_REQUEST =
            """
                    Проанализируй следующий текст и извлеки данные для события календаря. Это событие редактирования, когда пользователь меняет свое старое мероприятие на новое. Я отправлю тебе также старое событие, чтобы ты мог в нем ориентироваться. Например если пользователь попросить перенсти на час вперед и тд.
                                        
                    Текст: "%s."
                    
                    Старое событие: "%s"
                    
                    Правила:
                    1. Верни только JSON-объект без лишнего текста, обрамлений или комментариев.
                    2. Структура ответа:
                    {
                      "action": "", // EVENT_EDIT
                      "calendarEventAiResponse": {
                        "summary": "",        // краткое название события
                        "description": "",    // описание, если есть
                        "start": "",          // дата и время начала в формате yyyy-MM-dd'T'HH:mm:ss
                        "end": "",            // дата и время конца в таком же формате
                        "timezone": "",      \s
                        "result": "",         // Опиши действия которые ты сделал. Если не получилось извлечь данные - напиши об этом.
                        "isSuccessful": ""    // Boolean-ответ, true - если получилось извлечь данные, false - если не получилось извлечь название события и дату начала
                        "notifyInMinutesList": [] // Массив целых чисел. Каждый элемент — это количество минут до начала события,
                                                      // за которое нужно отправить напоминание.
                                                      // Если пользователь говорит: "напомни за X минут/часов/дней" или указывает несколько
                                                      // напоминаний ("за день и за час"), добавь все эти значения в МИНУТАХ.
                                                      // Если явного запроса на напоминания не было — верни пустой массив [].
                          }
                    }
                    3. Если в тексте нет описания — оставить пустую строку description.
                    4. Дату и время распознать из текста.
                    5. Продолжительность — такая же как была в старой задаче. Но если не указано время конца, оставь поле == null.
                    6. Если пользователь не назвал дату, считай, что сегодня %s.
                    7. Событие редактирования → action = "EVENT_EDIT". Заполни calendarEventAiResponse, НОВЫМ, А НЕ СТАРЫМ СОБЫТИЕМ.
                    8. В конце описания добавь, даже если оно пустое, добавь слова "Добавлено через Telegram-бота"
                      
                      ВЕРНИ ТОЛЬКО JSON, БЕЗ ЛИШНЕГО ТЕКСТА, ОБРАМЛЕНИЙ ИЛИ КОММЕНТАРИЕВ.!!!                  
                    """;

    private static final String AI_REQUEST = """
            Проанализируй пользовательскую команду и извлеки все данные для действий с событиями календаря.

            Текст:
            "%s"

            Твоя задача:
            Определи, что хочет сделать пользователь — добавить, удалить или изменить (редактировать) событие, и извлеки необходимые данные.

            ---

            ПРАВИЛА:

            1. Верни только корректный JSON без лишнего текста, форматирования, Markdown или комментариев.

            2. Структура ответа:
            {
              "action": "", // EVENT_ADD или EVENT_DELETE или EVENT_EDIT или ERROR
              "calendarEventAiResponse": {
                "summary": "",
                "description": "",
                "start": "",
                "end": "",
                "timezone": "",
                "result": "",          // краткий текст, какие данные распознаны и какие действия будут выполнены
                "isSuccessful": ""     // true или false
                "notifyInMinutesList": [] // Массив целых чисел. Каждый элемент — это количество минут до начала события,
                                              // за которое нужно отправить напоминание.
                                              // Если пользователь говорит: "напомни за X минут/часов/дней" или указывает несколько
                                              // напоминаний ("за день и за час"), добавь все эти значения в МИНУТАХ.
                                              // Если явного запроса на напоминания не было — верни пустой массив [].
                  }
            }

            ---

            3. Как определить **действие (`action`)**:
               
               **EVENT_DELETE** → если пользователь говорит об удалении:
               - Использует слова: "удали", "удалить", "сотри", "отмени", "отменить", "стереть", "убери", "очисти".
               - Примеры:  
                 - "Удали встречу завтра"  
                    - примерное "summary": "Удалить встречу завтра - дата (если смог дату вычислить) "
                 - "Отмени запись в барбершоп"  
                 - "Убери задачу по проекту"
               - Если это удаление, тебе в summary нужно отразить: что удалить. когда удалить. В description - дополнительные параметры полезные для идентификации удаляемой задачи.

               **EVENT_EDIT** → если пользователь говорит об изменении существующего события:
               - Использует слова: "измени", "переименуй", "перенеси", "поставь позже", "добавь описание", "сделай позже", "перенести на".
               - Примеры:  
                 - "Перенеси встречу на завтра в 10"  
                 - "Измени время собрания"  
                 - "Добавь описание к задаче"

               **EVENT_ADD** → во всех других случаях, когда пользователь говорит о новом событии:
               - Использует глаголы или формулировки вроде: "создай", "добавь", "запиши", "напомни", "встреча с", "собрание", "пойти", "надо".
               - Примеры:  
                 - "Добавь встречу завтра в 14:00"  
                 - "Запланируй звонок с клиентом"  
                 - "Создай новое напоминание позвонить маме"

                **ERROR** → во всех случаях, когда присланное сообщение кажется случайным или случайным набором символов или это не похоже на задачу:
               - Примеры:  
                 - "афыдбдэа"  
                 - "tedf" 
                 - "Эээ... Привет"  
                 - "Тест тест"   
                 
               ⚠️ Если действие не удалось однозначно определить — выбери то, что логически наиболее вероятно:
               - Если речь идёт о **новом событии** → EVENT_ADD  
               - Если упоминаются **изменения старого** → EVENT_EDIT  
               - Если звучит **удаление / отмена** → EVENT_DELETE

            ---

            4. Обработка дат и времени:
            - Если указано относительное время ("через 2 часа", "через 15 минут") — вычисли его относительно времени отправки.
             Если пользователь не назвал дату, считай, что сегодня %s, но вычисли дату по временой зоне календаря.
             Время нужно назвать в таймзоне %s, но это сообщение было отправлено в этом время: %s. Это на тот случай, если пользователь назовет не точное время, а скажет "через ... минут, часов и т.д". Это также относится к правилу 6, ты должен указать дату в таймзоне, указанной ранее.

            5. Длительность:
            - Если указано только время начала → продолжительность 30 минут.
            - Если нет времени конца → "end" = null.

            6. В description напиши детали озвученные пользователем, которые не попали в summary. Если description отсутствует — оставь пустую строку, но ВСЕГДА добавляй в конец `"Добавлено через Telegram-бота"`.
                Например:

            7. Поле "result" — кратко объясни, что именно распознано.  
            
            8. Можешь добавить подходящий под название задачи смайлик, в название задачи перед текстом. 
            Поле "isSuccessful" = true, если удалось извлечь хотя бы summary и start-время.

            ---

            ⚠️ ВЕРНИ ТОЛЬКО JSON без пояснений, без Markdown, без кодовых блоков и без комментариев.
            """;


    public ActionKnot getActionKnotOrNull(String message, String tz) {
        String aiResponse = aiService.fetchResponse(
                components.getAiKey(),
                String.format(AI_REQUEST, message, OffsetDateTime.now(), tz, OffsetDateTime.now()));
        aiResponse = extractResponse(aiResponse);
        aiMessageLogService.saveLog(message, aiResponse);
        try {
            return objectMapper.readValue(aiResponse, ActionKnot.class);
        } catch (JsonProcessingException e) {
            log.error("Не получилось распознать сообщение", e);
            return null;
        }
    }

    public ActionKnot getActionKnotForEditMessageOrNull(String message, Event oldEvent) {
        String aiResponse = aiService.fetchResponse(
                components.getAiKey(),
                String.format(AI_EDIT_REQUEST, message, getOldEventString(oldEvent), LocalDate.now()));
        aiResponse = extractResponse(aiResponse);
        aiMessageLogService.saveLog(message, aiResponse);
        try {
            return objectMapper.readValue(aiResponse, ActionKnot.class);
        } catch (JsonProcessingException e) {
            log.error("Не получилось распознать сообщение", e);
            return null;
        }
    }

    private String getOldEventString(Event event) {
        StringBuilder sb = new StringBuilder();
        sb
                .append("Название: ").append(event.getSummary()).append("\n")
                .append("Описание: ").append(event.getDescription()).append("\n")
                .append("Начало: ").append(event.getStart()).append("\n")
                .append("Конец: ").append(event.getEnd());
        return sb.toString();
    }

    private String extractResponse(String response) {
        if (response != null) {
            if (response.startsWith("```json")) {
                response = response.substring("```json".length()).trim();
            }

            if (response.endsWith("```")) {
                response = response.substring(0, response.length() - 3).trim();
            }
        }
        return response;
    }
}
