package com.example.sitnik.onetoonechat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;


public class AddGroupImage extends AppCompatActivity {

    private DatabaseReference mDatabaseRef;
    private StorageReference mStorageReference;
    private DatabaseReference mGroupDatabaseReference;

    private Button mSubmitImage;

    private CircleImageView mButtonImage;

    private String group_name, name_display;

    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private String mCurrentUserId;


    private static final int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group_image);

        group_name = getIntent().getStringExtra("group_name");

        mSubmitImage = findViewById(R.id.btn_submit_img);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mGroupDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Groups").child(group_name);

        mButtonImage = findViewById(R.id.img_group);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mStorageReference = FirebaseStorage.getInstance().getReference();

        mSubmitImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddGroupImage.this, CreateGroupActivity.class);
                intent.putExtra("group_date", group_name);
                intent.putExtra("name_display", name_display);
                startActivity(intent);
                finish();

            }
        });

        mButtonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(AddGroupImage.this);

            }
        });

        mGroupDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final String image = dataSnapshot.child("image").getValue().toString();
                String thumb = dataSnapshot.child("thumb_image").getValue().toString();
                name_display = dataSnapshot.child("name").getValue().toString();


                if(!image.equals("default")){

                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_avatar).into(mButtonImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {

                            Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(mButtonImage);

                        }
                    });

                }
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
                    .setMinCropResultSize(500, 500)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                progressDialog = new ProgressDialog(AddGroupImage.this);
                progressDialog.setTitle("Updating image");
                progressDialog.setMessage("Please wait ,we are uploading your image.");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Uri resultUri = result.getUri();

                final File thumb_filePath = new File(resultUri.getPath());

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                String uid = currentUser.getUid();

                try {

                    Bitmap thumbnailImage = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(50)
                            .compressToBitmap(thumb_filePath);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumbnailImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    final byte[] thumb_byte = baos.toByteArray();

                    StorageReference filepath = mStorageReference.child("profile_images").child(uid+".jpg");
                    final StorageReference thumb_filepath = mStorageReference.child("profile_images").child("Thumbs").child(uid+".jpg");

                    filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if(task.isSuccessful()){

                                final String downloadUrl = task.getResult().getDownloadUrl().toString();

                                UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                        String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                        if (thumb_task.isSuccessful()){

                                            Map update_hashMap = new HashMap<>();
                                            update_hashMap.put("image", downloadUrl);
                                            update_hashMap.put("thumb_image", thumb_downloadUrl);

                                            mGroupDatabaseReference.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){


                                                        Toast.makeText(AddGroupImage.this, "Uploading image to database succesful", Toast.LENGTH_LONG).show();
                                                    }

                                                }
                                            });

                                            mDatabaseRef.child("Users").child(mCurrentUserId).child("groups").child(group_name).updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull Task task) {
                                                    if(task.isSuccessful()){

                                                        progressDialog.dismiss();
                                                        Toast.makeText(AddGroupImage.this, "Uploading image to user database succesful", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });

                            }

                            else{

                                Toast.makeText(AddGroupImage.this, "error while uploading", Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();

                            }
                        }
                    });


                } catch (IOException e) {

                    e.printStackTrace();

                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
    }

}
