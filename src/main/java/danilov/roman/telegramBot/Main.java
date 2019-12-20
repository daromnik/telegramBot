package danilov.roman.telegramBot;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


public class Main {

    private static final String BOT_NAME = "BotTestRomanBot";
    private static final String BOT_TOKEN = "453595291:AAG3THcZ3gcm7KsdwmPj1zra2RuW9oXJEoc";
    public static final String PROXY_HOST = "208.113.154.14";
    public static final int PROXY_PORT = 32746;
    //private static final String PROXY_USER = "service";
    //private static final String PROXY_PASSWORD = "te1eg%40pr0xy";

    public static void main(String[] args) {
        try {

            ApiContextInitializer.init();

            // Create the TelegramBotsApi object to register your bots
            TelegramBotsApi botsApi = new TelegramBotsApi();

            DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);

            botOptions.setProxyHost(PROXY_HOST);
            botOptions.setProxyPort(PROXY_PORT);
            botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
            // Register your newly created AbilityBot
            Bot bot = new Bot(BOT_TOKEN, BOT_NAME, botOptions);

            botsApi.registerBot(bot);

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
