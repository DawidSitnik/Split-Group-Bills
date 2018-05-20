package com.example.sitnik.onetoonechat;

import android.content.Intent;
import android.media.MediaSync;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddGroupName extends AppCompatActivity {

    private EditText mGroupName;

    private Button mSubmitGroupName;

    private DatabaseReference mDatabaseRef;

    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group_name);

        mGroupName = findViewById(R.id.tv_group_name);
        mSubmitGroupName = findViewById(R.id.btn_submit_name);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();


        mSubmitGroupName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mGroupName.getText().toString().replaceAll("\\s+","").equals("")){
                    Toast.makeText(AddGroupName.this, "Enter Group Name", Toast.LENGTH_LONG).show();
                    mGroupName.setText("");
                }

                else {

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    Map memberMap = new HashMap();
                    memberMap.put("Groups/" + mGroupName.getText().toString() + "/" + mCurrentUser.getUid() + "/date", currentDate );
                    memberMap.put("Users/" + mCurrentUser.getUid() + "/groups/" + mGroupName.getText().toString() +"/role", "creator" );


                    mDatabaseRef.updateChildren(memberMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){
                                String error = databaseError.getMessage();
                                Toast.makeText(AddGroupName.this, error, Toast.LENGTH_LONG);
                            }

                            else{

                                Toast.makeText(AddGroupName.this, "Group created", Toast.LENGTH_LONG).show();
                            }


                        }
                    });


                    Intent intent = new Intent(AddGroupName.this, CreateGroupActivity.class);
                    intent.putExtra("group_name", mGroupName.getText().toString());
                    startActivity(intent);
                }
            }
        });

    }
}
