package com.example.sitnik.onetoonechat;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private RecyclerView mRecyclerView;

    private DatabaseReference databaseReference;

    private Query query;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);


        mToolbar = findViewById(R.id.users_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        mRecyclerView = findViewById(R.id.users_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        query = FirebaseDatabase.getInstance().getReference().child("Users").limitToLast(50);

        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(query, Users.class)
                        .setLifecycleOwner(this)
                        .build();


        FirebaseRecyclerAdapter <Users, UsersViewHolder> adapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {
            @Override
            public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                return new UsersViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false));
            }

            @Override
            protected void onBindViewHolder(UsersViewHolder usersViewHolder, int position, Users users) {

                usersViewHolder.setName(users.getName());
                usersViewHolder.setStatus(users.getStatus());
                usersViewHolder.setThumbImage(users.getThumb_image());

                final String user_id = getRef(position).getKey();

                usersViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent profileIntent = new Intent(UsersActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("user_id", user_id);
                        startActivity(profileIntent);

                    }
                });
            }
        };


        mRecyclerView.setAdapter(adapter);


    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public UsersViewHolder(View itemView){
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
