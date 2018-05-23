package com.example.sitnik.onetoonechat;
//moze byc cos zle z user permissions
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private android.support.v7.widget.Toolbar mainToolbar;

    private ViewPager viewPager;

    private TabLayout tabLayout;

    private SectionsPagerAdapter sectionsPagerAdapter;

    private DatabaseReference mUserRef;

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

            //tabs
            viewPager = findViewById(R.id.main_tabPager);
            sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

            viewPager.setAdapter(sectionsPagerAdapter);

            tabLayout = findViewById(R.id.main_tabs);
            tabLayout.setupWithViewPager(viewPager);


            mainToolbar = findViewById(R.id.main_page_toolbar);
            setSupportActionBar(mainToolbar);
            getSupportActionBar().setTitle("Talk to Sitnik");


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
    protected void onStop() {
        super.onStop();


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

        if(item.getItemId() == R.id.main_logout_btn) {

            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }

        if(item.getItemId() == R.id.main__settings_btn){
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

        if(item.getItemId() == R.id.main_allUsers_btn){
            Intent intent = new Intent(MainActivity.this, UsersActivity.class);
            startActivity(intent);
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
