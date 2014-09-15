package com.example.ditjon.ditjonberisha;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class Location extends Activity {

    public TextView latitude;
    public TextView longitude;
    public TextView altitude;
    public TextView city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        latitude = (TextView) findViewById(R.id.txtViewlatitude);
        longitude = (TextView) findViewById(R.id.txtViewlongitude);
        altitude = (TextView) findViewById(R.id.txtViewaltitude);
        city = (TextView) findViewById(R.id.txtViewcity1);

        //Accept data from MyActivity class
        Bundle extras = getIntent().getExtras();
        String strcity = extras.getString("city");
        String strlatitude = extras.getString("latitude");
        String strlongitude = extras.getString("longitude");
        String straltitude = extras.getString("altitude");

        city.setText(strcity);
        latitude.setText(strlatitude);
        longitude.setText(strlongitude);
        altitude.setText(straltitude);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
