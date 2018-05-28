package com.example.sitnik.onetoonechat;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PopOutSplittingType extends AppCompatActivity {

    private Button mButtonOk;
    private Spinner mSpinner;
    private RecyclerView mMembersList;

    private DatabaseReference mDatabaseRef;

    private FirebaseAuth mAuth;

    private String mCurrentUserId;

    private Query query;

    private static final String[]paths = {"Split equally", "Split unequally", "Split by percentages", "Split by shares", "Split by adjustment"};

    private String splitting_type;

    private String group_date, user_id;

    public Map hashMap = new HashMap();

    private String bill_date;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_out_splitting_type);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*0.8), (int)(height*0.6));

        mButtonOk = findViewById(R.id.popOut_button);
        mSpinner = findViewById(R.id.popOut_splittingType);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        mMembersList = findViewById(R.id.popOut_list);
        mMembersList.setHasFixedSize(true);
        mMembersList.setLayoutManager(new LinearLayoutManager(this));

        group_date = getIntent().getStringExtra("group_date");

        bill_date = getIntent().getStringExtra("bill_date");

        query = FirebaseDatabase.getInstance().getReference().child("Groups").child(group_date).child("members");

        ArrayAdapter<String> spinnerAapter = new ArrayAdapter<String>(PopOutSplittingType.this,
                android.R.layout.simple_spinner_item,paths);

        spinnerAapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(spinnerAapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {
                    case 0:
                        hashMap.put("Groups/" + group_date + "/bills/" + bill_date + "/splitting_type", "equally");
                        break;
                    case 1:
                        hashMap.put("Groups/" + group_date + "/bills/" + bill_date + "/splitting_type", "unequally");
                        break;
                    case 2:
                        hashMap.put("Groups/" + group_date + "/bills/" + bill_date + "/splitting_type", "by_percentages");
                        break;
                    case 3:
                        hashMap.put("Groups/" + group_date + "/bills/" + bill_date + "/splitting_type", "by_shares");
                        break;
                    case 4:
                        hashMap.put("Groups/" + group_date + "/bills/" + bill_date + "/splitting_type", "by_adjustment");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        displayViewHolder();

        //Checking if member is checked, if so, we are adding him to the map


        mButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // pamietac zeby putExtra bill_date + odebrac to w nastepnej activity
               //Intent intent = new Intent(PopOutSplittingType.this, AddBill.class);
               //intent.putExtra("hash_map", (Serializable) hashMap);
               //startActivity(intent);
//                mDatabaseRef.updateChildren(hashMap, new DatabaseReference.CompletionListener() {
//                    @Override
//                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//
//                        if (databaseError != null) {
//                            String error = databaseError.getMessage();
//                            Toast.makeText(PopOutSplittingType.this, error, Toast.LENGTH_LONG);
//                        } else {
//                            Toast.makeText(PopOutSplittingType.this, "Splitters added", Toast.LENGTH_LONG).show();
//                        }
//                    }
//                });
               finish();


            }
        });

    }

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
                    public void onClick(View v) {

                        if(checkBox.isChecked()){ //adds member to hash map is his field is checked
                            AddBillGroup.addToHashMap("Groups/" + group_date + "/bills/" + bill_date + "/split_with/" + list_member_id, "");
                        } else{
                            AddBillGroup.removeFromHashMap("Groups/" + group_date + "/bills/" + bill_date + "/split_with/" + list_member_id);
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
