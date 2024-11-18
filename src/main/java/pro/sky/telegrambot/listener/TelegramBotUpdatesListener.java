package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;
import pro.sky.telegrambot.service.NotificationTaskService;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    @Autowired
    private NotificationTaskService notificationTaskService;
    @Autowired
    private NotificationTaskRepository notificationTaskRepository;



    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            if (update.message().text().equals("/start")) {
                Long chatId = Long.valueOf(String.valueOf(update.message().chat().id()));
                String messageText = "Добро пожаловать! Я ваш бот. Чем могу помочь?";
                SendMessage sendMessage = new SendMessage(chatId, messageText);
                telegramBot.execute(sendMessage);
            } else {
                Long chatId = Long.valueOf(String.valueOf(update.message().chat().id()));
                String messageText = update.message().text();
                notificationTaskService.processReminderMessage(messageText, chatId);
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }


    @Scheduled(cron = "0 0/1 * * * *")
    public void checkForNotifications() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<NotificationTask> tasks = notificationTaskRepository.findBySendTime(now);

        for (NotificationTask task : tasks) {
            SendMessage sendMessage = new SendMessage(task.getChatId(), task.getNotificationText());
            telegramBot.execute(sendMessage);
        }
    }

}
