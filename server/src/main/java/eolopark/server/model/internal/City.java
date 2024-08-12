package eolopark.server.model.internal;

public class City {

    /* Attributes */
    private String capital;
    private double latETRS89;
    private double lonETRS89;
    private String altitude;
    private String meanWind;

    /* Constructors */
    public City () {
    }

    public City (String capital, double latETRS89, double lonETRS89, String altitude, String meanWind) {
        super();
        this.capital = capital;
        this.latETRS89 = latETRS89;
        this.lonETRS89 = lonETRS89;
        this.altitude = altitude;
        this.meanWind = meanWind;
    }

    /* Getters and Setters */
    public String getCapital () {
        return capital;
    }

    public void setCapital (String capital) {
        this.capital = capital;
    }

    public double getLatETRS89 () {
        return latETRS89;
    }

    public void setLatETRS89 (double latETRS89) {
        this.latETRS89 = latETRS89;
    }

    public double getLonETRS89 () {
        return lonETRS89;
    }

    public void setLonETRS89 (double lonETRS89) {
        this.lonETRS89 = lonETRS89;
    }

    public String getAltitude () {
        return altitude;
    }

    public void setAltitude (String altitude) {
        this.altitude = altitude;
    }

    public String getMeanWind () {
        return meanWind;
    }

    public void setMeanWind (String meanWind) {
        this.meanWind = meanWind;
    }

    /* Methods */
    @Override
    public String toString () {
        return "City{" + "capital='" + capital + '\'' + ", latETRS89=" + latETRS89 + ", lonETRS89=" + lonETRS89 + ", "
                + "altitude='" + altitude + '\'' + ", meanWind='" + meanWind + '\'' + '}';
    }
}