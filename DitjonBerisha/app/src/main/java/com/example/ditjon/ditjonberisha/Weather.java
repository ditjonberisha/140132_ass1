package com.example.ditjon.ditjonberisha;

import android.app.Activity;
import android.app.AlertDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class Weather extends Activity {

    TextView temperature;
    TextView pressure;
    TextView humidity;
    TextView description;
    TextView city;
    private String StrLatitude;
    private String StrLongitude;
    private String StrCity;
    private String StrTemperature;
    private String StrPressure;
    private String StrHumidity;
    private String StrDescription;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        temperature = (TextView) findViewById(R.id.txtViewtemperature);
        pressure = (TextView) findViewById(R.id.txtViewpressure);
        humidity = (TextView) findViewById(R.id.txtViewhumidity);
        description = (TextView) findViewById(R.id.txtViewdescription);
        city = (TextView) findViewById(R.id.txtViewcity);

        //Accept data from MyActivity class
        Bundle extras = getIntent().getExtras();
        StrCity=extras.getString("city");
        city.setText(StrCity);
        StrLatitude = extras.getString("latitude");
        StrLongitude = extras.getString("longitude");

        db = openOrCreateDatabase("database.db", MODE_PRIVATE, null);


        // if has internet will get weather from url that has latitude and longitude
        if(isConnected() && !StrLatitude.equals("n/a")) {
            new HttpAsyncTask().execute("http://api.openweathermap.org/data/2.5/weather?lat=" + StrLatitude + "&lon=" + StrLongitude);
        }
        else {
            this.history();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.weather, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            try {
                JSONObject json = new JSONObject(result);

                // Get the data from JSONObject
                Double temp = Double.parseDouble(json.getJSONObject("main").getString("temp"))-273.15;
                StrTemperature = temp.toString();
                StrPressure = json.getJSONObject("main").getString("pressure");
                StrHumidity = json.getJSONObject("main").getString("humidity");
                StrDescription = json.getJSONArray("weather").getJSONObject(0).getString("description");

                temperature.setText(StrTemperature);
                pressure.setText(StrPressure);
                humidity.setText(StrHumidity);
                description.setText(StrDescription);
                new_data();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    // http://hmkcode.com/android-parsing-json-data/
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    // refresh the weather
    public void refresh(View v){
        if(isConnected() && !StrLatitude.equals("n/a")) {
            new HttpAsyncTask().execute("http://api.openweathermap.org/data/2.5/weather?lat="+StrLatitude+"&lon="+StrLongitude);
            Toast.makeText(this, "Connecting...", Toast.LENGTH_LONG).show();
        } else {
            this.history();
        }
    }

    // http://stackoverflow.com/questions/18375962/how-to-check-that-android-phone-is-not-connected-to-internet
    public boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }
    public void new_data(){
        try {
            // Insert data in table tblWeather
            db.execSQL("Insert into tblWeather(City,Temperature,Description,Pressure,Humidity) Values('" + StrCity + "','" +
                    StrTemperature + "','" + StrDescription + "','" + StrPressure + "','" + StrHumidity + "');");
            Toast.makeText(this,"Inserted into database",Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(this,"Not Inserted into database: " + e.toString(),Toast.LENGTH_LONG).show();
        }
    }

    public void history(){
        try {
            // Select the last row from table
            Cursor cursor = db.rawQuery("SELECT * FROM tblWeather ORDER BY ID DESC LIMIT 1", null);
            if (cursor != null) {
                cursor.moveToNext();
                //get data from history
                StrCity = cursor.getString(cursor.getColumnIndex("City"));
                StrTemperature = cursor.getString(cursor.getColumnIndex("Temperature"));
                StrDescription = cursor.getString(cursor.getColumnIndex("Description"));
                StrPressure = cursor.getString(cursor.getColumnIndex("Pressure"));
                StrHumidity = cursor.getString(cursor.getColumnIndex("Humidity"));

                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("No internet connection or gps!");
                alert.setMessage("Data from history");
                alert.setPositiveButton("OK",null);
                alert.show();

                city.setText(StrCity);
                temperature.setText(StrTemperature);
                description.setText(StrDescription);
                pressure.setText(StrPressure);
                humidity.setText(StrHumidity);
            }
        }catch (Exception e){
            Toast.makeText(this, "No data on the history", Toast.LENGTH_LONG).show();
        }
    }

}
