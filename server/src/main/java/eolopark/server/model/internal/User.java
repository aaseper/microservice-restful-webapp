package eolopark.server.model.internal;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity (name = "user")
public class User {

    /* Attributes */
    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    private Long id;
    @Column (unique = true)
    private String name;
    private String encodedPassword;
    private int parkMax;
    @ElementCollection (fetch = FetchType.EAGER)
    private List<String> roles;

    @OneToMany (mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Eolopark> eoloparks = new ArrayList<>();

    /* Constructors */
    public User () {
    }

    public User (String name, String encodedPassword) {
        this.name = name;
        this.encodedPassword = encodedPassword;
    }

    public User (String name, String encodedPassword, int parkMax, String... roles) {
        this.name = name;
        this.encodedPassword = encodedPassword;
        this.parkMax = parkMax;
        this.roles = List.of(roles);
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

    public String getEncodedPassword () {
        return encodedPassword;
    }

    public void setEncodedPassword (String encodedPassword) {
        this.encodedPassword = encodedPassword;
    }

    public int getParkMax () {
        return parkMax;
    }

    public void setParkMax (int parkMax) {
        this.parkMax = parkMax;
    }

    public List<String> getRoles () {
        return roles;
    }

    public void setRoles (List<String> roles) {
        this.roles = roles;
    }

    public List<Eolopark> getEoloparks () {
        return eoloparks;
    }

    public void setEoloparks (List<Eolopark> eoloparks) {
        this.eoloparks = eoloparks;
    }

    public Eolopark getEolopark (int index) {
        return this.eoloparks.get(index);
    }

    public void setEolopark (Eolopark eolopark) {
        this.eoloparks.add(eolopark);
    }

    /* Methods */
    @Override
    public String toString () {
        return "Name: " + name + ", parkMax: " + parkMax + ", roles: " + roles;
    }
}