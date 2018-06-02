package com.example.sitnik.onetoonechat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class StatusActivity extends AppCompatActivity {

    /***layout*/
    private Toolbar toolbar;
    private EditText et_statusUpdate;
    private Button btn_statusUpdate;

    /***database*/
    private DatabaseReference database;
    private FirebaseUser currentUser;

    /***other*/
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        et_statusUpdate = findViewById(R.id.updateStatus);
        btn_statusUpdate = findViewById(R.id.updateStatusButton);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid(); /***current user id*/
        database = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        String statusValue = getIntent().getStringExtra("status_value");
        et_statusUpdate.setText(statusValue);

        toolbar = findViewById(R.id.status_appBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /***updating database with new status*/
        btn_statusUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Progress
                progressDialog = new ProgressDialog(StatusActivity.this);
                progressDialog.setTitle("Saving Changes");
                progressDialog.setMessage("Please wait, we are saving changes.");
                progressDialog.show();

                String status = et_statusUpdate.getText().toString();

                database.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            progressDialog.dismiss();

                        } else{
                            Toast.makeText(StatusActivity.this, "There was some error while saving changes.", Toast.LENGTH_LONG).show() ;

                        }
                    }
                });
            }
        });

    }
}
