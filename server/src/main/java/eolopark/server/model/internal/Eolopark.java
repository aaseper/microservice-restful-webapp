package eolopark.server.model.internal;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity (name = "eolopark")
public class Eolopark {

    /* Attributes */
    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    private Long id;
    @Column (unique = true)
    private String name;
    private String city;
    private double latitude;
    private double longitude;
    private int area;
    private String terrainType;
    @OneToMany (mappedBy = "eolopark", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Aerogenerator> aerogenerators = new ArrayList<>();
    @OneToOne (cascade = CascadeType.ALL)
    private Substation substation;
    @ManyToOne
    private User user;

    /* Constructors */
    public Eolopark () {
    }

    public Eolopark (String name, String city, double latitude, double longitude, int area, String terrainType,
                     Substation substation) {
        super();
        this.name = name;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.area = area;
        this.terrainType = terrainType;
        this.substation = substation;
    }

    public Eolopark (Eolopark eolopark, Substation substation) {
        super();
        this.name = eolopark.getName();
        this.city = eolopark.getCity();
        this.latitude = eolopark.getLatitude();
        this.longitude = eolopark.getLongitude();
        this.area = eolopark.getArea();
        this.terrainType = eolopark.getTerrainType();
        this.substation = substation;
    }


    /* Getters and Setters */
    public Long getId () {
        return id;
    }

    public void setId (Long id) {
        this.id = id;
    }

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public String getCity () {
        return city;
    }

    public void setCity (String city) {
        this.city = city;
    }

    public double getLatitude () {
        return latitude;
    }

    public void setLatitude (double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude () {
        return longitude;
    }

    public void setLongitude (double longitude) {
        this.longitude = longitude;
    }

    public int getArea () {
        return area;
    }

    public void setArea (int area) {
        this.area = area;
    }

    public String getTerrainType () {
        return terrainType;
    }

    public void setTerrainType (String terrainType) {
        this.terrainType = terrainType;
    }

    public Substation getSubstation () {
        return substation;
    }

    public void setSubstation (Substation substation) {
        this.substation = substation;
    }

    public List<Aerogenerator> getAerogenerators () {
        return aerogenerators;
    }

    public void setAerogenerators (List<Aerogenerator> aerogenerators) {
        this.aerogenerators = aerogenerators;
    }

    public User getUser () {
        return user;
    }

    public void setUser (User user) {
        this.user = user;
    }

    /* Methods */
    @Override
    public String toString () {
        StringBuilder aux = new StringBuilder("Eolopark {id=" + id + ", name=" + name + ", city=" + city + ", " +
                "latitude=" + latitude + ", longitude=" + longitude + ", area=" + area + ", terrainType=" + terrainType + "\n" + substation.toString() + "\n");
        for (Aerogenerator aerogenerator : aerogenerators) {
            aux.append("\n").append(aerogenerator.toString());
        }
        return aux + "}";
    }
}