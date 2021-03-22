package com.example.e_bornes.AsyncTasks;

import android.os.AsyncTask;
import android.util.Log;

import com.example.e_bornes.BorneAdapter;
import com.example.e_bornes.Model.Borne;
import com.example.e_bornes.Model.ListBornes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class LoadBorneFilter extends AsyncTask<Object, Void, Boolean> {

    private static final String API_URL_START = "https://public.opendatasoft.com/api/records/1.0/search/?dataset=fichier-consolide-des-bornes-de-recharge-pour-vehicules-electriques-irve&q=&rows=1000&start=";
    private static final String API_URL_END ="&facet=accessibilite&facet=code_insee&facet=acces_recharge&facet=ad_station&facet=puiss_max&facet=coordonnees&facet=n_station";
    private static final String ZIP_FILTER_PREFIX = "&refine.code_insee=";
    private static final String DEP_FILTER_PREFIX = "&refine.dep_code=";

    private static final String[] KEYS_STRING = {"accessibilite", "code_insee", "acces_recharge", "ad_station",  "n_station"};
    private static final String KEY_INTEGER = "puiss_max";
    private static final String KEY_COORDINATES = "coordonnees";
    private BorneAdapter adapter;

    public void onPostExecute(Boolean result){
        adapter.notifyDataSetChanged();
    }


    /**
     * @param objects [list , adapter, startIndex, filter]
     * @return boolean
     */
    @Override
    protected Boolean doInBackground(Object... objects) {
        ListBornes bornes = (ListBornes) objects[0];
        adapter = (BorneAdapter) objects[1];
        String filter = (String) objects[3];
        HttpsURLConnection connection = null;
        try {
            URL url;
            switch (filter.length()){
                case 2:
                    url = new URL(API_URL_START+ objects[2] + API_URL_END + DEP_FILTER_PREFIX + filter);
                    break;
                case 5:
                    url = new URL(API_URL_START+ objects[2] + API_URL_END + ZIP_FILTER_PREFIX + filter);
                    break;
                default:
                    return false;
            }

            connection = (HttpsURLConnection) url.openConnection();

            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK){

                //InputBuffer
                InputStreamReader in = new InputStreamReader(connection.getInputStream());
                BufferedReader buff = new BufferedReader(in);
                StringBuilder builder = new StringBuilder();
                String tmp = "";

                //Read data on builder
                while ((tmp = buff.readLine()) != null){
                    builder.append(tmp);
                }

                //Parse JSON
                JSONObject root = new JSONObject(builder.toString());

                Log.d("number", "" + root.getInt("nhits"));

                bornes.setNumberMaxBornes(root.getInt("nhits"));

                JSONArray records = root.getJSONArray("records");
                for (int index = 0; index < records.length(); index++) {

                    JSONObject record = records.getJSONObject(index);
                    JSONObject fields = record.getJSONObject("fields");
                    String[] data = new String[KEYS_STRING.length];
                    for (int i = 0; i < KEYS_STRING.length; i++) {
                        if (fields.has(KEYS_STRING[i])) {
                            data[i] = fields.getString(KEYS_STRING[i]);
                        }else {
                            data[i] = "";
                        }

                    }
                    int power = 0;
                    if (fields.has(KEY_INTEGER)) {
                       power = fields.getInt(KEY_INTEGER);
                    }
                    double[] coordinates = new double[2];
                    JSONArray coords = fields.getJSONArray(KEY_COORDINATES);
                    for (int i = 0; i < coords.length(); i++){
                        coordinates[i] = coords.getDouble(i);
                    }

                    Borne borne = new Borne(data, power, coordinates);

                    bornes.addBorne(borne);

                }

            }
            else {

                Log.e("Response Code", "Response code = " + connection.getResponseCode());
                connection.disconnect();
                return false;

            }

        } catch (MalformedURLException e) {

            Log.e("Url", "Url malformed", e);

        } catch (IOException e) {

            Log.e("Connection", "Connection Error", e);

        } catch (JSONException e) {
            Log.e("Json parsing", "error on parsing", e);
        }
        finally {
            if  (connection != null) {
                connection.disconnect();
            }
        }

        return true;
    }
}
