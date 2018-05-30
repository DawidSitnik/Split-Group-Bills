package com.example.sitnik.onetoonechat;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import java.util.Map;

public class AddBill extends AppCompatActivity {

    /***layout*/
    private Toolbar mToolbar;
    private EditText mDescription;
    private EditText mAmount;
    private Button mAdd;

    /***database*/
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    /***activity variable*/
    private String balance;
    private String borrower;
    private String amount;
    private String description;
    private String user_id;
    private String date;
    private String splitting_type;

    private double balanceDouble, amountDouble;

    private Spinner mSpinner;
    private static final String[]paths = {"Paid by you and split equally", "Paid by the other person and split equally", "You owe the full amount", "They owe the full amount", "More options"};

    Map addBillMap = new HashMap();

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
        mSpinner = findViewById(R.id.addBill_spinner);

        mAdd = findViewById(R.id.addBill_add);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        user_id = getIntent().getStringExtra("user_id");

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                balance = dataSnapshot.child("Friends").child(mCurrentUserId).child(user_id).child("balance").getValue().toString();

                //take borrower only if it is !0 ,in other case we will type borrower manually depending on spliting option
                if(!balance.equals("0")){
                    borrower = dataSnapshot.child("Friends").child(mCurrentUserId).child(user_id).child("borrower").getValue().toString();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        ArrayAdapter<String>adapter = new ArrayAdapter<String>(AddBill.this,
                android.R.layout.simple_spinner_item,paths);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                ((TextView) parent.getChildAt(0)).setTextColor((Color.parseColor("#091321"))); //chyba cos nie dziala

                switch (position) {
                    case 0:
                        splitting_type = "paid_by_me_split_equally";
                        break;
                    case 1:
                        splitting_type = "paid_by_other_person_split_equally";
                        break;
                    case 2:
                        splitting_type = "you_owe_the_full_amount";
                        break;
                    case 3:
                        splitting_type = "they_owe_the_full_amount";
                        break;
                    case 4:
                        splitting_type = "more_options";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

                    date = DateFormat.getDateTimeInstance().format(new Date());

                    switch (splitting_type) {
                        case "paid_by_me_split_equally":

                            paidByMeSplitEqually("friend", "me");
                            break;
                        case "paid_by_other_person_split_equally":

                            paidByOtherPersonSplitEqually("me", "friend");
                            break;
                        case "you_owe_the_full_amount":

                            youOweTheFullAMount("me", "friend");
                            break;
                        case "they_owe_the_full_amount":

                            theyOweTheFullAmount("friend", "me");
                            break;
                        case "more_options":

                            moreOptions();
                            break;
                    }

                    finish();
                }


            }
        });

    }


    private void checkIfBalance0(String borrower1, String borrower2){

        if(balance.equals("0")){
            Map startMap = new HashMap();
            startMap.put("Friends/" + mCurrentUserId + "/" + user_id + "/borrower", borrower1);
            startMap.put("Friends/" + user_id + "/" + mCurrentUserId + "/borrower", borrower2);

            borrower = borrower2;

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

    }

    private void paidBySplitEqually(String borrower1, String borrower2){

        if(borrower.equals(borrower2)){
            balanceDouble = balanceDouble - amountDouble;

            if (amountDouble > balanceDouble){
                addBillMap.put("Friends/" + mCurrentUserId + "/" + user_id + "/borrower", borrower1);
                addBillMap.put("Friends/" + user_id + "/" + mCurrentUserId + "/borrower", borrower2);
                balanceDouble = -balanceDouble;
            }
        }
        else {
            balanceDouble = balanceDouble + amountDouble;
        }


        addBillMap.put("Friends/" + mCurrentUserId + "/" + user_id + "/bills/" + date + "/amount", amount);
        addBillMap.put("Friends/" + user_id + "/" + mCurrentUserId + "/bills/" + date + "/amount", amount);

        addBillMap.put("Friends/" + mCurrentUserId + "/" + user_id + "/bills/" + date + "/payed_by", borrower2);
        addBillMap.put("Friends/" + user_id + "/" + mCurrentUserId + "/bills/" + date + "/payed_by", borrower1);

        addBillMap.put("Friends/" + mCurrentUserId + "/" + user_id + "/bills/" + date + "/description", description);
        addBillMap.put("Friends/" + user_id + "/" + mCurrentUserId + "/bills/" + date + "/description", description);

        addBillMap.put("Friends/" + mCurrentUserId + "/" + user_id + "/balance", balanceDouble);
        addBillMap.put("Friends/" + user_id + "/" + mCurrentUserId + "/balance", balanceDouble);


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

    private void paidByMeSplitEqually(String borrower1, String borrower2){

        checkIfBalance0(borrower1, borrower2);

        addBillMap.put("Friends/" + mCurrentUserId + "/" + user_id + "/bills/" + date + "/splitting_type", "paid_by_me_split_equally");
        addBillMap.put("Friends/" + user_id + "/" + mCurrentUserId + "/bills/" + date + "/splitting_type", "paid_by_me_split_equally");

        balanceDouble = Double.parseDouble(balance);
        amountDouble = Double.parseDouble(amount) / 2;

        paidBySplitEqually(borrower1, borrower2);
    }



    private void paidByOtherPersonSplitEqually(String borrower1, String borrower2) { // borrower1 = me, borrower2 = friend

        checkIfBalance0(borrower1, borrower2);

        addBillMap.put("Friends/" + mCurrentUserId + "/" + user_id + "/bills/" + date + "/splitting_type", "paid_by_other_person_split_equally");
        addBillMap.put("Friends/" + user_id + "/" + mCurrentUserId + "/bills/" + date + "/splitting_type", "paid_by_other_person_split_equally");

        balanceDouble = Double.parseDouble(balance);
        amountDouble = Double.parseDouble(amount) / 2;

        paidBySplitEqually(borrower1, borrower2);

    }


    private void youOweTheFullAMount(String borrower1, String borrower2) {

        checkIfBalance0(borrower1, borrower2);

        addBillMap.put("Friends/" + mCurrentUserId + "/" + user_id + "/bills/" + date + "/splitting_type", "you_owe_the_full_amount");
        addBillMap.put("Friends/" + user_id + "/" + mCurrentUserId + "/bills/" + date + "/splitting_type", "you_owe_the_full_amount");

        balanceDouble = Double.parseDouble(balance);
        amountDouble = Double.parseDouble(amount);

        paidBySplitEqually(borrower1, borrower2);

    }


    private void theyOweTheFullAmount(String borrower1, String borrower2) {

        checkIfBalance0(borrower1, borrower2);

        addBillMap.put("Friends/" + mCurrentUserId + "/" + user_id + "/bills/" + date + "/splitting_type", "they_owe_the_full_amount");
        addBillMap.put("Friends/" + user_id + "/" + mCurrentUserId + "/bills/" + date + "/splitting_type", "they_owe_the_full_amount");

        balanceDouble = Double.parseDouble(balance);
        amountDouble = Double.parseDouble(amount);

        paidBySplitEqually(borrower1, borrower2);

    }


    private void moreOptions() {
    }


}
