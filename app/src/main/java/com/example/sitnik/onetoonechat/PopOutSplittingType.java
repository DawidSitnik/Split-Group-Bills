package com.example.sitnik.onetoonechat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


public class PopOutSplittingType extends AppCompatActivity {

    /***layout*/
    private Button mButtonOk;
    private Spinner mSpinner;
    private RecyclerView mMembersList;

    /***database*/
    private DatabaseReference mDatabaseRef;

    /***other*/
    private Query query;/***query to fill members list*/

    /***paths to the spinner*/
    private static final String[]paths = {"Split equally", "Split unequally", "Split by percentages", "Split by shares", "Split by adjustment"};

    private String splitting_type;/***type of splitting the bill, chosen in spinner*/

    /***extras from previous intent*/
    private String group_date; /***date of group creation*/
    private String bill_date; /***date of bill creation*/

    public Map hashMap = new HashMap(); /***hashMap to update database after choosing members to split bill with and splitting type */

    /***defying variables*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_out_splitting_type);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width * 0.8), (int) (height * 0.6));

        mButtonOk = findViewById(R.id.popOut_button);
        mSpinner = findViewById(R.id.popOut_splittingType);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        mMembersList = findViewById(R.id.popOut_list);
        mMembersList.setHasFixedSize(true);
        mMembersList.setLayoutManager(new LinearLayoutManager(this));

        group_date = getIntent().getStringExtra("group_date");
        bill_date = getIntent().getStringExtra("bill_date");

        query = FirebaseDatabase.getInstance().getReference().child("Groups").child(group_date).child("members");

        ArrayAdapter<String> spinnerAapter = new ArrayAdapter<String>(PopOutSplittingType.this,
                android.R.layout.simple_spinner_item, paths);

        spinnerAapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(spinnerAapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /***filling splitting type spinner*/
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {
                    case 0:
                        hashMap.put("Groups/" + group_date + "/bills/" + bill_date + "/splitting_type", "equally");
                        splitting_type = "equally";
                        break;
                    case 1:
                        hashMap.put("Groups/" + group_date + "/bills/" + bill_date + "/splitting_type", "unequally");
                        splitting_type = "unequally";
                        break;
                    case 2:
                        hashMap.put("Groups/" + group_date + "/bills/" + bill_date + "/splitting_type", "by_percentages");
                        splitting_type = "by percentages";
                        break;
                    case 3:
                        hashMap.put("Groups/" + group_date + "/bills/" + bill_date + "/splitting_type", "by_shares");
                        splitting_type = "by shares";
                        break;
                    case 4:
                        hashMap.put("Groups/" + group_date + "/bills/" + bill_date + "/splitting_type", "by_adjustment");
                        splitting_type = "by adjustment";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        displayViewHolder();

        mButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            /***removing previous split_with table from database and adding new one*/
            public void onClick(View v) {

                mDatabaseRef.child("Groups").child(group_date).child("bills").child(bill_date).child("split_with").removeValue();
                mDatabaseRef.updateChildren(hashMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                        if (databaseError != null) {
                            String error = databaseError.getMessage();
                            Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG);
                        } else {

                            Toast.makeText(getApplicationContext(), "Bill added.", Toast.LENGTH_LONG).show();
                            AddBillGroup.mSplittingType.setText(splitting_type);
                            finish();
                        }


                    }
                });


            }
        });
    }

    /***displaying view holder with group members*/
    private void displayViewHolder(){

        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(query, Friends.class)
                        .setLifecycleOwner(this)
                        .build();



        FirebaseRecyclerAdapter<Friends,MemberViewHolder> adapter = new FirebaseRecyclerAdapter<Friends, MemberViewHolder>(options) {
            @Override
            public MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                return new MemberViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_group_member, parent, false));
            }

            @Override
            protected void onBindViewHolder(final MemberViewHolder memberViewHolder, int position, final Friends friends) {

                final String list_member_id = getRef(position).getKey();

                final CheckBox checkBox = memberViewHolder.getmView().findViewById(R.id.single_member_check);
                checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    /***changing counter of people who pay bill and adding members to split_with table*/
                    public void onClick(View v) {

                        if(checkBox.isChecked()){ //adds member to hash map is his field is checked
                            hashMap.put("Groups/" + group_date + "/bills/" + bill_date + "/split_with/" + list_member_id, "");
                            if(AddBillGroup.payers_count == -1) AddBillGroup.payers_count = 1;
                            else AddBillGroup.payers_count++;
                        } else{
                            hashMap.remove("Groups/" + group_date + "/bills/" + bill_date + "/split_with/" + list_member_id);
                            if(AddBillGroup.payers_count > 0) {
                                if (AddBillGroup.payers_count == 1) AddBillGroup.payers_count = -1;
                                else AddBillGroup.payers_count--;
                            }
                        }
                    }
                });


                mDatabaseRef.child("Users").child(list_member_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String name = dataSnapshot.child("name").getValue().toString();
                        String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                        memberViewHolder.setName(name);
                        memberViewHolder.setThumbImage(thumb_image);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        mMembersList.setAdapter(adapter);

    }

}
