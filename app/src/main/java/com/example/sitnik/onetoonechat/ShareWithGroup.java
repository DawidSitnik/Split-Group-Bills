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
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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

public class ShareWithGroup extends AppCompatActivity {

    /*** Layout**/
    private Toolbar mToolbar;
    private RecyclerView mBillsList;
    private FloatingActionButton mAddBill;
    private Button mSettleUp;
    private CircleImageView mProfileImage;
    private TextView mWhoOwes;
    private TextView mName;
    private TextView mAmount;

    /*** Database**/
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    private Query query; /*** querry to fill list of bills**/


    private String billPayedBy; /*** id of user who paid the bill**/
    private String billPayedByName; /*** name of user who paid the bill**/
    public static String billAmount; /*** amount of bill, value passed to GroupBillViewHolder**/
    private String yourBalance; /*** Users payoff with the group**/
    private String name; /*** name of the group**/
    private String splitting_type;
    private String bill_date; /*** date of single bill creation**/
    private String billUserAmount; /*** amount that current user paid**/

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
        billPayedBy = "";
        billUserAmount = "0";


        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mBillsList = findViewById(R.id.shareGroup_billsList);
        mBillsList.setHasFixedSize(true);
        mBillsList.setLayoutManager(new LinearLayoutManager(ShareWithGroup.this));

        mDatabase = FirebaseDatabase.getInstance().getReference();

        final HashMap hashMap = new HashMap(); /*** hash map to update database with default bill data**/

        final String group_id = getIntent().getStringExtra("group_id"); /*** group creation date**/


        //SETTLE UP LISTENER
        mSettleUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Intent intent = new Intent(ShareWithFriend.this, SettleUp.class);
//                intent.putExtra("user_id", user_id);
//                startActivity(intent);
            }
        });

        //GROUP
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                yourBalance = dataSnapshot.child("Users").child(mCurrentUserId).child("groups").child(group_id).child("balance").child("amount").getValue().toString();
                name = dataSnapshot.child("Groups").child(group_id).child("name").getValue().toString();
                String thumb_image = dataSnapshot.child("Groups").child(group_id).child("thumb_image").getValue().toString();

                for (DataSnapshot addressSnapshot: dataSnapshot.child("Groups").child(group_id).child("members").getChildren()) {

                    hashMap.put("Groups/" + group_id + "/bills/" + bill_date + "/split_with/" + addressSnapshot.getKey(), "0");

                }
                mName.setText(name);
                mAmount.setText(yourBalance + " zl");
                Picasso.get().load(thumb_image).placeholder(R.drawable.default_avatar).into(mProfileImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mAddBill.setOnClickListener(new View.OnClickListener() {
            @Override
            /*** Default bill to database adding. **/
            public void onClick(View v) {

                final Intent intent = new Intent(ShareWithGroup.this, AddBillGroup.class);
                intent.putExtra("group_name_display", name);
                intent.putExtra("group_id", group_id);
                intent.putExtra("bill_date", bill_date);

                hashMap.put("Groups/" + group_id + "/bills/" + bill_date + "/splitting_type", "equally");
                hashMap.put("Groups/" + group_id + "/bills/" + bill_date + "/paid_by", mCurrentUserId);
                hashMap.put("Groups/" + group_id + "/bills/" + bill_date + "/description", "default");
                hashMap.put("Groups/" + group_id + "/bills/" + bill_date + "/amount", 0);

                mDatabase.updateChildren(hashMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                        if (databaseError != null) {
                            String error = databaseError.getMessage();
                            Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG);
                        } else {

                            Toast.makeText(getApplicationContext(), "Starting bill.", Toast.LENGTH_LONG).show();
                            startActivity(intent);
                            finish();
                        }
                    }
                });
            }
        });


        //BILLS LIST
        /**
         * Preparing view holder for bills list
         */
        query = FirebaseDatabase.getInstance().getReference().child("Groups").child(group_id).child("bills");

        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(query, Friends.class)
                        .setLifecycleOwner(this)
                        .build();


        FirebaseRecyclerAdapter<Friends, GroupBillViewHolder> adapter = new FirebaseRecyclerAdapter<Friends, GroupBillViewHolder>(options) {
            @Override
            public GroupBillViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                return new GroupBillViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.bill_single_layout, parent, false));
            }

            @Override
            protected void onBindViewHolder(final GroupBillViewHolder billsViewHolder, int position, final Friends friends) {

                final String list_bill_date = getRef(position).getKey();


                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.child("Groups").child(group_id).child("bills").child(list_bill_date).exists() ) {
                            String billDescription = dataSnapshot.child("Groups").child(group_id).child("bills").child(list_bill_date).child("description").getValue().toString();
                            billPayedBy = dataSnapshot.child("Groups").child(group_id).child("bills").child(list_bill_date).child("paid_by").getValue().toString();
                            billAmount = dataSnapshot.child("Groups").child(group_id).child("bills").child(list_bill_date).child("amount").getValue().toString();
                            splitting_type = dataSnapshot.child("Groups").child(group_id).child("bills").child(list_bill_date).child("splitting_type").getValue().toString();
                            if(dataSnapshot.child("Groups").child(group_id).child("bills").child(list_bill_date).child("split_with").child(mCurrentUserId).exists() ) {
                                billUserAmount = dataSnapshot.child("Groups").child(group_id).child("bills").child(list_bill_date).child("split_with").child(mCurrentUserId).getValue().toString();
                                billsViewHolder.setYourPayoff(billUserAmount);
                            }else billsViewHolder.setYourPayoff("0");
                            billPayedByName = dataSnapshot.child("Users").child(billPayedBy).child("name").getValue().toString();

                            billsViewHolder.setWhoPaid(billPayedByName);

                            billsViewHolder.setDescription(billDescription);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                billsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

//                        Intent intent = new Intent (ShareWithFriend.this, BillDetails.class);
//                        intent.putExtra("bill_date", list_bill_date);
//                        intent.putExtra("user_id", group_id);
//                        intent.putExtra("splitting_type", splitting_type);
//                        startActivity(intent);

                    }
                });
            }
        };

        mBillsList.setAdapter(adapter);

    }

    /**
     * Updating database after adding bill
     */
    @Override
    protected void onResume() {
        super.onResume();
        mDatabase.updateChildren(AddBillGroup.splitEquallyMap);
    }



}
