package org.horrgs.weathertweets;

import org.horrgs.weathertweets.wunderground.WGLookup;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Horrgs on 3/9/2015.
 */
public class WeatherTweets implements Runnable {

    public static void main(String[] args) {
        File f = new File("keys.txt");
        if(!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        System.out.println("Creating thread constructor.");
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(new WeatherTweets(), 0, 1, TimeUnit.HOURS);
    }

    @Override
    public void run() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(calendar.getTime());
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int year = calendar.get(Calendar.YEAR);
        int dayinyear = calendar.get(Calendar.DAY_OF_YEAR);
        int min = calendar.get(Calendar.MINUTE);
        boolean message = new WGLookup(WGLookup.Protocol.ALERT, "NY", "Buffalo").getMessage().equals("Zero weather alerts.");
        WGLookup.Protocol protocol = WGLookup.Protocol.CONDITION;
        switch(min) {
            case 0:
                protocol = WGLookup.Protocol.CONDITION;
                break;
            case 20:
                if(!message) {
                    protocol = WGLookup.Protocol.ALERT;
                }
                break;
            case 30:
                if(message) {
                    protocol = WGLookup.Protocol.FORECAST;
                }
                break;
            case 40:
                if(!message) {
                    protocol = WGLookup.Protocol.FORECAST;
                }

        }
        WGLookup wgLookup = new WGLookup(protocol, "NY", "Buffalo");
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = null;
        try {
            Object obj = jsonParser.parse(new FileReader("secrets.json"));
            jsonObject = (JSONObject) obj;
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setDebugEnabled(true)
            .setOAuthConsumerKey(jsonObject.get("OAuthConsumerKey").toString())
            .setOAuthConsumerSecret(jsonObject.get("OAuthConsumerSecret").toString())
            .setOAuthAccessToken(jsonObject.get("OAuthAccessToken").toString())
            .setOAuthAccessTokenSecret(jsonObject.get("OAuthAccessTokenSecret").toString());
        TwitterFactory tf = new TwitterFactory(configurationBuilder.build());
        Twitter twitter = tf.getInstance();
        try {
            twitter.updateStatus("[" + hour + "/" + dayinyear + "/" + year + "]\n" +
            "Temp: " + wgLookup.getFTemp() + "F\n" +
            "Feels Like: " + wgLookup.getFeelsLike() + "F\n" +
            "Humidity: " + wgLookup.getHumidity() + "\n" +
            "Forecast: " + wgLookup.getForecast() + "\n" +
            "Precip: " + wgLookup.getPrecipitation()  + " inches\n"  +
            "Wind: " + wgLookup.getWind() + "MPH\n" +
            "Wind Gusts: "  + wgLookup.getWindGusts() + "MPH");

        } catch (TwitterException ex) {
            ex.printStackTrace();
        }

        try {
            String format = "[" + hour + "/" + dayinyear + "/" + year + "]\n";
            switch(protocol) {
                case CONDITION:
                    twitter.updateStatus(format +
                            "Temp: " + wgLookup.getFTemp() + "F\n" +
                            "Feels Like: " + wgLookup.getFeelsLike() + "F\n" +
                            "Humidity: " + wgLookup.getHumidity() + "\n" +
                            "Forecast: " + wgLookup.getForecast() + "\n" +
                            "Precip: " + wgLookup.getPrecipitation()  + " inches\n"  +
                            "Wind: " + wgLookup.getWind() + "MPH\n" +
                            "Wind Gusts: "  + wgLookup.getWindGusts() + "MPH");
                    break;
                case ALERT:
                    //TODO: this will need to check for multiple alerts.
                    break;
                case FORECAST:
                    if(wgLookup.shouldTweetSimplistic()) {
                        twitter.updateStatus(format +
                                "Outlook: " + wgLookup.getPrediction() + "\n" +
                                "Chance of Precipitation: " + wgLookup.getPrecipitationPossibility() + "%");
                    } else {
                        twitter.updateStatus(format +
                                "Temp: " + wgLookup.getAccuHighFahrenheit() + "F/" + wgLookup.getAccuLowFahrenheit() + "F\n" +
                                "Outlook: " + wgLookup.getAccuConditions() + "\n" +
                                "Chance of Precipitation: " + wgLookup.getAccuPrecipPossibility() + "%\n" +
                                "Wind: " + wgLookup.getMaxWind()  + "MPH max / " + wgLookup.getAvgWind() + "MPH avg\n" +
                                "Humidity: " + wgLookup.getAvgHumidity() + "%");
                    }
            }
        } catch (TwitterException ex) {

        }
    }
}
