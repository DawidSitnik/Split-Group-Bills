package com.example.sitnik.onetoonechat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Map;

public class AddBill extends AppCompatActivity {

    private Toolbar mToolbar;

    private EditText mDescription;
    private EditText mAmount;

    private Button mAdd;

    private DatabaseReference mDatabase;

    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    private String balance;
    private String borrower;
    private String amount;
    private String description;
    private String user_id;
    private String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bill);

        mToolbar = findViewById(R.id.users_appBar);
        setSupportActionBar(mToolbar);
        setTitle("Add Bill");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDescription = findViewById(R.id.addBill_description);
        mAmount = findViewById(R.id.addBill_amount);

        mAdd = findViewById(R.id.addBill_add);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        user_id = getIntent().getStringExtra("user_id");

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                balance = dataSnapshot.child("Friends").child(mCurrentUserId).child(user_id).child("balance").getValue().toString();

                //tutaj musimy zrobic switch case w zaleznosci od opcji
                //take borrower only if it is !0 ,in other case we will type borrower manually depending on spliting option
                if(!balance.equals("0")){
                    borrower = dataSnapshot.child("Friends").child(mCurrentUserId).child(user_id).child("borrower").getValue().toString();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //PAID BY ME SPLIT EQUALLY

                description = mDescription.getText().toString();
                amount = mAmount.getText().toString();

                if(mDescription.getText().toString().replaceAll("\\s+","").equals("")){
                    Toast.makeText(AddBill.this, "Enter Description", Toast.LENGTH_LONG).show();
                    mDescription.setText("");
                }

                if(mAmount.getText().toString().replaceAll("\\s+","").equals("")){
                    Toast.makeText(AddBill.this, "Enter Amount", Toast.LENGTH_LONG).show();
                    mAmount.setText("");
                }

                else {

                    payedByMeSplitEqualy();



                    finish();
                }


            }
        });



    }

    private void payedByMeSplitEqualy(){

        if(balance.equals("0")){
            Map startMap = new HashMap();
            startMap.put("Friends/" + mCurrentUserId + "/" + user_id + "/borrower", "friend");
            startMap.put("Friends/" + user_id + "/" + mCurrentUserId + "/borrower", "me");

            borrower = "me";

            mDatabase.updateChildren(startMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if (databaseError != null) {
                        String error = databaseError.getMessage();
                        Toast.makeText(AddBill.this, error, Toast.LENGTH_LONG);
                    } else {

                        Toast.makeText(AddBill.this, "Borrower added.", Toast.LENGTH_LONG).show();
                    }


                }
            });
        }


        int balanceInt = Integer.parseInt(balance);
        int amountInt = Integer.parseInt(amount);

        Map addBillMap = new HashMap();

        if(borrower.equals("me")){ //if we paied more than we owed we are changing a borrower
            balanceInt = balanceInt - amountInt;

            if (amountInt > balanceInt){
                addBillMap.put("Friends/" + mCurrentUserId + "/" + user_id + "/borrower", "friend");
                addBillMap.put("Friends/" + user_id + "/" + mCurrentUserId + "/borrower", "me");
                balanceInt = -balanceInt;
            }
        }
        else {
            balanceInt = balanceInt + amountInt;
        }

        date = DateFormat.getDateTimeInstance().format(new Date());
        addBillMap.put("Friends/" + mCurrentUserId + "/" + user_id + "/bills/" + date + "/amount", amount);
        addBillMap.put("Friends/" + user_id + "/" + mCurrentUserId + "/bills/" + date + "/amount", amount);

        addBillMap.put("Friends/" + mCurrentUserId + "/" + user_id + "/bills/" + date + "/payed_by", "me");
        addBillMap.put("Friends/" + user_id + "/" + mCurrentUserId + "/bills/" + date + "/payed_by", "friend");

        addBillMap.put("Friends/" + mCurrentUserId + "/" + user_id + "/bills/" + date + "/description", description);
        addBillMap.put("Friends/" + user_id + "/" + mCurrentUserId + "/bills/" + date + "/description", description);

        addBillMap.put("Friends/" + mCurrentUserId + "/" + user_id + "/balance", balanceInt);
        addBillMap.put("Friends/" + user_id + "/" + mCurrentUserId + "/balance", balanceInt);


        mDatabase.updateChildren(addBillMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if (databaseError != null) {
                    String error = databaseError.getMessage();
                    Toast.makeText(AddBill.this, error, Toast.LENGTH_LONG);
                } else {

                    Toast.makeText(AddBill.this, "Bill added.", Toast.LENGTH_LONG).show();
                }


            }
        });

    }
}
