package com.example.sitnik.onetoonechat;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddBillGroup extends AppCompatActivity {

    /*** Layout elemets */
    private Toolbar mToolbar;
    private EditText mDescription;
    private EditText mAmount;
    private Button mAdd;
    public static Button mSplittingType;
    public static Button mPaidBy;
    private DatabaseReference mDatabase;

    /*** Firebase */
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    /*** Variables to populate database */
    private String balance;
    private String yourBalance;
    private String amount; /*** amount from input */
    private String description; /*** description from input */
    private String group_id; /*** date of group creation, taken from another activity */
    private String splitting_type; /*** depending on option we have choosen, default value "equally" */
    private String paid_by; /*** depending on who we have picked as a payer, default value "user_id" */
    private String bill_date; /*** date of bill creation, from previous intent */

    public static int payers_count; /*** number of people that are paying a bill */

    public static HashMap splitEquallyMap = new HashMap(); /*** map with values added in splitEqually method */
    private Map billMap = new HashMap(); /*** map with basic bill information */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bill_group);

        payers_count = -1;

        mToolbar = findViewById(R.id.users_appBar);
        setSupportActionBar(mToolbar);
        setTitle("Add Bill");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDescription = findViewById(R.id.addBillGroup_description);
        mAmount = findViewById(R.id.addBillGroup_amount);
        mSplittingType = findViewById(R.id.addBillGroup_splittingType);
        mPaidBy = findViewById(R.id.addBillGroup_paidByButton);

        mAdd = findViewById(R.id.addBillGroup_add);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        group_id = getIntent().getStringExtra("group_id");
        bill_date = getIntent().getStringExtra("bill_date");


        mDatabase.child("Users").child(mCurrentUserId).child("groups").child(group_id).child("balance").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                yourBalance = dataSnapshot.child("amount").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.child("Groups").child(group_id).child("bills").child(bill_date).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                splitting_type = dataSnapshot.child("splitting_type").getValue().toString();
                paid_by = dataSnapshot.child("paid_by").getValue().toString();
                payers_count = 0;
                for (DataSnapshot addressSnapshot : dataSnapshot.child("split_with").getChildren()) {
                    payers_count++;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mSplittingType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddBillGroup.this, PopOutSplittingType.class);
                intent.putExtra("group_date", group_id);
                intent.putExtra("bill_date", bill_date);
                startActivity(intent);
            }
        });

        mPaidBy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddBillGroup.this, PopOutPaidBy.class);
                intent.putExtra("group_date", group_id);
                intent.putExtra("bill_date", bill_date);
                startActivity(intent);
            }
        });

                    mAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        description = mDescription.getText().toString();
                        amount = mAmount.getText().toString();

                        if(mDescription.getText().toString().replaceAll("\\s+","").equals("")){
                            Toast.makeText(AddBillGroup.this, "Enter Description", Toast.LENGTH_LONG).show();
                            mDescription.setText("");
                        }

                        if(mAmount.getText().toString().replaceAll("\\s+","").equals("")){
                            Toast.makeText(AddBillGroup.this, "Enter Amount", Toast.LENGTH_LONG).show();
                            mAmount.setText("");
                        }

                        else {


                            switch (splitting_type) {
                                case "equally":

                                    splitEqually();

                                    break;
                                case "unequally":


                                    break;
                                case "by_percentages":

                                    ;
                                    break;
                                case "by_shares":


                                    break;
                                case "by_adjustment":


                                    break;
                            }
                            billMap.put("Groups/" + group_id + "/bills/" + bill_date + "/amount", amount);
                            billMap.put("Groups/" + group_id + "/bills/" + bill_date + "/description", description);
                            mDatabase.updateChildren(billMap);

                            Intent intent = new Intent(AddBillGroup.this, ShareWithGroup.class);
                            intent.putExtra("group_id", group_id);
                            startActivity(intent);
                            finish();
                        }


                    }
                });


    }

    /*** called while splitting_type is "equal" */
    private void splitEqually(){

        String amountString = mAmount.getText().toString();
        final double amountDouble = Double.parseDouble(amountString);
        final double amountPerUser = amountDouble / payers_count;

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot memberSnapshot: dataSnapshot.child("Groups").child(group_id).child("members").getChildren()) {
                    //et the beginning we have just pepole who pays in split_with, after
                    String memberId = memberSnapshot.getKey().toString();
                    String memberBalance = dataSnapshot.child("Users").child(memberId).child("groups").child(group_id).child("balance").child("amount").getValue().toString();
                    double memberBalanceDouble = Double.parseDouble(memberBalance);
                    if (paid_by.equals(memberId)) { //if we paid by bill
                        if (dataSnapshot.child("Groups").child(group_id).child("bills").child(bill_date).child("split_with").child(memberId).exists()) { //if we are also a payer
                            memberBalanceDouble += amountDouble - amountPerUser;
                            splitEquallyMap.put("Groups/" + group_id + "/bills/" + bill_date + "/split_with/" + memberId, amountDouble - amountPerUser);
                        } else { //else we are not a payer
                            memberBalanceDouble += amountDouble;
                            splitEquallyMap.put("Groups/" + group_id + "/bills/" + bill_date + "/split_with/" + memberId, amountDouble);
                        }

                    } //if we are not a payer
                    else {
                        if(dataSnapshot.child("Groups").child(group_id).child("bills").child(bill_date).child("split_with").child(memberId).exists()){ //we should check if bill is splitted with us
                            memberBalanceDouble -= amountPerUser;
                            splitEquallyMap.put("Groups/" + group_id + "/bills/" + bill_date + "/split_with/" + memberId, -amountPerUser);
                        } //if not we dont get any money and we dont lose anything
                    }


                    splitEquallyMap.put("Users/" + memberId + "/groups/" + group_id + "/balance/amount", memberBalanceDouble);

                }


                }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });



    }
}


