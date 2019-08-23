package com.example.mcr_rework;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class LandingActivity extends AppCompatActivity {
    public static final String CHANNEL_ID  = "simplified_coding";
    private static final String CHANNEL_NAME = "Simplified Coding";
    private static final String CHANNEL_DESC = "Simplified Coding Notifications";
    private FirebaseAuth mAuth;

    private Button EtButton, BtButton, setting, ownerButton;
    private Toolbar mToolbar;
    private ImageView i1,i2;
    private String currentUserID;



    private DatabaseReference RootRef;
    private DatabaseReference UsersRef;

    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String type = dataSnapshot.child("Type").getValue().toString();
                    Log.e("check",type);

                    if(type.equals("client")){
                        Log.e("check","c");
                        SendUserToMainActivity();

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });






        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

        }



        mToolbar = (Toolbar)findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("MCR");

        EtButton = (Button)findViewById(R.id.employee_track);
        BtButton = (Button)findViewById(R.id.business_track);

        ownerButton = (Button)findViewById(R.id.Owner_view);
        setting = (Button)findViewById(R.id.go_to_settings);

        ownerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ownerIntent = new Intent(LandingActivity.this, OwnerActivity.class);
                startActivity(ownerIntent);
            }
        });

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToSettingsActivity();
            }
        });


        EtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent employeeIntent = new Intent(LandingActivity.this, EmployeeTrackerActivity.class);
                startActivity(employeeIntent);

            }
        });

        BtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent BusinessIntent = new Intent(LandingActivity.this, MainActivity.class);
                startActivity(BusinessIntent);

            }
        });
        i1 = (ImageView) findViewById(R.id.image1);
        i2 = (ImageView) findViewById(R.id.image2);

    }
    protected void onStart() {
        super.onStart();



        if(currentUser == null){
            SendUserToLoginActivity();
        }else{
            VerifyUserExistance();
        }
        Log.e("check","a");






        Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/mcrrework.appspot.com/o/landingPage%2Fph1.jpg?alt=media&token=629eda9b-0ba6-4d71-9d25-a2311770c12f").placeholder(R.drawable.profile_image).into(i1);
        Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/mcrrework.appspot.com/o/landingPage%2Fph2.jpg?alt=media&token=899afc99-46a6-4889-b1a2-450f5088cadd").placeholder(R.drawable.profile_image).into(i2);




    }

    private void VerifyUserExistance() {
        String currentUserId = mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child("name").exists())){
                    Toast.makeText(LandingActivity.this, "Welcome", Toast.LENGTH_SHORT).show();

                }
                else
                {
                    Toast.makeText(LandingActivity.this, "Please Update Your info", Toast.LENGTH_SHORT).show();
                    SendUserToSettingsActivity();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(LandingActivity.this, LoginActivity.class );
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
    private void SendUserToMainActivity() {
        Intent loginIntent = new Intent(LandingActivity.this, MainActivity.class );
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void SendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(LandingActivity.this, SettingsActivity.class );
        //settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsIntent);

    }
}
