package com.example.sitnik.onetoonechat;

import android.app.ProgressDialog;
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

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/*** Updates database with new group member and default data*/
public class AddFriendToGroup extends AppCompatActivity {

    /***layout */
    private RecyclerView mFriendList;
    private TextView mGroupName;
    private CircleImageView mUserImage;

    /***database */
    private DatabaseReference mFriendsDatabase; /***reference to friends database*/
    private DatabaseReference mUsersDatabase; /***reference to users database */
    private DatabaseReference mDatabaseRef; /***general reference to database*/
    private FirebaseAuth mAuth; /***user authentication*/

    private String mCurrentUserId; /***id of current user*/

    private Query query; /***querry to populate FriendList*/

    private String group_image; /***name of user image from database*/
    private String group_thumb_image; /***name of user thumb image from databse*/

    private ProgressDialog mAddingFriendProgress; /***loading dialog, starts after sending querry to db, closes after succesful querry sending*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend_to_group);

        final String group_date = getIntent().getStringExtra("group_date"); /***date of group creation*/

        mGroupName = findViewById(R.id.tv_group_name);
        mGroupName.setText(group_date);

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

        mAddingFriendProgress = new ProgressDialog(this);

        mDatabaseRef.child("Groups").child(group_date).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                group_image = dataSnapshot.child("image").getValue().toString();
                group_thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        query = FirebaseDatabase.getInstance().getReference().child("Friends");

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

                        mAddingFriendProgress.setTitle("Add Friend");
                        mAddingFriendProgress.setMessage("Adding friend to the group");
                        mAddingFriendProgress.setCanceledOnTouchOutside(false);
                        mAddingFriendProgress.show();
                        Map memberMap = new HashMap();
                        memberMap.put("Groups/" + group_date + "/members/" + user_id + "/date", "aaa"  );
                        memberMap.put("Users/"+ user_id + "/groups/" + group_date + "/role", "member");
                        memberMap.put("Users/" + user_id + "/groups/" + group_date + "/image", group_image);
                        memberMap.put("Users/" + user_id + "/groups/" + group_date + "/thumb_image", group_thumb_image);
                        memberMap.put("Users/" + user_id + "/groups/" + group_date + "/balance/amount", 0);
                        memberMap.put("Users/" + user_id + "/groups/" + group_date + "/balance/borrower", "none");

                        mDatabaseRef.updateChildren(memberMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                if(databaseError != null){
                                    String error = databaseError.getMessage();
                                    Toast.makeText(AddFriendToGroup.this, error, Toast.LENGTH_LONG);
                                    mAddingFriendProgress.hide();
                                }

                                else{

                                    Toast.makeText(AddFriendToGroup.this, "Friend added to group", Toast.LENGTH_LONG).show();
                                    mAddingFriendProgress.dismiss();
                                    finish();
                                }
                            }
                        });
                    }
                });
            }
        };
        mFriendList.setAdapter(adapter);
    }
}
