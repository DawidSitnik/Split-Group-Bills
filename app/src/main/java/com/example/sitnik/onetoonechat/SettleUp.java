package com.example.sitnik.onetoonechat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettleUp extends AppCompatActivity {

    private Toolbar mToolbar;

    private Button mSettleUp;
    private CircleImageView mLenderImage;
    private CircleImageView mBorrowerImage;
    private TextView mWhoPaid;
    private EditText mAmount;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    private String borrower_thumb_image, lender_thumb_image, whoPaid, balance, borrower, date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settle_up);

        mToolbar = findViewById(R.id.users_appBar);
        setSupportActionBar(mToolbar);
        setTitle("Settle Up");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSettleUp = findViewById(R.id.settleUp_submit);
        mLenderImage = findViewById(R.id.settleUp_lender);
        mBorrowerImage = findViewById(R.id.settleUp_borrower);
        mWhoPaid = findViewById(R.id.settleUp_whoPaid);
        mAmount = findViewById(R.id.settleUp_amount);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        final String user_id = getIntent().getStringExtra("user_id");

        mSettleUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                HashMap settleUpMap = new HashMap();

                double balanceInt = Double.parseDouble(balance);
                double settleUpAmount = Double.parseDouble(mAmount.getText().toString());
                date = DateFormat.getDateTimeInstance().format(new Date());

                if(borrower.equals("me")){

                    settleUpMap.put("Friends/" + mCurrentUserId + "/" + user_id + "/bills/" + date + "/payed_by", "me");
                    settleUpMap.put("Friends/" + user_id + "/" + mCurrentUserId + "/bills/" + date + "/payed_by", "friend");
                }
                if(borrower.equals("friend")) {

                    settleUpMap.put("Friends/" + mCurrentUserId + "/" + user_id + "/bills/" + date + "/payed_by", "friend");
                    settleUpMap.put("Friends/" + user_id + "/" + mCurrentUserId + "/bills/" + date + "/payed_by", "me");
                }

                if (settleUpAmount > balanceInt){ //if we settled up more than total balance

                    if(borrower.equals("me")){
                        settleUpMap.put("Friends/" + mCurrentUserId + "/" + user_id + "/borrower", "friend");
                        settleUpMap.put("Friends/" + user_id + "/" + mCurrentUserId + "/borrower", "me");

                    }
                    if(borrower.equals("friend")){
                        settleUpMap.put("Friends/" + mCurrentUserId + "/" + user_id + "/borrower", "me");
                        settleUpMap.put("Friends/" + user_id + "/" + mCurrentUserId + "/borrower", "friend");

                    }

                    balanceInt = - (balanceInt - settleUpAmount);
                }

                if (settleUpAmount <= balanceInt){
                    balanceInt = balanceInt - settleUpAmount;
                }


                settleUpMap.put("Friends/" + mCurrentUserId + "/" + user_id + "/balance", balanceInt);
                settleUpMap.put("Friends/" + user_id + "/" + mCurrentUserId + "/balance", balanceInt);

                settleUpMap.put("Friends/" + mCurrentUserId + "/" + user_id + "/bills/" + date + "/amount", mAmount.getText().toString());
                settleUpMap.put("Friends/" + user_id + "/" + mCurrentUserId + "/bills/" + date + "/amount", mAmount.getText().toString());

                settleUpMap.put("Friends/" + mCurrentUserId + "/" + user_id + "/bills/" + date + "/description", "settle_up");
                settleUpMap.put("Friends/" + user_id + "/" + mCurrentUserId + "/bills/" + date + "/description", "settle_up");


                mDatabase.updateChildren(settleUpMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                        if (databaseError != null) {
                            String error = databaseError.getMessage();
                            Toast.makeText(SettleUp.this, error, Toast.LENGTH_LONG);
                        } else {

                            Toast.makeText(SettleUp.this, "Settled up.", Toast.LENGTH_LONG).show();
                        }


                    }
                });

                finish();
            }
        });

        //FILLING IMAGES, TEXT VIEW
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                balance = dataSnapshot.child("Friends").child(mCurrentUserId).child(user_id).child("balance").getValue().toString();

                String friend_name = dataSnapshot.child("Users").child(user_id).child("name").getValue().toString();
                borrower = dataSnapshot.child("Friends").child(mCurrentUserId).child(user_id).child("borrower").getValue().toString();

                if(borrower.equals("me")){ //we are a borrower
                    borrower_thumb_image = dataSnapshot.child("Users").child(mCurrentUserId).child("thumb_image").getValue().toString();
                    lender_thumb_image = dataSnapshot.child("Users").child(user_id).child("thumb_image").getValue().toString();
                    whoPaid = "You paid " + friend_name +":";
                }
                if(borrower.equals("friend")){// friend is a borrower
                    borrower_thumb_image = dataSnapshot.child("Users").child(user_id).child("thumb_image").getValue().toString();
                    lender_thumb_image = dataSnapshot.child("Users").child(mCurrentUserId).child("thumb_image").getValue().toString();
                    whoPaid = friend_name + " paid You:";
                }

                Picasso.get().load(lender_thumb_image).placeholder(R.drawable.default_avatar).into(mLenderImage);
                Picasso.get().load(borrower_thumb_image).placeholder(R.drawable.default_avatar).into(mBorrowerImage);
                mWhoPaid.setText(whoPaid);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }
}
