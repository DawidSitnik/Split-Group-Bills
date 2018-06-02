package com.example.sitnik.onetoonechat;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/***filling groups list with users groups.*/
public class GroupsFragment extends Fragment {

    /***layout*/
    private RecyclerView mFriendList;
    private FloatingActionButton mAddFriendButton;
    private View mMainView;

    /***database*/
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    private Query query;

    /***required empty constructor*/
    public GroupsFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_groups, container, false);

        mAddFriendButton = mMainView.findViewById(R.id.btn_add_group);

        mAddFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AddGroupName.class);
                startActivity(intent);
            }
        });

        mFriendList = mMainView.findViewById(R.id.groups_list);
        mFriendList.setHasFixedSize(true);
        mFriendList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        query = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUserId).child("groups");

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
            /***taking data from database and filling view holder*/
            protected void onBindViewHolder(final FriendsViewHolder friendsViewHolder, int position, final Friends friends) {

                final String group_id = getRef(position).getKey();

                //HIDE DELETE BUTTON
                FloatingActionButton deleteButton = friendsViewHolder.mView.findViewById(R.id.btn_delete_user);
                deleteButton.setVisibility(View.INVISIBLE);


                //HIDE STATUS
                TextView hideStatus = friendsViewHolder.mView.findViewById(R.id.single_user_status);
                hideStatus.setVisibility(View.INVISIBLE);

                    mUsersDatabase.child(mCurrentUserId).child("groups").child(group_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String groupName = dataSnapshot.child("name").getValue().toString();
                            String groupThumbImage = dataSnapshot.child("thumb_image").getValue().toString();

                            friendsViewHolder.setThumbImage(groupThumbImage);
                            friendsViewHolder.setName(groupName);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    Intent intent = new Intent (getContext(), ShareWithGroup.class);
                    intent.putExtra("group_id", group_id);
                    startActivity(intent);

                    }
                });
            }
        };

        mFriendList.setAdapter(adapter);

        return mMainView;
    }

}