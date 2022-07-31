package com.example.militaryaibot;

public class ChatUser {

    private String firstName, middleName, givenName;

    public ChatUser(){}
    public ChatUser(String firstName, String givenName){
        this.firstName = firstName;
        this.givenName = givenName;
    }
    public ChatUser(String firstName, String middleName, String givenName){
        this.firstName = firstName;
        this.middleName = middleName;
        this.givenName = givenName;
    }

    public String getFullName() {
        return firstName + " " + middleName + " " + givenName;
    }

}
