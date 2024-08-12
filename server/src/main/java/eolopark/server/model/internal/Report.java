package eolopark.server.model.internal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity (name = "report")
public class Report {

    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    private Long id;
    private String username;
    private int progress;
    private boolean completed;

    /* Constructors */
    public Report (String username, int progress, boolean completed) {
        this.username = username;
        this.progress = progress;
        this.completed = completed;
    }

    public Report () {
    }

    /* Getters and Setters */
    public Long getId () {
        return id;
    }

    public String getUsername () {
        return username;
    }

    public void setUsername (String username) {
        this.username = username;
    }

    public int getProgress () {
        return progress;
    }

    public void setProgress (int progress) {
        this.progress = progress;
    }

    public boolean isCompleted () {
        return completed;
    }

    public void setCompleted (boolean completed) {
        this.completed = completed;
    }

    /* Methods */
    @Override
    public String toString () {
        return "CreationProgressMessage{" + "id=" + id + ", progress=" + progress + ", completed=" + completed + '}';
    }
}