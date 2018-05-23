package com.example.sitnik.onetoonechat;

import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddFriendToGroup extends AppCompatActivity {

    private RecyclerView mFriendList;

    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mDatabaseRef;

    private FirebaseAuth mAuth;

    private String mCurrentUserId;

    private Query query;

    private TextView mGroupName;

    private CircleImageView mUserImage;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend_to_group);

        final String group_name = getIntent().getStringExtra("group_name");

        mGroupName = findViewById(R.id.tv_group_name);
        mGroupName.setText(group_name);

        mFriendList = findViewById(R.id.friend_to_group_list);
        mFriendList.setHasFixedSize(true);
        mFriendList.setLayoutManager(new LinearLayoutManager(AddFriendToGroup.this));

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUserId);
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        query = FirebaseDatabase.getInstance().getReference().child("Friends");

        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(query, Friends.class)
                        .setLifecycleOwner(this)
                        .build();


        FirebaseRecyclerAdapter<Friends, FriendsFragment.FriendsViewHolder> adapter = new FirebaseRecyclerAdapter<Friends, FriendsFragment.FriendsViewHolder>(options) {
            @Override
            public FriendsFragment.FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                return new FriendsFragment.FriendsViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false));
            }

            @Override
            protected void onBindViewHolder(final FriendsFragment.FriendsViewHolder friendsViewHolder, int position, final Friends friends) {

                final String list_user_id = getRef(position).getKey();
                final String user_id = getRef(position).getKey();

                FloatingActionButton deleteButton = friendsViewHolder.mView.findViewById(R.id.btn_delete_user);
                deleteButton.setVisibility(View.INVISIBLE);

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                        String userStatus = dataSnapshot.child("status").getValue().toString();

                        friendsViewHolder.setName(userName);
                        friendsViewHolder.setStatus(userStatus);
                        friendsViewHolder.setThumbImage(userThumb);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                mUserImage = friendsViewHolder.mView.findViewById(R.id.user_single_image);
                mUserImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        mDatabaseRef.child("Groups").child(group_name).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                                String group_image = dataSnapshot.child("image").getValue().toString();
                                String group_thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                                Map memberMap = new HashMap();
                                memberMap.put("Groups/" + group_name + "/members/" + user_id + "/date", currentDate  );
                                memberMap.put("Users/"+ user_id + "/groups/" + group_name + "/role", "member");
                                memberMap.put("Users/" + user_id + "/groups/" + group_name + "/image", group_image);
                                memberMap.put("Users/" + user_id + "/groups/" + group_name + "/thumb_image", group_thumb_image);


                                mDatabaseRef.updateChildren(memberMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                        if(databaseError != null){
                                            String error = databaseError.getMessage();
                                            Toast.makeText(AddFriendToGroup.this, error, Toast.LENGTH_LONG);
                                        }

                                        else{

                                            Toast.makeText(AddFriendToGroup.this, "Friend added to group", Toast.LENGTH_LONG).show();
                                        }


                                    }
                                });

                                finish();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }
                });
            }
        };


        mFriendList.setAdapter(adapter);


    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public FriendsViewHolder(View itemView){
            super(itemView);
            mView = itemView;

        }


        public void setName(String name){
            TextView userNameView = mView.findViewById(R.id.single_user_name);
            userNameView.setText(name);

        }

        public void setStatus(String status){
            TextView userStatusView = mView.findViewById(R.id.single_user_status);
            userStatusView.setText(status);
        }

        public void setThumbImage(String thumb_image){
            CircleImageView userImageView = mView.findViewById(R.id.user_single_image);
            Picasso.get().load(thumb_image).placeholder(R.drawable.default_avatar).into(userImageView);
        }



    }

}
