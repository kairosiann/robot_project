package com.samples.flironecamera.view.main;

import com.google.android.gms.vision.face.Face;

public class PersonData {
    private double temp;

    public PersonData(double temp) {
        this.temp = temp;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }
}
