package com.example.sitnik.onetoonechat;
//moze byc cos zle z user permissions
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private android.support.v7.widget.Toolbar mainToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Checking internet connection, external storage
        if (isOnline()==false) {
            MainActivity.this.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
        }

        if (!isExternalStorageWritable()){
            Toast.makeText(MainActivity.this, "No external storage writable!", Toast.LENGTH_LONG).show();
        }

        if (!isExternalStorageReadable()){
            Toast.makeText(MainActivity.this, "No external storage readable!", Toast.LENGTH_LONG).show();
        }


            setContentView(R.layout.activity_main);

            mAuth = FirebaseAuth.getInstance();

            mainToolbar = findViewById(R.id.main_page_toolbar);
            setSupportActionBar(mainToolbar);
            getSupportActionBar().setTitle("SitnikSB Chat");

    }

    private void sendToStart() {
        Intent StartIntent = new Intent(this, StartActivity.class);
        startActivity(StartIntent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            sendToStart();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.main_logout_btn){

            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }

        return true;
    }

    //Checking if we have internet connection
    public boolean isOnline() {
        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if(netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()){
            Toast.makeText(MainActivity.this, "No Internet connection!", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    // Checks if external storage is available for read and write
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    // Checks if external storage is available to at least read
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

}
