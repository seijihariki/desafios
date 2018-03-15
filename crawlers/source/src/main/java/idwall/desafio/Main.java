package idwall.desafio;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import idwall.desafio.reddit.RedditFetcher;
import idwall.desafio.reddit.RedditFetcher.RedditThread;
import idwall.desafio.telegram.RedditBot;

public class Main {
    public static void main(String[] args) {
        String subredditList = null;

        // Read subreddit list from args or stdin
        if (args.length == 1) {
            subredditList = args[0];
        
            // Check if list is valid
            if (!subredditList.matches("[a-zA-Z0-9_]+(;[a-zA-Z0-9_]+)*")) {
                System.err.println("Invalid subreddit list!");
                System.exit(-1);
            }

            // Fetch threads from subreddits
            RedditFetcher fetcher = new RedditFetcher(subredditList.split(";"));

            // Print to the terminal
            for (RedditThread thread : fetcher.fetch(5000)) {
                System.out.println(thread);
                System.out.println('\n');
            }
        // If no argument was found, run telegram bot
        } else runTelegramBot();


    }

    /**
     * Registers telegram reddit bot and runs it
     */
    private static void runTelegramBot() {
        ApiContextInitializer.init();
        TelegramBotsApi telegramApi = new TelegramBotsApi();
        try {
            telegramApi.registerBot(new RedditBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
            System.err.println("Telegram bot could not be registered! Stopping...");
            System.exit(-1);
        }
    }
};
