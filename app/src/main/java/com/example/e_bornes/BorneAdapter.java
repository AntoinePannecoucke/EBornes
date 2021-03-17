package com.example.e_bornes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.e_bornes.Model.Borne;

import java.util.ArrayList;

public class BorneAdapter extends ArrayAdapter {

    private ArrayList<Borne> bornes;

    public BorneAdapter(Context context, ArrayList<Borne> bornes) {
        super(context, 0, bornes);
        this.bornes = bornes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Borne borne = this.bornes.get(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_borne_item, parent, false);
        }

        TextView address = convertView.findViewById(R.id.address_item);
        address.setText(borne.getName());
        TextView access = convertView.findViewById(R.id.acessibility_item);
        access.setText(borne.getAccessibility());

        return convertView;
    }
}
