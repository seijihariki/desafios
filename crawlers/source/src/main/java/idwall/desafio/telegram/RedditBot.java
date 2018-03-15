package idwall.desafio.telegram;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import idwall.desafio.reddit.RedditFetcher;
import idwall.desafio.reddit.RedditFetcher.RedditThread;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.objects.Privacy;

public class RedditBot extends AbilityBot {
    public static String BOT_TOKEN = "394998059:AAFIsFZyi-TadwVPviXSo2P1v32dNbmu2Fc";
    public static String BOT_USERNAME = "SeijiRedditBot";

    /**
     * Telegram's AbilityBot requires explicit constructor
     */
    public RedditBot() {
        super(BOT_TOKEN, BOT_USERNAME);
    }

    /**
     * Handler for the 'NadaPraFazer' Ability
     * @param ctx Message context of the ability request
     */
    private void sendReddit(MessageContext ctx) {
        // Prepare response
        SendMessage message = new SendMessage();
        message.setChatId(ctx.chatId());

        // If received wrong number of arguments
        if (ctx.arguments().length < 1) {
            message.setText("Você precisa me dizer quais subreddits você quer!\nPor exemplo:\n/NadaPraFazer sub1;sub2... [points]");
            try {
                execute(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Parses arguments
            String subredditList = ctx.arguments()[0];

            int minPoints = 5000;

            if (ctx.arguments().length >= 2) {
                try {
                    minPoints = Integer.parseInt(ctx.arguments()[1]);
                } catch (Exception e) {
                    message.setText("'" + ctx.arguments()[1] + "' não é um inteiro válido!");
                    try {
                        execute(message);
                    } catch (Exception em) {
                        em.printStackTrace();
                    }
                }
            }

            // Checks if list of subreddits is valid
            if (!subredditList.matches("[a-zA-Z0-9_]+(;[a-zA-Z0-9_]+)*")) {
                message.setText("'" + subredditList + "' não é uma lista válida!\nO formato é:\n/NadaPraFazer sub1;sub2... [points]"); 
                try {
                    execute(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            message.setText("Vou dar uma olhada!");
            try {
                execute(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            RedditFetcher fetcher = new RedditFetcher(subredditList.split(";"));
            
            // Fetches thread list (5000 as 'Bombando' is only if over 5000 points)
            RedditThread[] threads = fetcher.fetch(minPoints);

            // If there is nothing (very likely to happen with a lower limit of 5000)
            if (threads.length == 0) {
                message.setText("Ahh... Não tem nada bombando nessas threads!");
                try {
                    execute(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                message.setText("Esses são os resultados que consegui:");
                try {
                    execute(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Tries to style and send the messages so they don't become an unintelligible block of text
            for (RedditThread thread : threads) {
                StringBuilder builder = new StringBuilder();
                builder.append("-- ");
                builder.append(thread.getPointCount());
                builder.append(" pontos - subreddit ");
                builder.append(thread.getSubreddit() + " --\n\n> ");
                builder.append(thread.getTitle() + " <");
                message.setText(builder.toString());
                try {
                    execute(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                message.setText("Comentários:\n" + thread.getCommentsLink());
                try {
                    execute(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                message.setText("Thread:\n" + thread.getThreadLink());
                try {
                    execute(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.println(thread);
                System.out.println('\n');
            }
        }
    }

    /**
     * Creates and registers the actual 'NadaPraFazer' Ability
     * @return Returns the ability
     */
    public Ability showReddit() {
        return Ability.builder().name("NadaPraFazer")
                                .info("Mostra as Threads que estão Bombando do Reddit")
                                .locality(Locality.ALL)
                                .privacy(Privacy.PUBLIC)
                                // Lambda that redirects action to function sendReddit()
                                .action(ctx -> sendReddit(ctx))
                                .build();
    }

    /**
     * Some necessary getters for Telegram Bots
     */
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    public String getBotToken() {
        return BOT_TOKEN;
    }
    
    /**
     * Required for AbilityBot
     */
    public int creatorId() {
        // Not my actual Telegram ID
        return 123456789;
    }
}