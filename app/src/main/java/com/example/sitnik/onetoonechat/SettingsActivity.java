package com.example.sitnik.onetoonechat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference database;
    private FirebaseDatabase currentUser;

    //Android Layout
    private TextView name_settings;
    private TextView status_settings;
    private CircleImageView image_settings;

    //Buttons
    private Button btn_image_settings;
    private Button btn_status_settings;

    private static final int GALLERY_PICK = 1;

    //Storage Firebase
    private StorageReference storageReference;

    //Progress bar
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        name_settings = findViewById(R.id.tv_name_settings);
        status_settings = findViewById(R.id.tv_status_settings);
        image_settings = findViewById(R.id.img_settings);

        btn_image_settings = findViewById(R.id.btn_image_settings);
        btn_status_settings = findViewById(R.id.btn_status_settings);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();

        database = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        storageReference = FirebaseStorage.getInstance().getReference();

        btn_status_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String statusValue = status_settings.getText().toString();

                Intent intent = new Intent(SettingsActivity.this, StatusActivity.class);
                intent.putExtra("status_value", statusValue);
                startActivity(intent);
            }
        });

        btn_image_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);

            }
        });



        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb = dataSnapshot.child("thumb_image").getValue().toString();

                name_settings.setText(name);
                status_settings.setText(status);
                Picasso.get().load(image).into(image_settings);




            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK){


            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                progressDialog = new ProgressDialog(SettingsActivity.this);
                progressDialog.setTitle("Updating image");
                progressDialog.setMessage("Please wait ,we are uploading your image.");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Uri resultUri = result.getUri();

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                String uid = currentUser.getUid();

                StorageReference filepath = storageReference.child("profile_images").child(uid+"jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful()){

                            String downloadUrl = task.getResult().getDownloadUrl().toString();
                            database.child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {

                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){

                                        progressDialog.dismiss();
                                        Toast.makeText(SettingsActivity.this, "Uploading image to database succesful", Toast.LENGTH_LONG).show();
                                    }

                                }
                            });

                        }

                        else{

                            Toast.makeText(SettingsActivity.this, "error while uploading", Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();

                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
    }

}
