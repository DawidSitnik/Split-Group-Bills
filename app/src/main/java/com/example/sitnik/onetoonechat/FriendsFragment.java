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

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class FriendsFragment extends Fragment {

    private RecyclerView mFriendList;

    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mDatabase;

    private FirebaseAuth mAuth;

    private String mCurrentUserId;

    private View mMainView;

    private Query query;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);

        mFriendList = mMainView.findViewById(R.id.friends_list);
        mFriendList.setHasFixedSize(true);
        mFriendList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUserId);
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        query = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUserId);

        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(query, Friends.class)
                        .setLifecycleOwner(this)
                        .build();


        FirebaseRecyclerAdapter<Friends, FriendsFragment.FriendsViewHolder> adapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
            @Override
            public FriendsFragment.FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                return new FriendsViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false));
            }

            @Override
            protected void onBindViewHolder(final FriendsFragment.FriendsViewHolder friendsViewHolder, int position, final Friends friends) {

                final String list_user_id = getRef(position).getKey();

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

                friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Map addBillMap = new HashMap();

                        Intent intent = new Intent(getContext(), ShareWithFriend.class);
                        intent.putExtra("user_id", list_user_id);
                        startActivity(intent);

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
