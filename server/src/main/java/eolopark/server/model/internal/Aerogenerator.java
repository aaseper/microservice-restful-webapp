package eolopark.server.model.internal;

import jakarta.persistence.*;

@Entity (name = "aerogenerator")
public class Aerogenerator {

    /* Attributes */
    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    private Long id;
    private String aerogeneratorId;
    private double aerogeneratorLatitude;
    private double aerogeneratorLongitude;
    private int bladeLength;
    private int height;
    private int aerogeneratorPower;
    @ManyToOne
    private Eolopark eolopark;

    /* Constructors */
    public Aerogenerator () {
    }

    public Aerogenerator (String aerogeneratorId, double aerogeneratorLatitude, double aerogeneratorLongitude,
                          int bladeLength, int height, int aerogeneratorPower) {
        super();
        this.aerogeneratorId = aerogeneratorId;
        this.aerogeneratorLatitude = aerogeneratorLatitude;
        this.aerogeneratorLongitude = aerogeneratorLongitude;
        this.bladeLength = bladeLength;
        this.height = height;
        this.aerogeneratorPower = aerogeneratorPower;
    }

    /* Getters and Setters */
    public Long getId () {
        return id;
    }

    public void setId (Long id) {
        this.id = id;
    }

    public String getAerogeneratorId () {
        return aerogeneratorId;
    }

    public void setAerogeneratorId (String aerogeneratorId) {
        this.aerogeneratorId = aerogeneratorId;
    }

    public double getAerogeneratorLatitude () {
        return aerogeneratorLatitude;
    }

    public void setAerogeneratorLatitude (double aerogeneratorLatitude) {
        this.aerogeneratorLatitude = aerogeneratorLatitude;
    }

    public double getAerogeneratorLongitude () {
        return aerogeneratorLongitude;
    }

    public void setAerogeneratorLongitude (double aerogeneratorLongitude) {
        this.aerogeneratorLongitude = aerogeneratorLongitude;
    }

    public int getBladeLength () {
        return bladeLength;
    }

    public void setBladeLength (int bladeLength) {
        this.bladeLength = bladeLength;
    }

    public int getHeight () {
        return height;
    }

    public void setHeight (int height) {
        this.height = height;
    }

    public int getAerogeneratorPower () {
        return aerogeneratorPower;
    }

    public void setAerogeneratorPower (int aerogeneratorPower) {
        this.aerogeneratorPower = aerogeneratorPower;
    }

    public Eolopark getEolopark () {
        return eolopark;
    }

    public void setEolopark (Eolopark eolopark) {
        this.eolopark = eolopark;
    }

    /* Methods */
    @Override
    public String toString () {
        return "Aerogenerator " + aerogeneratorId + ", latitude: " + aerogeneratorLatitude + ", longitude: " + aerogeneratorLongitude + ", blade length: " + bladeLength + ", height: " + height + ", power: " + aerogeneratorPower + "W";
    }
}