package com.example.sitnik.onetoonechat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

/***class activity starts after clicking on change paid by
 * person while creating group bill*/
public class PopOutPaidBy extends AppCompatActivity {

    /***layout*/
    private Button mButtonOk;
    private RecyclerView mMembersList;

    /***database*/
    private DatabaseReference mDatabaseRef; /***general reference to database*/

    private Query query; /***querry to fill recycler view*/

    /***other*/
    private String group_date; /***date of group creation*/
    private String user_name;/***user name*/
    private String bill_date;/***date of bill creation*/

    public Map hashMap = new HashMap(); /***hash map to update a database after changing paid by user*/


    /***initializing variables*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_out_paid_by);

        //changing size of the window
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*0.8), (int)(height*0.6));

        mButtonOk = findViewById(R.id.popOutPaidBy_button);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        mMembersList = findViewById(R.id.popOutPaidBy_list);
        mMembersList.setHasFixedSize(true);
        mMembersList.setLayoutManager(new LinearLayoutManager(this));

        group_date = getIntent().getStringExtra("group_date");
        bill_date = getIntent().getStringExtra("bill_date");

        query = FirebaseDatabase.getInstance().getReference().child("Groups").child(group_date).child("members");

        displayViewHolder();

        mButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            /***just finish activity*/
            public void onClick(View v) {

                finish();
            }
        });


    }

    /***populating view holder with group members*/
    private void displayViewHolder(){

        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(query, Friends.class)
                        .setLifecycleOwner(this)
                        .build();


        FirebaseRecyclerAdapter<Friends, MemberViewHolder> adapter = new FirebaseRecyclerAdapter<Friends, MemberViewHolder>(options) {
            @Override
            public MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                return new MemberViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_group_member, parent, false));
            }

            @Override
            protected void onBindViewHolder(final MemberViewHolder memberViewHolder, int position, final Friends friends) {

                memberViewHolder.getmView().findViewById(R.id.single_member_check).setVisibility(View.INVISIBLE);

                final String list_member_id = getRef(position).getKey();

                mDatabaseRef.child("Users").child(list_member_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        user_name = dataSnapshot.child("name").getValue().toString();
                        String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                        memberViewHolder.setName(user_name);
                        memberViewHolder.setThumbImage(thumb_image);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                memberViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    /***updating database after choosing new paid by person*/
                    public void onClick(View v) {
                        hashMap.put("Groups/" + group_date + "/bills/" + bill_date + "/paid_by" , list_member_id);
                        AddBillGroup.mPaidBy.setText(memberViewHolder.getName());

                        mDatabaseRef.updateChildren(hashMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                if (databaseError != null) {
                                    String error = databaseError.getMessage();
                                    Toast.makeText(PopOutPaidBy.this, error, Toast.LENGTH_LONG);
                                } else {

                                    Toast.makeText(PopOutPaidBy.this, "Paid by added.", Toast.LENGTH_LONG).show();
                                    finish();
                                }


                            }
                        });

                    }
                });


            }


        };

        mMembersList.setAdapter(adapter);

    }


}
