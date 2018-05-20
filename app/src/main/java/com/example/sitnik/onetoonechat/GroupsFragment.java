package com.example.sitnik.onetoonechat;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class GroupsFragment extends Fragment {

    private RecyclerView mFriendList;

    private FloatingActionButton mAddFriendButton;

    private View mMainView;

    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;

    private String mCurrentUserId;

    private Query query;

    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

// DOKONCZYC TEN ADAPTER, SKONCZYLEM DOPIERO NA DODANIU Child "groups"

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

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUserId).child("groups");
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

                friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                        profileIntent.putExtra("user_id", list_user_id);
                        startActivity(profileIntent);

                    }
                });
            }
        };


        mFriendList.setAdapter(adapter);

        return mMainView;
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