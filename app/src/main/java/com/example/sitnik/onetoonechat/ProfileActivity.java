//notifications are just for single device, if we want to use multiple devices we have to change few things

package com.example.sitnik.onetoonechat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView mName, mStatus, mFriendsCount;
    private Button mRequestFriend, mDeclineFriend;
    private ImageView mProfileImage;

    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendsRequestDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mDatabaseRef;

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
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mName = findViewById(R.id.et_name_profile);
        mStatus = findViewById(R.id.et_status_profile);
        mFriendsCount = findViewById(R.id.et_friends_profile);
        mRequestFriend = findViewById(R.id.btn_friend_request);
        mDeclineFriend = findViewById(R.id.btn_decline_profile);
        mProfileImage = findViewById(R.id.img_main_profile);

        mCurrent_state = "not_friends";

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

                mDeclineFriend.setVisibility(View.INVISIBLE);
                mDeclineFriend.setEnabled(false);

                mName.setText(name);
                mStatus.setText(status);
                Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);

                //-----------FRIEND LIST / REQUEST FEATURE -------------

                mFriendsRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(userId)){

                            String req_type = dataSnapshot.child(userId).child("request_type").getValue().toString();

                            if(req_type.equals("received")){

                                mCurrent_state = "req_received";
                                mRequestFriend.setText("Accept Friend Request");

                                mDeclineFriend.setVisibility(View.VISIBLE);
                                mDeclineFriend.setEnabled(true);

                            }

                            if(req_type.equals("sent")){

                                mCurrent_state = "req_sent";
                                mRequestFriend.setText("Cancel Friend Request");

                                mDeclineFriend.setVisibility(View.INVISIBLE);
                                mDeclineFriend.setEnabled(false);

                            }
                            mProgressDialog.dismiss();

                        }

                        else{

                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(userId)){

                                        mCurrent_state = "friends";
                                        mRequestFriend.setText("Unfriend this Person");

                                        mDeclineFriend.setVisibility(View.INVISIBLE);
                                        mDeclineFriend.setEnabled(false);
                                    }

                                    mProgressDialog.dismiss();

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgressDialog.dismiss();

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
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //---------------------- ADD FRIENDS BUTTON -----------------------------
        mRequestFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mRequestFriend.setEnabled(false);


                //---------NOT FRIEND---------

                if(mCurrent_state.equals("not_friends")){

                    DatabaseReference newNotificationer = mDatabaseRef.child("Notifications").child(userId).push();
                    String newNotificationId = newNotificationer.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrentUser.getUid());
                    notificationData.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + userId + "/request_type", "sent");
                    requestMap.put("Friend_req/" + userId + "/" + mCurrentUser.getUid() + "/request_type", "received");
                    requestMap.put("Notifications/" + userId + "/" + newNotificationId, notificationData);

                    mDatabaseRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG);
                            }

                            else{
                                mCurrent_state = "req_sent";
                                mRequestFriend.setText("Cancel Friend Request");
                                Toast.makeText(ProfileActivity.this, "Request sent", Toast.LENGTH_LONG).show();
                            }

                            mRequestFriend.setEnabled(true);
                        }
                    });


                }

                //-------------------FRIENDS-----------------
                if(mCurrent_state.equals("req_sent")){

                    mFriendsRequestDatabase.child(mCurrentUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendsRequestDatabase.child(userId).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mRequestFriend.setEnabled(true);
                                    mCurrent_state = "not_friends";
                                    mRequestFriend.setText("Send Friend Request");

                                    mDeclineFriend.setVisibility(View.INVISIBLE);
                                    mDeclineFriend.setEnabled(false);

                                }
                            });

                        }
                    });

                }

                //---------------UNFRIEND---------------------------
                if(mCurrent_state.equals("friends")){

                    Map unfriendMap = new HashMap();

                    unfriendMap.put("Friends/" + mCurrentUser.getUid() + "/" + userId, null);
                    unfriendMap.put("Friends/" + userId + "/" + mCurrentUser.getUid(), null);

                    mDatabaseRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG);
                            }

                            else{

                                mCurrent_state = "not_friends";
                                mRequestFriend.setText("Send Friend Request");

                                Toast.makeText(ProfileActivity.this, "Request sent", Toast.LENGTH_LONG).show();

                            }

                            mRequestFriend.setEnabled(true);

                        }
                    });



                }

                //---------------REQ RECEIVED STATE ----------------
                if(mCurrent_state.equals("req_received")){

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();

                    friendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + userId + "/date", currentDate);
                    friendsMap.put("Friends/" + userId + "/" + mCurrentUser.getUid() + "/date", currentDate);

                    friendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + userId + "/balance", 0);
                    friendsMap.put("Friends/" + userId + "/" + mCurrentUser.getUid() + "/balance", 0);

                    friendsMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + userId + "/date", null);
                    friendsMap.put("Friend_req/" + userId + "/" + mCurrentUser.getUid() + "/date", null);

                    mDatabaseRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null){

                                mCurrent_state = "friends";
                                mRequestFriend.setText("Unfriend this person");
                                Toast.makeText(ProfileActivity.this, "Request sent", Toast.LENGTH_LONG).show();
                            }

                            else{
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG);
                            }

                            mRequestFriend.setEnabled(true);

                        }
                    });

                }




            }
        });


        //--------------------------------- DECLINE FRIEND BUTTON ---------------------------------
        mDeclineFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                //-------------------DECLINE FRIEND REQUEST---------------------
                if(mCurrent_state.equals("req_received")){

                    Map declineReqMap = new HashMap();

                    declineReqMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + userId, null);
                    declineReqMap.put("Friend_req/" + userId + "/" + mCurrentUser.getUid(), null);

                    mDatabaseRef.updateChildren(declineReqMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG);
                            }

                            else{

                                mCurrent_state = "not_friends";
                                Toast.makeText(ProfileActivity.this, "Request declined", Toast.LENGTH_LONG).show();

                            }

                            mDeclineFriend.setVisibility(View.INVISIBLE);
                            mDeclineFriend.setEnabled(false);

                        }
                    });



                }

            }
        });


    }
}
