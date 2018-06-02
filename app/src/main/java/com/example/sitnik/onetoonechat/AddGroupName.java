package com.example.sitnik.onetoonechat;

import android.app.ProgressDialog;
import android.content.Intent;
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

/*** class that starts group creation process.
 * In this step we are updating database with
 * default information about new group and add its unique name */
public class AddGroupName extends AppCompatActivity {

    /***layout*/
    private EditText mGroupName; /***group name input*/
    private Button mSubmitGroupName; /***submit button*/

    /***database*/
    private DatabaseReference mDatabaseRef; /***general database reference*/
    private FirebaseUser mCurrentUser; /***id of current user*/

    /***other*/
    private ProgressDialog progressDialog;

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
            /***updating group name and default data about group to database*/
            public void onClick(View v) {

                if(mGroupName.getText().toString().replaceAll("\\s+","").equals("")){
                    Toast.makeText(AddGroupName.this, "Enter Group Name", Toast.LENGTH_LONG).show();
                    mGroupName.setText("");
                }

                else {

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    Map memberMap = new HashMap();
                    memberMap.put("Groups/" + currentDate + "/members/" + mCurrentUser.getUid() + "/date", currentDate );
                    memberMap.put("Groups/" + currentDate + "/name", mGroupName.getText().toString() );
                    memberMap.put("Groups/" + currentDate + "/image", "default" );
                    memberMap.put("Groups/" + currentDate + "/thumb_image", "default" );

                    memberMap.put("Users/" + mCurrentUser.getUid() + "/groups/" + currentDate +"/balance/amount", 0 );
                    memberMap.put("Users/" + mCurrentUser.getUid() + "/groups/" + currentDate +"/balance/borrower", "none" );

                    memberMap.put("Users/" + mCurrentUser.getUid() + "/groups/" + currentDate +"/role", "creator" );
                    memberMap.put("Users/" + mCurrentUser.getUid() + "/groups/" + currentDate + "/image", "default");
                    memberMap.put("Users/" + mCurrentUser.getUid() + "/groups/" + currentDate + "/thumb_image", "default");
                    memberMap.put("Users/" + mCurrentUser.getUid() + "/groups/" + currentDate + "/name", mGroupName.getText().toString());

                    progressDialog = new ProgressDialog(AddGroupName.this);
                    progressDialog.setTitle("Updating image");
                    progressDialog.setMessage("Creating group.");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    mDatabaseRef.updateChildren(memberMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){
                                String error = databaseError.getMessage();
                                Toast.makeText(AddGroupName.this, error, Toast.LENGTH_LONG);
                            }

                            else{

                                Toast.makeText(AddGroupName.this, "Group created", Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                                Intent intent = new Intent(AddGroupName.this, AddGroupImage.class);
                                intent.putExtra("group_name", currentDate);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });

                }
            }
        });

    }
}