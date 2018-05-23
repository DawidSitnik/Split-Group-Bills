package com.example.sitnik.onetoonechat;

import android.content.Intent;
import android.graphics.Color;
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
import android.widget.EditText;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShareWithFriend extends AppCompatActivity {

    private Toolbar mToolbar;

    private RecyclerView mBillsList;

    private FloatingActionButton mAddBill;
    private Button mSettleUp;
    private CircleImageView mProfileImage;
    private TextView mWhoOwes;
    private TextView mName;
    private TextView mAmount;

    private View mMainView;

    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mDatabase;

    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    private Query query;

    private String who_ows;

    private int billAmount2;
    private String billWhoOws;
    private String billWhoLend;
    private String billPayedBy;
    private String billAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_with_friend);

        mToolbar = findViewById(R.id.users_appBar);
        setSupportActionBar(mToolbar);
        setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAddBill = findViewById(R.id.shareFriends_addBill);
        mSettleUp = findViewById(R.id.shareFriends_settleUp);
        mProfileImage = findViewById(R.id.shareFriends_image);
        mWhoOwes = findViewById(R.id.shareFriends_whoOwes);
        mAmount = findViewById(R.id.shareFriends_amount);
        mName = findViewById(R.id.shareFriends_friend);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mBillsList = findViewById(R.id.shareFriends_billsList);
        mBillsList.setHasFixedSize(true);
        mBillsList.setLayoutManager(new LinearLayoutManager(ShareWithFriend.this));

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUserId);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        final String user_id = getIntent().getStringExtra("user_id");

        //FILLING FRIEND INFO
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("Users").child(user_id).child("name").getValue().toString();
                String thumb_image = dataSnapshot.child("Users").child(user_id).child("thumb_image").getValue().toString();
                String amount = dataSnapshot.child("Friends").child(mCurrentUserId).child(user_id).child("balance").getValue().toString();
                String borrower = dataSnapshot.child("Friends").child(mCurrentUserId).child(user_id).child("borrower").getValue().toString();
                String friend_name = dataSnapshot.child("Users").child(user_id).child("name").getValue().toString();

                mName.setText(name);
                mAmount.setText(amount + " zl");

                if(borrower.equals("me")){
                    who_ows = "You owe " + friend_name;
                    mAmount.setTextColor(Color.parseColor("#FF5521"));
                }
                if(borrower.equals("friend")){
                    who_ows = friend_name + " ows you";
                    mAmount.setTextColor(Color.parseColor("#00ff00"));
                }

                mWhoOwes.setText(who_ows);

                Picasso.get().load(thumb_image).placeholder(R.drawable.default_avatar).into(mProfileImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mAddBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShareWithFriend.this, AddBill.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });


        //SETTING LIST OF BILLS
        query = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUserId).child(user_id).child("bills");

        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(query, Friends.class)
                        .setLifecycleOwner(this)
                        .build();


        FirebaseRecyclerAdapter<Friends, BillsViewHolder> adapter = new FirebaseRecyclerAdapter<Friends, BillsViewHolder>(options) {
            @Override
            public BillsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                return new BillsViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.bill_single_layout, parent, false));
            }

            @Override
            protected void onBindViewHolder(final BillsViewHolder billsViewHolder, int position, final Friends friends) {

                final String list_user_id = getRef(position).getKey();


                mDatabase.child("Friends").child(mCurrentUserId).child(user_id).child("bills").child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String billDescription = dataSnapshot.child("description").getValue().toString();
                        billPayedBy = dataSnapshot.child("payed_by").getValue().toString();
                        billAmount = dataSnapshot.child("amount").getValue().toString();

                        billAmount2 = Integer.parseInt(billAmount) / 2; //chyba bedzie w zaleznosci od wybranej opcji

                        billsViewHolder.setDescription(billDescription);
                        billsViewHolder.setWhoLend();
                        billsViewHolder.setWhoOws();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

//                billsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//                        Map addBillMap = new HashMap();
//
//                        Intent intent = new Intent(ShareWithFriend.this, ShareWithFriend.class);
//                        intent.putExtra("user_id", list_user_id);
//                        startActivity(intent);
//
//                    }
//                });
            }
        };


        mBillsList.setAdapter(adapter);



    }

    public class BillsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public BillsViewHolder(View itemView){
            super(itemView);
            mView = itemView;
        }

        public void setDescription(String description){
            TextView singleDescription = mView.findViewById(R.id.bill_single_description);
            singleDescription.setText(description);
        }

        public void setWhoOws (){

            TextView singleWhoOws = mView.findViewById(R.id.bill_single_ows);

            if(billPayedBy.equals("me")){
                billWhoOws = "You paid "+billAmount + "zl";
            }
            if(billPayedBy.equals("friend")){
                billWhoOws = "Your friend paid "+billAmount + "zl";
            }

            singleWhoOws.setText(billWhoOws);
        }

        public void setWhoLend(){

            TextView singleWhoLend = mView.findViewById(R.id.single_bill_whoLend);

            if(billPayedBy.equals("me")){
                billWhoLend = "You lent " + billAmount2 + "zl";
                singleWhoLend.setTextColor(Color.parseColor("#00FF00"));
            }
            if(billPayedBy.equals("friend")){
                billWhoLend = "You borrowed " + billAmount2 + "zl" ;
                singleWhoLend.setTextColor(Color.parseColor("#FF5521"));
            }
            singleWhoLend.setText(billWhoLend);
        }

    }


}
