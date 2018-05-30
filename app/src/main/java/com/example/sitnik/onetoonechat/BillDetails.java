package com.example.sitnik.onetoonechat;

import android.content.Intent;

import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class BillDetails extends AppCompatActivity {

    private CircleImageView mImage;
    private FloatingActionButton mOptions;
    private TextView mDescription;
    private TextView mAmount;
    private TextView mDate;
    private TextView mDetail1;
    private TextView mDetail2;
    private Button mDelete;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    private String billPayedBy, billAmount, billDescription, friendName, yourName, user_id, bill_date, detail1, detail2, balance, borrower, splitting_type;
    private double billAmount2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_details);

        mImage = findViewById(R.id.billDetails_image);
        mOptions = findViewById(R.id.billDetails_options);
        mDescription = findViewById(R.id.billDetails_description);
        mAmount = findViewById(R.id.billDetails_amount);
        mDate = findViewById(R.id.billDetails_date);
        mDetail1 = findViewById(R.id.billDetails_detail1);
        mDetail2 = findViewById(R.id.billDetails_detail2);
        mDelete = findViewById(R.id.billDetails_delete);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        bill_date = getIntent().getStringExtra("bill_date");
        user_id = getIntent().getStringExtra("user_id");
        splitting_type = getIntent().getStringExtra("splitting_type");

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                    balance = dataSnapshot.child("Friends").child(mCurrentUserId).child(user_id).child("balance").getValue().toString();
                    borrower = dataSnapshot.child("Friends").child(mCurrentUserId).child(user_id).child("borrower").getValue().toString();

                    if (dataSnapshot.child("Friends").child(mCurrentUserId).child(user_id).child("bills").child(bill_date).exists())
                    {
                    billDescription = dataSnapshot.child("Friends").child(mCurrentUserId).child(user_id).child("bills").child(bill_date).child("description").getValue().toString();
                    billPayedBy = dataSnapshot.child("Friends").child(mCurrentUserId).child(user_id).child("bills").child(bill_date).child("payed_by").getValue().toString();
                    billAmount = dataSnapshot.child("Friends").child(mCurrentUserId).child(user_id).child("bills").child(bill_date).child("amount").getValue().toString();

                    if (splitting_type.equals("you_owe_the_full_amount") || splitting_type.equals("they_owe_the_full_amount")) {
                        billAmount2 = Double.parseDouble(billAmount);
                    } else {
                        billAmount2 = Double.parseDouble(billAmount) / 2;
                    }
                    friendName = dataSnapshot.child("Users").child(user_id).child("name").getValue().toString();
                    yourName = dataSnapshot.child("Users").child(mCurrentUserId).child("name").getValue().toString();

                    if (billDescription.equals("settle_up")) {
                        mImage.setImageResource(R.drawable.settle_up);
                    }
                    if (!billDescription.equals("settle_up")) {
                        mImage.setImageResource(R.drawable.bill);
                    }

                    if (billPayedBy.equals("me")) {
                        detail1 = yourName + " paid " + billAmount + "zl and owes " + billAmount2 + "zl";
                        detail2 = friendName + " owes " + billAmount2 + "zl";
                    }
                    if (billPayedBy.equals("friend")) {
                        detail1 = friendName + " paid " + billAmount + "zl and owes " + billAmount2 + "zl";
                        detail2 = yourName + " owes " + billAmount2 + "zl";
                    }

                    mDetail1.setText(detail1);
                    mDetail2.setText(detail2);
                    mDescription.setText(billDescription);
                    mAmount.setText(billAmount + "zl");
                    mDate.setText(bill_date);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap deleteMap = new HashMap();
                deleteMap.put("Friends/" + mCurrentUserId + "/" + user_id + "/bills/" + bill_date, null);
                deleteMap.put("Friends/" + user_id + "/" + mCurrentUserId + "/bills/" + bill_date, null);

                double balanceDouble = Double.parseDouble(balance);
                double amountDouble = Double.parseDouble(billAmount) / 2;

                if((borrower.equals("me") && billPayedBy.equals("me")) || (borrower.equals("friend") && billPayedBy.equals("friend"))) { //if we are a borrower
                    balanceDouble = balanceDouble + amountDouble;
                }

                if((borrower.equals("me") && billPayedBy.equals("friend")) || (borrower.equals("friend") && billPayedBy.equals("me"))){

                    balanceDouble = balanceDouble - amountDouble;

                    if (amountDouble > balanceDouble){
                        if(borrower.equals("me")) {
                            deleteMap.put("Friends/" + mCurrentUserId + "/" + user_id + "/borrower", "friend");
                            deleteMap.put("Friends/" + user_id + "/" + mCurrentUserId + "/borrower", "me");
                        }
                        if(borrower.equals("friend")){
                            deleteMap.put("Friends/" + mCurrentUserId + "/" + user_id + "/borrower", "me");
                            deleteMap.put("Friends/" + user_id + "/" + mCurrentUserId + "/borrower", "friend");
                        }
                        balanceDouble = -balanceDouble;
                    }
                }

                deleteMap.put("Friends/" + mCurrentUserId + "/" + user_id + "/balance", balanceDouble);
                deleteMap.put("Friends/" + user_id + "/" + mCurrentUserId + "/balance", balanceDouble);

                mDatabase.updateChildren(deleteMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                        if (databaseError != null) {
                            String error = databaseError.getMessage();
                            Toast.makeText(BillDetails.this, error, Toast.LENGTH_LONG);
                        } else {

                            Toast.makeText(BillDetails.this, "Bill deleted.", Toast.LENGTH_LONG).show();
                        }


                    }
                });

                finish();

            }
        });

    }
}
