package com.example.mcr_rework;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String currentUserID;



    private DatabaseReference RootRef;
    private DatabaseReference UsersRef;

    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(currentUser == null){
            SendUserToLoginActivity();
        }else {
            VerifyUserExistance();
            currentUserID = mAuth.getCurrentUser().getUid();
            UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String type = dataSnapshot.child("Type").getValue().toString();
                        Log.e("check", type);

                        if (type.equals("2")) {
                            Log.e("check", "c");
                            SendUserToMainActivity();

                        } else if (type.equals("1")) {
                            Intent employeeIntent = new Intent(SplashActivity.this, EmployeeTrackerActivity.class);
                            startActivity(employeeIntent);
                        } else if (type.equals("0")) {
                            SendUserToLandingActivity();
                        }

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void VerifyUserExistance() {
        String currentUserId = mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child("name").exists())){
                    Toast.makeText(SplashActivity.this, "Welcome", Toast.LENGTH_SHORT).show();

                }
                else
                {
                    Toast.makeText(SplashActivity.this, "Please Update Your info", Toast.LENGTH_SHORT).show();
                    SendUserToSettingsActivity();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(this, LoginActivity.class );
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
    private void SendUserToMainActivity() {
        Intent loginIntent = new Intent(this, MainActivity.class );
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
    private void SendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class );
        //settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsIntent);

    }
    private void SendUserToLandingActivity() {
        Intent settingsIntent = new Intent(this, LandingActivity.class );
        //settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsIntent);

    }
}
