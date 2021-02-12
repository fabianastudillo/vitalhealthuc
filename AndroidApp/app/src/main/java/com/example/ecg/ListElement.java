package com.example.ecg;

import java.io.Serializable;

public class ListElement implements Serializable {

    public String Fecha;

    public ListElement(String fecha) {
        Fecha = fecha;
    }

    public String getFecha() {
        return Fecha;
    }

    public void setFecha(String fecha) {
        Fecha = fecha;
    }
}
