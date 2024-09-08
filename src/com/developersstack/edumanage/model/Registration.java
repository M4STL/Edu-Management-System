package com.developersstack.edumanage.model;

public class Registration {
    private String registrationID;
    private String name;
    private String programs;
    private boolean paid;

    public Registration() {
    }

    public Registration(String registrationID, String name, String programs, boolean paid) {
        this.registrationID = registrationID;
        this.name = name;
        this.programs = programs;
        this.paid = paid;
    }

    public String getRegistrationID() {
        return registrationID;
    }

    public void setRegistrationID(String registrationID) {
        this.registrationID = registrationID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrograms() {
        return programs;
    }

    public void setPrograms(String programs) {
        this.programs = programs;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }
}
