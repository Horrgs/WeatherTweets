package org.horrgs.weathertweets.wunderground;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Horrgs on 3/9/2015.
 */
public class WunderGroundAPI {
    protected static String WEBSITE = "http://api.wunderground.com/api/";
    public String url = "";
    protected URLConnection openURL(String url) {
        System.out.println("A connection is being established to: " + url);
        try {
            this.url = url;
            URLConnection urlConnection = new URL(url).openConnection();
            urlConnection.setUseCaches(false);
            urlConnection.setDefaultUseCaches(false);
            System.out.println("Connection has been established to: " + url);
            return urlConnection;
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("A connection has failed to be established to: " + url);
        }
        return null;
    }
}
