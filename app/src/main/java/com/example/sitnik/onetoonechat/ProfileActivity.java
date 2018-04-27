package com.example.sitnik.onetoonechat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView mName, mStatus, mFriendsCount;
    private Button mRequestFriend, mDeclineFriend;
    private CircleImageView mProfileImage;

    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendsRequestDatabase;

    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgressDialog;

    private String mCurrent_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String userId = getIntent().getStringExtra("user_id");

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        mFriendsRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mName = findViewById(R.id.et_name_profile);
        mStatus = findViewById(R.id.et_status_profile);
        mFriendsCount = findViewById(R.id.et_friends_profile);
        mRequestFriend = findViewById(R.id.btn_friend_request);
        mDeclineFriend = findViewById(R.id.btn_decline_profile);
        mProfileImage = findViewById(R.id.img_main_profile);

        mCurrent_state = "no_friends";

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait until we load user data.");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);
                Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);

                mProgressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mRequestFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCurrent_state.equals("no_friends")){

                    mFriendsRequestDatabase.child(mCurrentUser.getUid()).child(userId).child("request_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                mFriendsRequestDatabase.child(userId).child(mCurrentUser.getUid()).child("request_type")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(ProfileActivity.this, "Request sent", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                            else{

                                Toast.makeText(ProfileActivity.this, "Failed sending request", Toast.LENGTH_LONG).show();

                            }
                        }
                    });


                }
            }
        });

    }
}
