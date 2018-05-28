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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddBillGroup extends AppCompatActivity {

    private Toolbar mToolbar;

    private EditText mDescription;
    private EditText mAmount;

    private Button mAdd;
    private Button mSplittingType;
    public static Button mPaidBy;

    private DatabaseReference mDatabase;

    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    private String balance;
    private String yourBalance;
    private String amount;
    private String description;
    private String group_id;
    private String date;
    private String splitting_type;
    private String group_name;
    private String borrower;

    private List<String> propertyAddressList;

    private double balanceDouble, amountDouble;

    private Map addBillMap = new HashMap();
    private static Map hashMap;

    private String bill_date;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bill_group);

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

        group_name = getIntent().getStringExtra("group_name");
        group_id = getIntent().getStringExtra("group_id");
        bill_date = getIntent().getStringExtra("bill_date");

        hashMap = new HashMap();

       // hashMap = (HashMap) getIntent().getSerializableExtra("hash_map");

        final List<String> propertyAddressList = new ArrayList<String>();

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                yourBalance = dataSnapshot.child("Users").child(mCurrentUserId).child("groups").child(group_id).child("balance").child("amount").getValue().toString();
                borrower = dataSnapshot.child("Users").child(mCurrentUserId).child("groups").child(group_id).child("balance").child("borrower").getValue().toString();

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
               // finish();
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
                mDatabase.updateChildren(hashMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                        if (databaseError != null) {
                            String error = databaseError.getMessage();
                            Toast.makeText(AddBillGroup.this, error, Toast.LENGTH_LONG);
                        } else {
                            Toast.makeText(AddBillGroup.this, "Splitters added", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                });
            }
        });

    }

    public static void addToHashMap(String key, String value){
        hashMap.put(key, value);
    }

    public static void removeFromHashMap(String key) {
        hashMap.remove(key);
    }
}
