package com.example.sitnik.onetoonechat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText name;
    private EditText password;
    private EditText email;
    private Button registerButton;

    private Toolbar registerToolbar;

    private ProgressDialog registerProgress;

    //Firebase Auth
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        registerProgress = new ProgressDialog(this);

        //Registration Fields
        name = findViewById(R.id.et_name);
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
                    registerProgress.setMessage("Registering yout account");
                    registerProgress.setCanceledOnTouchOutside(false);
                    registerProgress.show();

                    registerUser(displayName, displayEmail, displayPassword);
                }

                else{
                    Toast.makeText(RegisterActivity.this, "Fill the form",
                            Toast.LENGTH_LONG).show();

                }


            }
        });
    }

    private void registerUser(String name, String email, String password){

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    //registerProgress.dismiss();
                    Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                }

                else {
                    //registerProgress.hide();
                    Toast.makeText(RegisterActivity.this, "Error while registering.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
