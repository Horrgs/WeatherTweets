package org.horrgs.weathertweets;

import org.horrgs.weathertweets.wunderground.WGLookup;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Map.Entry;

/**
 * Created by Horrgs on 3/9/2015.
 */
public class WeatherTweets implements Runnable {
    //TODO: decrease API calls
    //TODO: make it able to start at any time by using setDelay with %.
    //TODO: allow to set the delay when started
    public static void main(String[] args) throws IOException {
        File f = new File("keys.txt");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        System.out.println("Creating thread constructor.");
        int minute = Calendar.getInstance().get(Calendar.MINUTE);
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        if(args.length == 1) {
            System.out.println("Starting in " + args[0] + " min(s)....");
            scheduledExecutorService.scheduleAtFixedRate(new WeatherTweets(), Integer.parseInt(args[0]), 10, TimeUnit.MINUTES);
        } else {
            System.out.println("Starting in " + getDelay(minute) + " min(s)....");
            scheduledExecutorService.scheduleAtFixedRate(new WeatherTweets(), getDelay(minute), 10, TimeUnit.MINUTES);
        }

    }

    public static int getDelay(int minute) {
        int remainder = minute % 10;
        int a = 60 - remainder;
        return  a % 10;
    }

    @Override
    public void run() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(calendar.getTime());
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int year = calendar.get(Calendar.YEAR);
        int dayinyear = calendar.get(Calendar.DAY_OF_YEAR);
        int min = calendar.get(Calendar.MINUTE);
        String debugDate = "[" + calendar.get(Calendar.MONTH  + 1) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + year  + "]" +
                " at " + hour + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + " ";
        boolean message = new WGLookup(WGLookup.Protocol.ALERT, "NY", "Buffalo").getMessage().equals("Zero weather alerts.");;
        System.out.println(debugDate + "Is the message \"Zero weather alerts.?\" " + message);
        System.out.println(debugDate + "The mesage is: " + new WGLookup(WGLookup.Protocol.ALERT, "NY", "Buffalo").getMessage());
        WGLookup.Protocol protocol = WGLookup.Protocol.CONDITION;
        WGLookup wgLookup = new WGLookup(protocol, "NY", "Buffalo");
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = null;
        try {
            Object obj = jsonParser.parse(new FileReader("secrets.json"));
            jsonObject = (JSONObject) obj;
            System.out.println(debugDate + "Parsing secrets.json ....");
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        String format = "[" + hour + "/" + dayinyear + "/" + year + "]\n";
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setDebugEnabled(true)
                .setOAuthConsumerKey(jsonObject.get("OAuthConsumerKey").toString())
                .setOAuthConsumerSecret(jsonObject.get("OAuthConsumerSecret").toString())
                .setOAuthAccessToken(jsonObject.get("OAuthAccessToken").toString())
                .setOAuthAccessTokenSecret(jsonObject.get("OAuthAccessTokenSecret").toString());
        TwitterFactory tf = new TwitterFactory(configurationBuilder.build());
        Twitter twitter = tf.getInstance();
        try {
            switch(min) {
                case 0:
                    protocol = WGLookup.Protocol.CONDITION;
                    wgLookup.setProtocol(protocol);
                    wgLookup.recall("NY", "Buffalo");
                    System.out.println(debugDate + "Protocol updated to " + protocol.getProtocolType());
                    System.out.println(debugDate + "A weather " + protocol.getProtocolType() + " tweet is being sent.");
                    twitter.updateStatus(format +
                            "Temp: " + wgLookup.getFTemp() + "F\n" +
                            "Feels Like: " + wgLookup.getFeelsLike() + "F\n" +
                            "Humidity: " + wgLookup.getHumidity() + "\n" +
                            "Forecast: " + wgLookup.getForecast() + "\n" +
                            "Precip: " + wgLookup.getPrecipitation() + " inches\n" +
                            "Wind: " + wgLookup.getWind() + "MPH\n" +
                            "Wind Gusts: " + wgLookup.getWindGusts() + "MPH");
                    break;
                case 20:
                    if (!message) {
                        protocol = WGLookup.Protocol.ALERT;
                        wgLookup.setProtocol(protocol);
                        wgLookup.recall("NY", "Buffalo");
                        //TODO: this will need to check for multiple alerts.
                        System.out.println(debugDate + "Protocol updated to " + protocol.getProtocolType());
                        System.out.println(debugDate + "A weather " + protocol.getProtocolType() + " tweet is being sent.");
                        twitter.updateStatus("WEATHER ALERT\n" +
                                "Description: " + wgLookup.getDescription() + "\n" +
                                "Began At: " + wgLookup.dateSet() +  "\n" +
                                "Expires At: " + wgLookup.dateExpires());

                    }
                    break;
                case 30:
                    if (message) {
                        protocol = WGLookup.Protocol.FORECAST;
                        wgLookup.setProtocol(protocol);
                        wgLookup.recall("NY", "Buffalo");
                        System.out.println(debugDate + "Protocol updated to " + protocol.getProtocolType());
                        System.out.println(debugDate + "A weather " + protocol.getProtocolType() + " tweet is being sent.");
                        if (wgLookup.shouldTweetSimplistic()) {
                            wgLookup.setForecastType(WGLookup.ForecastType.TXTFORECAST);
                            //String[] tweets = splitTweet("For: " + wgLookup.getDay() + "\n" + "Outlook: " + wgLookup.getPrediction() + "\n" + "Chance of Precipitation: " + wgLookup.getPrecipitationPossibility() + "%");
                            //186 chars.
                            twitter.updateStatus("[1/2] " + format +
                                    "For: " + wgLookup.getDay() + "\n" +
                                    "Outlook: " + wgLookup.getPrediction() + "\n");
                            twitter.updateStatus("[2/2] " + format +
                                    "Chance of Precipitation: " + wgLookup.getPrecipitationPossibility() + "%");
                         /*   if(!tweets[1].equals("STATUS IS LESS THAN 134 CHARACTERS")) {
                                twitter.updateStatus("");
                            } */

                        } else {
                            wgLookup.setForecastType(WGLookup.ForecastType.SIMPLEFORECAST);
                            twitter.updateStatus("[1/3]" + format +
                                    "Temp: " + wgLookup.getAccuHighFahrenheit() + "F/" + wgLookup.getAccuLowFahrenheit() + "F\n" +
                                    "Outlook: " + wgLookup.getAccuConditions() + "\n");
                            twitter.updateStatus("[2/3] " + format +
                                    "Chance of Precipitation: " + wgLookup.getAccuPrecipPossibility() + "%\n" +
                                    "Wind: " + wgLookup.getMaxWind() + "MPH max / " + wgLookup.getAvgWind() + "MPH avg\n");
                            twitter.updateStatus("[3/3] " + format +
                                    "Humidity: " + wgLookup.getAvgHumidity() + "%");
                        }
                    }
                    break;
                case 40:
                    if (!message) {
                        protocol = WGLookup.Protocol.FORECAST;
                        wgLookup.setProtocol(protocol);
                        wgLookup.recall("NY", "Buffalo");
                        System.out.println(debugDate + "Protocol updated to " + protocol.getProtocolType());
                        System.out.println(debugDate + "A weather " + protocol.getProtocolType() + " tweet is being sent.");
                        if (wgLookup.shouldTweetSimplistic()) {
                            wgLookup.setForecastType(WGLookup.ForecastType.TXTFORECAST);
                            twitter.updateStatus("[1/2] " + format +
                                    "For: " + wgLookup.getDay() + "\n" +
                                    "Outlook: " + wgLookup.getPrediction() + "\n");
                            twitter.updateStatus("[2/2] " + format +
                                    "Chance of Precipitation: " + wgLookup.getPrecipitationPossibility() + "%");
                        } else {
                            wgLookup.setForecastType(WGLookup.ForecastType.SIMPLEFORECAST);
                            System.out.println(debugDate + "A weather " + protocol.getProtocolType() + " tweet is being sent.");
                            twitter.updateStatus("[1/3]" + format +
                                    "Temp: " + wgLookup.getAccuHighFahrenheit() + "F/" + wgLookup.getAccuLowFahrenheit() + "F\n" +
                                    "Outlook: " + wgLookup.getAccuConditions() + "\n");
                            twitter.updateStatus("[2/3] " + format +
                                    "Chance of Precipitation: " + wgLookup.getAccuPrecipPossibility() + "%\n" +
                                    "Wind: " + wgLookup.getMaxWind() + "MPH max / " + wgLookup.getAvgWind() + "MPH avg\n");
                            twitter.updateStatus("[3/3] " + format +
                                    "Humidity: " + wgLookup.getAvgHumidity() + "%");
                        }
                    }
                    break;
            }
        } catch(TwitterException ex) {
            ex.printStackTrace();
        }
    }
    //TODO: the issue is that the max period only goes up to 140.
    public String[] splitTweet(String status) {
        String[] tweets = new String[15];
        if(status.length() < 140) {
            tweets[0] = status;
            return tweets;
        } else {
            HashMap<Integer, Character> periodMap = new HashMap<>();
            for (int x = 140; x != 0; x--) {
                if (status.charAt(x) == '.' || status.charAt(x) == '?' || status.charAt(x) == ',') {
                    periodMap.put(x, status.charAt(x));
                }
            }
            Entry<Integer, Character> maxPeriod = null;
            for(Entry<Integer, Character> entry : periodMap.entrySet()) {
                if(maxPeriod == null || entry.getValue() > maxPeriod.getValue()) {
                    maxPeriod = entry;
                }
            }
            if(maxPeriod == null || maxPeriod.getKey() == null || maxPeriod.getValue() == null) {
                tweets[10] = "Error 20: the key or value is null.";
            }
            String tweetOne = "";
            for(int x = 0; x < maxPeriod.getKey(); x++) {
                tweetOne = tweetOne + status.charAt(x + 1);
            }
            tweets[1] = tweetOne;
            String tweetTwo = "";
            for(int x = tweetOne.length() + 1; x < status.length(); x++) {
                tweetTwo = tweetTwo + status.charAt(x);
            }
            tweets[2] = tweetTwo;
            return tweets;
        }
    }
  /*  public String[] splitTweet(String status) {
        String[] tweets = new String[15];
        if(status.length() < 140) {
            tweets[0] = status;
            return tweets;
        } else {
            HashMap<Integer, Character> periodMap = new HashMap<>();
            for (int x = 140; x != 0; x--) {
                if (status.charAt(x) == '.' || status.charAt(x) == '?' || status.charAt(x) == ',') {
                    periodMap.put(x, status.charAt(x));
                }
            }
            Entry<Integer, Character> maxPeriod = null;
            for(Entry<Integer, Character> entry : periodMap.entrySet()) {
                if(maxPeriod == null || entry.getValue() > maxPeriod.getValue()) {
                    maxPeriod = entry;
                }
            }
            if(maxPeriod == null || maxPeriod.getKey() == null || maxPeriod.getValue() == null) {
                tweets[10] = "Error 20: the key or value is null.";
            }
            String tweetOne = "";
            for(int x = 0; x < maxPeriod.getKey(); x++) {
                tweetOne = tweetOne + status.charAt(x);
            }
            tweets[1] = tweetOne;
            String tweetTwo = "";
            for(int x = tweetOne.length() + 1; x < status.length(); x++) {
                tweetTwo = tweetTwo + status.charAt(x);
            }
            tweets[2] = tweetTwo;
            return tweets;
        }
    }

    public int getMaxPoint(String string, int multiply) {
        HashMap<Integer, Character> periodMap = new HashMap<>();
        for (int x = 140 * multiply; x != x / multiply; x--) {
            System.out.println(x);
            if (string.charAt(x) == '.') {
                periodMap.put(x, string.charAt(x));
            }
        }
        Entry<Integer, Character> maxPeriod = null;
        for (Entry<Integer, Character> entry : periodMap.entrySet()) {
            if (maxPeriod == null || entry.getValue() > maxPeriod.getValue()) {
                maxPeriod = entry;
            }
        }
        if (maxPeriod == null || maxPeriod.getKey() == null || maxPeriod.getValue() == null) {
            return 1000;
        }
        return maxPeriod.getKey();
    }                  */
}