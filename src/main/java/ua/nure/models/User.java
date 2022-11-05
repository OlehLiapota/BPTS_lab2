package ua.nure.models;

public class User {
    private String login;
    private String password;
    private String key;

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getKey() {
        return key;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object obj) {
        return equals((User) obj);
    }

    private boolean equals(User user) {
        return this.getLogin().equals(user.getLogin()) && this.getPassword().equals(user.getPassword());
    }
}
