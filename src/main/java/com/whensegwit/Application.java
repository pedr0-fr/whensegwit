package com.whensegwit;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class Application {

    static String[] accountsToReply;
    static long considerLastMinues;

    static String OAuthConsumerKey;
    static String OAuthConsumerSecret;
    static String OAuthAccessToken;
    static String OAuthAccessTokenSecret;

    static String botHandle;

    static Logger LOGGER = LoggerFactory.getLogger(Application.class);

    static Twitter twitter;
    static HashMap<String, Set<Long>> repliedTweetsPerAccount = new HashMap<>();


    private static void setUp(){
        try {
            Properties properties = IOutils.getProperties();
            botHandle = properties.getProperty("botHandle", "WSegwit");
            OAuthConsumerKey = properties.getProperty("OAuthConsumerKey");
            OAuthConsumerSecret = properties.getProperty("OAuthConsumerSecret");
            OAuthAccessToken = properties.getProperty("OAuthAccessToken");
            OAuthAccessTokenSecret = properties.getProperty("OAuthAccessTokenSecret");
            setUpTwitter();

            considerLastMinues = Long.valueOf(properties.getProperty("considerLastMinues"));
            considerLastMinues = Math.max(1, considerLastMinues);

            accountsToReply = properties.getProperty("accountsToReply").replace(" ", "").split(",");
            Arrays.asList(accountsToReply).forEach(s -> repliedTweetsPerAccount.put(s, new HashSet<>()));
            repliedTweetsPerAccount = IOutils.getSavedReplies(accountsToReply);
        } catch (Exception ex){
            LOGGER.error("Failed to initialize. Exiting...", ex);
            System.exit(1);
        }

    }

    private static void setUpTwitter() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(OAuthConsumerKey)
                .setOAuthConsumerSecret(OAuthConsumerSecret)
                .setOAuthAccessToken(OAuthAccessToken)
                .setOAuthAccessTokenSecret(OAuthAccessTokenSecret);

        TwitterFactory tf = new TwitterFactory(cb.build());
        twitter = tf.getInstance();
    }

    public static void main(String[] args) throws Exception{
        setUp();
        LOGGER.info("Successful initialization. Starting to check for possibles replies within the last "
                + considerLastMinues + " minutes.");
        while (true) {
            try{
                for (String account : accountsToReply) {
                    replyToAccount(account);
                }
                IOutils.saveRepliesToJson(repliedTweetsPerAccount);
                LOGGER.info("Finished verifying tweets. Pausing for 60 seconds...");
                Thread.sleep(60 * 1000);
            } catch(Exception ex){
                LOGGER.warn("Unexpected error...", ex);
            }
        }
    }

    private static void replyToAccount(String account) {

        List<Status> tweets;
        try {
            tweets = getUserTweets(account);
        } catch (Exception ex){
            LOGGER.warn("Wasn't able to get tweets from " + account, ex);
            return;
        }

        for(Status tweet : tweets) {
            long id = tweet.getId();
            if(repliedTweetsPerAccount.get(account).contains(id)) continue;

            StatusUpdate stat = new StatusUpdate("@" + account + "\n When SegWit?");
            stat.setInReplyToStatusId(id);
            try{
                //Status newStatus = twitter.updateStatus(stat);
                repliedTweetsPerAccount.get(account).add(id);
                LOGGER.info("Tweeted reply! original tweet=https://twitter.com/" + account + "/" + id +
                        ", new tweet=https://twitter.com/" + botHandle + "/" + 1); //newStatus.getId()
            } catch(Exception ex){
                LOGGER.warn("Failed to reply to tweet. account=" + account + ", id=" + id, ex);
            }
        }
    }

    private static List<Status> getUserTweets(String account) throws Exception{
        List<Status> tweets = new ArrayList<>();
        Paging page = new Paging(1);
        Instant lastTweet = Instant.now();
        while (lastTweet.plus(considerLastMinues, ChronoUnit.MINUTES).isAfter(Instant.now())) {
            List<Status> newTweets = twitter.getUserTimeline(account, page);
            tweets.addAll(newTweets);
            lastTweet = newTweets.size() > 0 ? newTweets.get(newTweets.size()-1).getCreatedAt().toInstant() : Instant.EPOCH;
            page = new Paging(page.getPage()+1);
        }

        tweets = tweets.stream().filter(
                t -> t.getCreatedAt().toInstant().plus(considerLastMinues, ChronoUnit.MINUTES).isAfter(Instant.now())
        ).collect(Collectors.toList());
        return tweets;
    }

}
