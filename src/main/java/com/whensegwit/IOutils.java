package com.whensegwit;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.JSONArray;
import twitter4j.JSONObject;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class IOutils {

    static Logger LOGGER = LoggerFactory.getLogger(IOutils.class);

    public static void saveRepliesToJson(HashMap<String, Set<Long>> repliedTweetsPerAccount){
        JSONObject replies = new JSONObject();
        for(String account : repliedTweetsPerAccount.keySet()){
            replies.put(account, new JSONArray(new ArrayList<>(repliedTweetsPerAccount.get(account))));
        }

        String filenameWithPath = Paths.get(System.getProperty("user.dir"), "replies.json").toString();

        try {
            FileUtils.writeStringToFile(new File(filenameWithPath), replies.toString());
        } catch (Exception ex){
            LOGGER.error("Failed to write replies.json", ex);
        }
    }

    public static HashMap<String, Set<Long>> getSavedReplies(String[] accountsToReply){
        String rawReplies = importReplies();
        JSONObject parsedReplies = new JSONObject(rawReplies);

        HashMap<String, Set<Long>> savedReplies = new HashMap<>();
        for(String account: parsedReplies.keySet()){
            JSONArray idsArray = new JSONArray(parsedReplies.getString(account));
            HashSet<Long> idsSet = new HashSet<>();
            for(int i=0; i < idsArray.length(); i++) idsSet.add(idsArray.getLong(i));
            savedReplies.put(account, idsSet);
        }

        for(String account : accountsToReply) savedReplies.put(account, savedReplies.getOrDefault(account, new HashSet<>()));
        return savedReplies;
    }

    private static String importReplies() {
        String fileName = "replies.json";
        String line = null;
        String result = "";

        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);

            while ((line = bufferedReader.readLine()) != null) {
                result += line + "\r";
            }

            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            LOGGER.trace(
                    "Unable to open file '" +  fileName + "'");
        } catch (IOException ex) {
            LOGGER.trace("Error reading file '" + fileName + "'");

        }
        return result;
    }

    public static Properties getProperties() throws FileNotFoundException, IOException{
        InputStream input = new FileInputStream(System.getProperty("user.dir") + File.separator + "config.properties");
        Properties prop = new Properties();
        prop.load(input);
        return prop;
    }


}
