package dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CredentialEntry {
    private String place;
    private String login;
    private String password;
    private String comment;

    @JsonIgnore
    private CredentialEntry parent;
    private List<CredentialEntry> children;

    public CredentialEntry() {
        children = new ArrayList<>();
    }

    public CredentialEntry(CredentialEntry parent) {
        this();
        this.parent = parent;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public CredentialEntry getParent() {
        return parent;
    }

    public void setParent(CredentialEntry parent) {
        this.parent = parent;
    }

    public void addChild(CredentialEntry entry) {
        children.add(entry);
    }

    public List<CredentialEntry> getChildren() {
        return children;
    }
}
