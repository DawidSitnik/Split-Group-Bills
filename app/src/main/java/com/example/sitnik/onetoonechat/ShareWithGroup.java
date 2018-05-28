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


import java.text.DateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShareWithGroup extends AppCompatActivity {

    private Toolbar mToolbar;

    private RecyclerView mBillsList;

    private FloatingActionButton mAddBill;
    private Button mSettleUp;
    private CircleImageView mProfileImage;
    private TextView mWhoOwes;
    private TextView mName;
    private TextView mAmount;

    private DatabaseReference mDatabase;

    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    private Query query;

    private double billAmount2;
    private String who_ows;
    private String billWhoOws;
    private String billWhoLend;
    private String billPayedBy;
    private String billAmount;
    private String yourBalance;
    private String name;
    private String borrower;
    private String splitting_type;
    private String bill_date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_with_group);

        mToolbar = findViewById(R.id.users_appBar);
        setSupportActionBar(mToolbar);
        setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAddBill = findViewById(R.id.shareGroup_addBill);
        mSettleUp = findViewById(R.id.shareGroup_settleUp);
        mProfileImage = findViewById(R.id.shareGroup_image);
        mWhoOwes = findViewById(R.id.shareGroup_info);
        mAmount = findViewById(R.id.shareGroup_amount);
        mName = findViewById(R.id.shareGroup_name);

        bill_date = DateFormat.getDateTimeInstance().format(new Date());

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mBillsList = findViewById(R.id.shareGroup_billsList);
        mBillsList.setHasFixedSize(true);
        mBillsList.setLayoutManager(new LinearLayoutManager(ShareWithGroup.this));

        mDatabase = FirebaseDatabase.getInstance().getReference();

        final String group_id = getIntent().getStringExtra("group_id");


        //SETTLE UP LISTENER
        mSettleUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Intent intent = new Intent(ShareWithFriend.this, SettleUp.class);
//                intent.putExtra("user_id", user_id);
//                startActivity(intent);
            }
        });

        //FILLING FRIEND INFO
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                yourBalance = dataSnapshot.child("Users").child(mCurrentUserId).child("groups").child(group_id).child("balance").child("amount").getValue().toString();
                borrower = dataSnapshot.child("Users").child(mCurrentUserId).child("groups").child(group_id).child("balance").child("borrower").getValue().toString();
                name = dataSnapshot.child("Groups").child(group_id).child("name").getValue().toString();
                String thumb_image = dataSnapshot.child("Groups").child(group_id).child("thumb_image").getValue().toString();
//                String amount = dataSnapshot.child("Friends").child(mCurrentUserId).child(group_id).child("balance").getValue().toString();
//                balance = dataSnapshot.child("Groups").child(group_id).child("balance").getValue().toString();
//                borrower = dataSnapshot.child("Friends").child(mCurrentUserId).child(group_id).child("borrower").getValue().toString();
//                String friend_name = dataSnapshot.child("Users").child(group_id).child("name").getValue().toString();

                mName.setText(name);
                mAmount.setText(yourBalance + " zl");

//                if(borrower.equals("me")){
//                    who_ows = "You owe " + friend_name;
//                    mAmount.setTextColor(Color.parseColor("#FF5521"));
//                }
//                if(borrower.equals("friend")){
//                    who_ows = friend_name + " ows you";
//                    mAmount.setTextColor(Color.parseColor("#00ff00"));
//                }
//
//                mWhoOwes.setText(who_ows);

                Picasso.get().load(thumb_image).placeholder(R.drawable.default_avatar).into(mProfileImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mAddBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                Intent intent = new Intent(ShareWithGroup.this, AddBillGroup.class);
                intent.putExtra("group_name_display", name);
                intent.putExtra("group_id", group_id);
                intent.putExtra("bill_date", bill_date);
                startActivity(intent);

            }
        });


        //BILLS LIST
//        query = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUserId).child(group_id).child("bills");
//
//        FirebaseRecyclerOptions<Friends> options =
//                new FirebaseRecyclerOptions.Builder<Friends>()
//                        .setQuery(query, Friends.class)
//                        .setLifecycleOwner(this)
//                        .build();
//
//
//        FirebaseRecyclerAdapter<Friends, BillsViewHolder> adapter = new FirebaseRecyclerAdapter<Friends, BillsViewHolder>(options) {
//            @Override
//            public BillsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//
//                return new BillsViewHolder(LayoutInflater.from(parent.getContext())
//                        .inflate(R.layout.bill_single_layout, parent, false));
//            }
//
//            @Override
//            protected void onBindViewHolder(final BillsViewHolder billsViewHolder, int position, final Friends friends) {
//
//                final String list_user_id = getRef(position).getKey();
//
//
//                mDatabase.child("Friends").child(mCurrentUserId).child(group_id).child("bills").child(list_user_id).addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//
//                        String billDescription = dataSnapshot.child("description").getValue().toString();
//                        billPayedBy = dataSnapshot.child("payed_by").getValue().toString();
//                        billAmount = dataSnapshot.child("amount").getValue().toString();
//                        splitting_type = dataSnapshot.child("splitting_type").getValue().toString();
//
//                        if(splitting_type.equals("you_owe_the_full_amount") || splitting_type.equals("they_owe_the_full_amount")){
//                            billAmount2 = Double.parseDouble(billAmount);
//                        }else{
//                            billAmount2 = Double.parseDouble(billAmount) / 2;
//                        }
//
//
//
//
//                        billsViewHolder.setWhoOws();
//
//                        if(billDescription.equals("settle_up")){
//                            billsViewHolder.setWhoLendEmpty();
//                            billsViewHolder.setDescription("Settle Up");
//                            billsViewHolder.setSettleUpImage();
//                        }
//
//                        if(!billDescription.equals("settle_up")){
//
//                            billsViewHolder.setWhoLend();
//                            billsViewHolder.setDescription(billDescription);
//                            billsViewHolder.setDefaultImage();
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//
//                billsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//                        Intent intent = new Intent (ShareWithFriend.this, BillDetails.class);
//                        intent.putExtra("bill_date", list_user_id);
//                        intent.putExtra("user_id", group_id);
//                        intent.putExtra("splitting_type", splitting_type);
//                        startActivity(intent);
//
//                    }
//                });
//            }
//        };
//
//        mBillsList.setAdapter(adapter);

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

        public void setSettleUpImage(){
            CircleImageView mImage = mView.findViewById(R.id.bill_single_image);
            mImage.setImageResource(R.drawable.settle_up);
        }

        public void setDefaultImage() {
            CircleImageView mImage = mView.findViewById(R.id.bill_single_image);
            mImage.setImageResource(R.drawable.bill);
        }

        public void setWhoLendEmpty(){
            TextView singleWhoLend = mView.findViewById(R.id.single_bill_whoLend);
            singleWhoLend.setText("");
        }
    }


}
