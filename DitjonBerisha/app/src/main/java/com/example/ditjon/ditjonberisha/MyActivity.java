package com.example.ditjon.ditjonberisha;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MyActivity extends Activity {

    // create variable
    private GoogleMap map;
    private String city = "n/a";
    private String latitude = "n/a";
    private String longitude = "n/a";
    private String altitude = "n/a";
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        // Create or open database
        db=openOrCreateDatabase("database.db", MODE_PRIVATE,null);

        // Create table tblWeather
        db.execSQL("Create Table if not exists tblWeather (ID INTEGER PRIMARY KEY AUTOINCREMENT, City Varchar, " +
                "Temperature Varchar, Description Varchar, Pressure Varchar, Humidity Varchar)");

        // Create table tblLocation
        db.execSQL("Create Table if not exists tblLocation (ID INTEGER PRIMARY KEY AUTOINCREMENT, City Varchar, " +
                "Latitude Varchar, Longitude Varchar, Altitude Varchar)");

        // Get map
        map=((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMyLocationEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
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

    // My location button onclick
    public void location(View v){

        if (map.getMyLocation() != null) {
            // call function get_location
            this.get_location();
            // create a string for toast
            String message = "You are in: " + city;
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            // Zoom camera on my location
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)), 15));
        }
        else
            Toast.makeText(this,"Location not found!",Toast.LENGTH_SHORT).show();

    }

    public void get_location(){
        if (map.getMyLocation() != null) {
            try {
                // get latitude
                latitude = String.valueOf(map.getMyLocation().getLatitude());
                // get longitude
                longitude = String.valueOf(map.getMyLocation().getLongitude());
                // get altitude
                altitude = String.valueOf(map.getMyLocation().getAltitude());

                // get name of the city
                Geocoder gcd = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = gcd.getFromLocation(Double.parseDouble(latitude), Double.parseDouble(longitude), 1);
                if (addresses.size() > 0) {
                    city = (addresses.get(0).getLocality());
                } else
                    city = "City not found";
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Weather button onclick
    public void weather(View v){

        // start weather activity and send data(city,latitude,longitude)
        this.get_location();
        Intent intent = new Intent(this, Weather.class);
        Bundle extras = new Bundle();
        intent.putExtra("city",city);
        extras.putString("latitude",latitude);
        extras.putString("longitude",longitude);
        intent.putExtras(extras);
        startActivity(intent);
    }

    // Info location button onclick
    public void info_location(View v){

        // call function get_location
        this.get_location();
        if(map.getMyLocation() != null){
            try {
                // Insert data in table tblLocation
                db.execSQL("Insert into tblLocation(City,Latitude,Longitude,Altitude) Values('" + city + "','" + latitude
                        + "','" + longitude + "','" + altitude + "');");
                Toast.makeText(this,"Inserted into database",Toast.LENGTH_SHORT).show();
            }catch (Exception e){
                Toast.makeText(this,"Not Inserted into database: " + e.toString(),Toast.LENGTH_LONG).show();
            }
        }
        else {
            try {
                // Create query, Select the last row
                Cursor cursor = db.rawQuery("SELECT * FROM tblLocation ORDER BY ID DESC LIMIT 1", null);
                if (cursor != null) {
                    cursor.moveToNext();
                    // get column city from last row
                    city = cursor.getString(cursor.getColumnIndex("City"));
                    // get column latitude from last row
                    latitude = cursor.getString(cursor.getColumnIndex("Latitude"));
                    // get column longitude from last row
                    longitude = cursor.getString(cursor.getColumnIndex("Longitude"));
                    // get column altitude from last row
                    altitude = cursor.getString(cursor.getColumnIndex("Altitude"));

                    // Alert dialog to inform
                    AlertDialog.Builder alert = new AlertDialog.Builder(this,5);
                    alert.setTitle("No gps found!");
                    alert.setMessage("Data from history");
                    alert.setPositiveButton("OK",null);
                    alert.show();
                }
            }catch (Exception e){
                Toast.makeText(this, "No data on the history", Toast.LENGTH_LONG).show();
            }
        }

        // start Location activity and send data(city,latitude,longitude,altitude)
        Intent intent = new Intent(this, Location.class);
        Bundle extras = new Bundle();
        extras.putString("city",city);
        extras.putString("latitude",latitude);
        extras.putString("longitude",longitude);
        extras.putString("altitude",altitude);
        intent.putExtras(extras);
        startActivity(intent);
    }

}

