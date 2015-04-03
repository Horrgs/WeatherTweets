package org.horrgs.weathertweets;

import org.horrgs.weathertweets.wunderground.WGLookup;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.*;
import java.net.ProtocolException;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Horrgs on 3/9/2015.
 */
public class WeatherTweets implements Runnable {

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
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(new WeatherTweets(), 0, 1, TimeUnit.MINUTES);
    }

    @Override
    public void run() {
        System.out.println("Running");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(calendar.getTime());
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int year = calendar.get(Calendar.YEAR);
        int dayinyear = calendar.get(Calendar.DAY_OF_YEAR);
        int min = calendar.get(Calendar.MINUTE);
        boolean message = new WGLookup(WGLookup.Protocol.ALERT, "NY", "Buffalo").getMessage().equals("Zero weather alerts.");
        System.out.println("Is the message \"Zero weather alerts.?\" " + message);
        System.out.println("The mesage is: " + new WGLookup(WGLookup.Protocol.ALERT, "NY", "Buffalo").getMessage());
        WGLookup.Protocol protocol = WGLookup.Protocol.CONDITION;
        WGLookup wgLookup = new WGLookup(protocol, "NY", "Buffalo");
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = null;
        try {
            Object obj = jsonParser.parse(new FileReader("secrets.json"));
            jsonObject = (JSONObject) obj;
            System.out.println("Parsing secrets.json ....");
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
                    System.out.println("Protocol updated to " + protocol.getProtocolType());
                    System.out.println("A weather " + protocol.getProtocolType() + "tweet is being sent.");
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
                        //TODO: this will need to check for multiple alerts.
                        System.out.println("Protocol updated to " + protocol.getProtocolType());
                        System.out.println("A weather " + protocol.getProtocolType() + "tweet is being sent.");
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
                        System.out.println("Protocol updated to " + protocol.getProtocolType());
                        System.out.println("A weather " + protocol.getProtocolType() + " tweet is being sent.");
                        if (wgLookup.shouldTweetSimplistic()) {
                            twitter.updateStatus(format +
                                    "Outlook: " + wgLookup.getPrediction() + "\n" +
                                    "Chance of Precipitation: " + wgLookup.getPrecipitationPossibility() + "%");
                        } else {
                            System.out.println("A weather " + protocol.getProtocolType() + " tweet is being sent.");
                            wgLookup.setProtocol(protocol);
                            twitter.updateStatus(format +
                                    "Temp: " + wgLookup.getAccuHighFahrenheit() + "F/" + wgLookup.getAccuLowFahrenheit() + "F\n" +
                                    "Outlook: " + wgLookup.getAccuConditions() + "\n" +
                                    "Chance of Precipitation: " + wgLookup.getAccuPrecipPossibility() + "%\n" +
                                    "Wind: " + wgLookup.getMaxWind() + "MPH max / " + wgLookup.getAvgWind() + "MPH avg\n" +
                                    "Humidity: " + wgLookup.getAvgHumidity() + "%");
                        }
                    }
                    break;
                case 40:
                    if (!message) {
                        protocol = WGLookup.Protocol.FORECAST;
                        wgLookup.setProtocol(protocol);
                        System.out.println("Protocol updated to " + protocol.getProtocolType());
                        System.out.println("A weather " + protocol.getProtocolType() + " tweet is being sent.");
                        if (wgLookup.shouldTweetSimplistic()) {
                            twitter.updateStatus(format +
                                    "Outlook: " + wgLookup.getPrediction() + "\n" +
                                    "Chance of Precipitation: " + wgLookup.getPrecipitationPossibility() + "%");
                        } else {
                            twitter.updateStatus(format +
                                    "Temp: " + wgLookup.getAccuHighFahrenheit() + "F/" + wgLookup.getAccuLowFahrenheit() + "F\n" +
                                    "Outlook: " + wgLookup.getAccuConditions() + "\n" +
                                    "Chance of Precipitation: " + wgLookup.getAccuPrecipPossibility() + "%\n" +
                                    "Wind: " + wgLookup.getMaxWind() + "MPH max / " + wgLookup.getAvgWind() + "MPH avg\n" +
                                    "Humidity: " + wgLookup.getAvgHumidity() + "%");
                        }
                    }
                    break;
            }
        } catch(TwitterException ex) {
            ex.printStackTrace();
        }
    }
}
