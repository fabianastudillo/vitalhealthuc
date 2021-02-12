package com.example.ecg;

public class DatosEmail {

    private String emailDoctor;
    private String name;

    public DatosEmail() {
    }

    public DatosEmail(String emailDoctor, String name) {
        this.emailDoctor = emailDoctor;
        this.name = name;
    }

    public String getEmailDoctor() {
        return emailDoctor;
    }

    public void setEmailDoctor(String emailDoctor) {
        this.emailDoctor = emailDoctor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Datos{" +
                "emailDoctor='" + emailDoctor + '\'' +
                ", name=" + name +
                '}';
    }
}
