package com.example.sitnik.onetoonechat;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import java.util.HashMap;
import java.util.Map;

/*** Class witch activity starts after adding group image.
 * In this step we are adding new members to group */
public class CreateGroupActivity extends AppCompatActivity {

    /*** layout */
    private Toolbar mToolbarGroup;
    private FloatingActionButton mAddToFriend;
    private Button mCreateGroup;
    private TextView mGroupName;
    private RecyclerView mRecyclerView;

    /*** database */
    private DatabaseReference mUsersDatabase; /*** reference to users database */
    private DatabaseReference mDatabaseRef; /*** reference to general database */
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    /*** other */
    private Query query; /*** querry to fill informations group members list */

    private String group_date; /*** group creation date */
    private String name_display; /*** group name */
    private String user_id; /*** group member id (depending on position on the list) */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        mToolbarGroup = findViewById(R.id.groups_appBar);
        setSupportActionBar(mToolbarGroup);
        getSupportActionBar().setTitle("Create Group");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        mCreateGroup = findViewById(R.id.btn_create_group);

        mAddToFriend = findViewById(R.id.btn_add_to_group);
        mAddToFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateGroupActivity.this, AddFriendToGroup.class);
                intent.putExtra("group_date", group_date);
                startActivity(intent);

            }
        });

        group_date = getIntent().getStringExtra("group_date");
        name_display = getIntent().getStringExtra("name_display");

        mGroupName = findViewById(R.id.tv_group_name);
        mGroupName.setText(name_display);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mRecyclerView = findViewById(R.id.list_group_members);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        query = FirebaseDatabase.getInstance().getReference().child("Groups").child(group_date).child("members");

        mCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateGroupActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }


    @Override
    /*** redreshing view holder on resume */
    protected void onResume() {
        super.onResume();

        displayViewHolder();

    }

    /*** filling view holder with data and displaying it in the list*/
    protected void displayViewHolder(){

        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(query, Friends.class)
                        .setLifecycleOwner(this)
                        .build();


        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> adapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
            @Override
            public FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                return new FriendsViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false));
            }

            @Override
            protected void onBindViewHolder(final FriendsViewHolder friendsViewHolder, int position, final Friends friends) {

                user_id = getRef(position).getKey();

                FloatingActionButton deleteButton = friendsViewHolder.mView.findViewById(R.id.btn_delete_user);
                deleteButton.setVisibility(View.VISIBLE);

                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (user_id.equals(mCurrentUserId)) {

                            Toast.makeText(CreateGroupActivity.this, "You can not remove yourself from the group.", Toast.LENGTH_LONG).show();

                        } else {

                        /*** map to delete user */
                        Map deleteMemberMap = new HashMap();

                        deleteMemberMap.put("Groups/" + group_date + "/members/" + user_id, null); // putting null value to delete member from database

                        mDatabaseRef.updateChildren(deleteMemberMap);
                    }
                    }
                });

                if(mUsersDatabase.child(user_id) != null) {
                    mUsersDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
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
                }
            }
        };

        mRecyclerView.setAdapter(adapter);


    }

}
