package idwall.desafio.reddit;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.net.MalformedURLException;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class RedditFetcher {
    final String redditURL = "https://www.reddit.com/%s/";

    URL[] subredditUrls = null;
    String[] subreddits = null;

    public RedditFetcher(String[] subreddits) {
        this.subreddits = subreddits;
        subredditUrls = new URL[subreddits.length];

        // Stores URL objects of given subreddits
        for (int subreddit = 0; subreddit < subreddits.length; subreddit++) {
            String url = String.format(redditURL, "r/" + subreddits[subreddit]);
            try {
                subredditUrls[subreddit] = new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                System.err.println("This definitely shouldn't have triggered...");
                System.err.println("This is the generated URL if something went wrong: '" + url + "'");
            }
        }
    }

    /**
     *Class representing a reddit thread
     */
    public class RedditThread {
        // Formatting string for toString()
        final String format = "| %-10d | %-15s | '%-87s' |\n| %-120s |\n| %-120s |";

        // Thread data
        private int points = 0;
        private String subreddit = null;
        private String title = null;
        private String commentsLink = null;
        private String threadLink = null;

        /**
         * Must only be instantiated by RedditFetcher
         */
        RedditThread() {}

        RedditThread(int points, String subreddit, String title, String commentsLink, String threadLink) {
            this.points = points;
            this.subreddit = subreddit;
            this.title = title;
            this.commentsLink = commentsLink;
            this.threadLink = threadLink;
        }

        /**
         * Getters for data
         */
        public int getPointCount() {
            return points;
        }
        public String getSubreddit() {
            return subreddit;
        }
        public String getTitle() {
            return title;
        }
        public String getCommentsLink() {
            return commentsLink;
        }
        public String getThreadLink() {
            return threadLink;
        }

        /**
         * Print Thread
         */
        public String toString() {
            return String.format(format,
                getPointCount(),
                getSubreddit(),
                getTitle(),
                getCommentsLink(),
                getThreadLink());
        }
    }

    /**
     * Parses html and gets info about threads
     * @param html_str String containing website source code
     * @return List of RedditThread objects of threads from the html
     */
    private List<RedditThread> parseSubreddit(String html_str) {
        List<RedditThread> threads = new ArrayList<>();

        // Using Jsoup to parse html fomr subreddit page
        Document doc = Jsoup.parse(html_str);
        Elements listing = doc.select("div.thing[data-context=listing]");

        // For each thread found by the CSS selector
        for (Element element : listing) {
            // Get title
            Elements titleElement = element.select("a.title");
            String title = titleElement.first().text();

            // Get Comments link
            Elements commentsElement = element.select("a.bylink.comments");
            String rawCommentLink = commentsElement.attr("href");
            String finalCommentLink = null;
            if (rawCommentLink.charAt(0) == '/')
                finalCommentLink = String.format(redditURL, rawCommentLink.substring(1, rawCommentLink.length() - 1));
            else finalCommentLink = rawCommentLink;

            // Get Thread link
            String rawThreadLink = titleElement.attr("href");
            String finalThreadLink = null;
            if (rawThreadLink.charAt(0) == '/')
                finalThreadLink = String.format(redditURL, rawThreadLink.substring(1, rawThreadLink.length() - 1));
            else finalThreadLink = rawThreadLink;

            // Create Thread object
            threads.add(new RedditThread(
                Integer.parseInt(element.attr("data-score")),
                element.attr("data-subreddit"),
                title,
                finalCommentLink,
                finalThreadLink
                ));
        }

        return threads;
    }

    /**
     * Fetches threads subject to filters - !Atention! This only fetches from the first page (25 results)
     * @param minPoints Minimum points required for fetching thread
     * @return Vector of RedditThread objects with not less than minPoints points
     */
    public RedditThread[] fetch() {
        return fetch(5000);
    }

    public RedditThread[] fetch(int minPoints) {
        List<RedditThread> threads = new ArrayList<>();

        // How many times to retry a failed fetch
        int retries = 3;
        int remaining = retries;

        // For each subreddit from the list
        for (int subredditIndex = 0; subredditIndex < subreddits.length; subredditIndex++) {
            URLConnection connection = null;
            try {
                // Connect to website and fetch webpage
                connection = subredditUrls[subredditIndex].openConnection();
                byte[] data = connection.getInputStream().readAllBytes();

                // Decodes webpage into string
                String webpage = new String(data);

                // Adds to final thread list if has enough points
                for (RedditThread thread : parseSubreddit(webpage))
                    if (thread.getPointCount() >= minPoints)
                        threads.add(thread);

                // Reset retry count
                remaining = retries;
            } catch (IOException e) {
                // Try again
                if (remaining > 0) {
                    remaining--;
                    subredditIndex--;
                // If no remaining attempts just skip to the next
                } else remaining = retries;

                // Can't really do anything else if it goes wrong
                e.printStackTrace();
            }
        }

        // Return list converted to array
        return threads.toArray(new RedditThread[threads.size()]);
    }
}