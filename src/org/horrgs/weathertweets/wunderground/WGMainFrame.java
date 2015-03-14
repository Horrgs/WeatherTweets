package org.horrgs.weathertweets.wunderground;

/**
 * Created by Horrgs on 3/9/2015.
 */
public interface WGMainFrame {

    public double getFTemp();

    public double getWind();

    public String getHumidity();

    public double getFeelsLike();

    public String getForecast();

    public double getWindGusts();

    public String getPrecipitation();
}
