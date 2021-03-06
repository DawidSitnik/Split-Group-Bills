package com.example.sitnik.onetoonechat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

/***class to log into user account*/
public class LoginActivity extends AppCompatActivity {

    /***layout*/
    private EditText password;
    private EditText email;
    private Button loginButton;
    private Toolbar loginToolbar;

    /***database*/
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;

    /***other*/
    private ProgressDialog loginProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        loginButton = findViewById(R.id.btn_login);

        mAuth = FirebaseAuth.getInstance();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        loginProgress = new ProgressDialog(this);
        loginToolbar = findViewById(R.id.register_toolbar);
        setSupportActionBar(loginToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            /***getting values from input and trying to login*/
            public void onClick(View v) {

                String displayEmail = email.getText().toString();
                String displayPassword = password.getText().toString();

                if(!TextUtils.isEmpty(displayEmail) && !TextUtils.isEmpty(displayPassword) ){
                    loginProgress.setTitle("Sign in User");
                    loginProgress.setMessage("Signing in to your account");
                    loginProgress.setCanceledOnTouchOutside(false);
                    loginProgress.show();

                    loginUser(displayEmail, displayPassword); //login function
                }
            }
        });

    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            /***updating database with users device token id*/
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    String current_user_id = mAuth.getCurrentUser().getUid();

                    mUsersDatabase.child(current_user_id).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            loginProgress.dismiss();
                            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();
                        }
                    });
                }

                else {
                    loginProgress.hide();
                    Toast.makeText(LoginActivity.this, "Can not sign in. Please check the form and try again", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
