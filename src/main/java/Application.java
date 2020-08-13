import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Application {

     static String OAuthConsumerKey = "**";
     static String OAuthConsumerSecret = "**";
     static String OAuthAccessToken = "**";
     static String OAuthAccessTokenSecret = "**";

    static Twitter twitter;
    static HashMap<String, Set<Long>> repliedTweetsPerAccount;


    public static void main(String[] args){
        setUp();
        try{
            while (true) {
                for (String account : repliedTweetsPerAccount.keySet()) {

                    replyToAccount(account);
                    Thread.sleep(60 * 1000);
                }
            }
        } catch(Exception ex){}


    }

    private static void replyToAccount(String account) {
        ResponseList<Status> tweets = twitter.getUserTimeline(account);

        for(Status tweet : tweets) {
            long id = tweet.getId();

            if( tweet.getCreatedAt().toInstant().plus(1, ChronoUnit.HOURS).isAfter(Instant.now()) ||
                    !repliedTweetsPerAccount.get(account).contains(id)){
                // too old or already replied
                return;
            }


            StatusUpdate stat = new StatusUpdate("@" + account + "\n When segwit?");
            stat.setInReplyToStatusId(id);

            twitter.updateStatus(stat);
        }
    }

    private static void setUp() {
        ConfigurationBuilder cb = new ConfigurationBuilder();

        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(OAuthConsumerKey)
                .setOAuthConsumerSecret(OAuthConsumerSecret)
                .setOAuthAccessToken(OAuthAccessToken)
                .setOAuthAccessTokenSecret(OAuthAccessTokenSecret);

        repliedTweetsPerAccount.put("blockchain", new HashSet<Long>());
        TwitterFactory tf = new TwitterFactory(cb.build());
        twitter = tf.getInstance();
    }
}
