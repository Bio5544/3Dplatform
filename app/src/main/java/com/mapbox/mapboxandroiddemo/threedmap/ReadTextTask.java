package com.mapbox.mapboxandroiddemo.threedmap;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


// This class is called by seite2 class and carried out in the background.
// this class to read the information from the levels_num.json File on the server and to pass this information to the seite2 class
public class ReadTextTask extends AsyncTask<String, Void, Void> {
    String data="";
    String level="";

    @Override
    protected Void doInBackground(String... strings) {

        try {
            URL url= new URL(strings[0]+"/"+"levels_num.json");
            HttpURLConnection httpURLConnection =(HttpURLConnection) url.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader =  new BufferedReader(new InputStreamReader(inputStream));
            String line="";
            while (line != null){
                line= bufferedReader.readLine();
                data= data+line;
            }

            JSONArray JA = new JSONArray(data);
            for (int i=0 ; i<JA.length(); i++){
                JSONObject JO= (JSONObject) JA.get(i);
                level = level+ (String) JO.get("level");
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void  aVoid) {
        super.onPostExecute(aVoid);
        Log.d("readTask_test", level);
        seite2.tex2.setText(level);
    }
}

