package pro.sky.telegrambot.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.listener.TelegramBotUpdatesListener;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class NotificationTaskService {

    private final NotificationTaskRepository notificationTaskRepository;

    @Autowired
    public NotificationTaskService(NotificationTaskRepository notificationTaskRepository, TelegramBotUpdatesListener telegramBotUpdatesListener) {
        this.notificationTaskRepository = notificationTaskRepository;

    }

    public NotificationTask saveNotificationTask(NotificationTask task) {
        return notificationTaskRepository.save(task);
    }

    public void deleteTask(Long id) {
        notificationTaskRepository.deleteById(id);
    }

    public void processReminderMessage(String messageText, Long chatId) {
        String regex = "(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(messageText);

        if (matcher.matches()) {
            String dateTimeString = matcher.group(1);
            String reminderText = matcher.group(3);
            LocalDateTime sendTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

            NotificationTask notificationTask = new NotificationTask();
            notificationTask.setChatId(chatId);
            notificationTask.setNotificationText(reminderText);
            notificationTask.setSendTime(sendTime);

            notificationTaskRepository.save(notificationTask);

            System.out.println(dateTimeString + " " + "записано");
        } else {
            System.out.println("Неверный формат сообщения. Пожалуйста, используйте формат: 'дд.мм.гггг чч:мм Текст'.");
        }
    }



}