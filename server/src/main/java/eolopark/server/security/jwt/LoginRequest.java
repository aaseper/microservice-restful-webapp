package eolopark.server.security.jwt;

public class LoginRequest {

    /* Attributes */
    private String username;
    private String password;

    /* Constructor */
    public LoginRequest () {
    }

    public LoginRequest (String username, String password) {
        this.username = username;
        this.password = password;
    }

    /* Methods */
    public String getUsername () {
        return username;
    }

    public void setUsername (String username) {
        this.username = username;
    }

    public String getPassword () {
        return password;
    }

    public void setPassword (String password) {
        this.password = password;
    }

    @Override
    public String toString () {
        return "LoginRequest [username=" + username + ", password=" + password + "]";
    }
}
