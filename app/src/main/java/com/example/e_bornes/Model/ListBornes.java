package com.example.e_bornes.Model;

import java.io.Serializable;
import java.util.ArrayList;

public class ListBornes implements Serializable {
    private ArrayList<Borne> bornes;
    private int numberMaxBornes;

    public ListBornes() {
        bornes = new ArrayList<>();
        numberMaxBornes = 0;
    }

    public ArrayList<Borne> getBornes() {
        return bornes;
    }

    public void addBorne(Borne borne){
        if (borne != null){
            bornes.add(borne) ;
        }
    }

    public int getNumberMaxBornes() {
        return numberMaxBornes;
    }

    public void setNumberMaxBornes(int numberMaxBornes) {
        this.numberMaxBornes = numberMaxBornes;
    }
}
