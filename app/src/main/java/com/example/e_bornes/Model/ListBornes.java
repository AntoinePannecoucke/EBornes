package com.example.e_bornes.Model;

import java.io.Serializable;
import java.util.ArrayList;

public class ListBornes implements Serializable {
    private ArrayList<Borne> bornes = new ArrayList<>();
    private int numberMaxBornes;

    public ListBornes() {
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

    public void clear() {
        if (!bornes.isEmpty()) {
            bornes.clear();
        }
    }
}
