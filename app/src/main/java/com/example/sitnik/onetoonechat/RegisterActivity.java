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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText name;
    private EditText password;
    private EditText email;
    private Button registerButton;

    private Toolbar registerToolbar;

    private ProgressDialog registerProgress;

    //Firebase Auth
    private FirebaseAuth mAuth;

    //FirebaseDatabase
    private DatabaseReference database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        registerProgress = new ProgressDialog(this);

        //Registration Fields
        name = findViewById(R.id.tv_name_settings);
        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        registerButton = findViewById(R.id.btn_register);

        //toolbar
        registerToolbar = findViewById(R.id.register_toolbar);
        setSupportActionBar(registerToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        registerButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                String displayName = name.getText().toString();
                String displayEmail = email.getText().toString();
                String displayPassword = password.getText().toString();

                if(!TextUtils.isEmpty(displayName) && !TextUtils.isEmpty(displayEmail) && !TextUtils.isEmpty(displayPassword) ){
                    registerProgress.setTitle("Register User");
                    registerProgress.setMessage("Registering your account");
                    registerProgress.setCanceledOnTouchOutside(false);
                    registerProgress.show();

                    registerUser(displayName, displayEmail, displayPassword);
                }

                else{
                    Toast.makeText(RegisterActivity.this, "Can not register. Please check the form and try again",
                            Toast.LENGTH_LONG).show();

                }


            }
        });
    }

    private void registerUser(final String name, String email, String password){

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = currentUser.getUid();

                    database = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                    HashMap<String, String> userMap = new HashMap<>();

                    userMap.put("name", name);
                    userMap.put("status", "Hi there I new Sitnik's fella!");
                    userMap.put("image", "default");
                    userMap.put("thumb_image", "default");

                    database.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                registerProgress.dismiss();
                                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();
                            }
                        }
                    });



                }

                else {
                    registerProgress.hide();
                    Toast.makeText(RegisterActivity.this, "Error while registering.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
