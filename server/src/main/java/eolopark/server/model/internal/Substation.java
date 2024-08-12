package eolopark.server.model.internal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity (name = "substation")
public class Substation {

    /* Attributes */
    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    private Long id;
    private String model;
    private double power;
    private double voltage;

    /* Constructors */
    public Substation () {
    }

    public Substation (String model, double power, double voltage) {
        super();
        this.model = model;
        this.power = power;
        this.voltage = voltage;
    }

    /* Getters and Setters */
    public Long getId () {
        return id;
    }

    public void setId (Long id) {
        this.id = id;
    }

    public String getModel () {
        return model;
    }

    public void setModel (String model) {
        this.model = model;
    }

    public double getPower () {
        return power;
    }

    public void setPower (double power) {
        this.power = power;
    }

    public double getVoltage () {
        return voltage;
    }

    public void setVoltage (double voltage) {
        this.voltage = voltage;
    }

    /* toString */
    @Override
    public String toString () {
        return "Substation {id=" + id + ", model=" + model + ", power=" + power + ", voltage=" + voltage + '}';
    }
}