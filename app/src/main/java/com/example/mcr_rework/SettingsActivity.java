package com.example.mcr_rework;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity implements LocationListener {
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 401;
    private Button UpdateAccountSettings, changePassword, logoutButton;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private DatabaseReference UsersRef;
    private static final int GalleryPick = 1;
    private StorageReference UserProfileImagesRef;
    private ProgressDialog loadingBar;
    private Toolbar SettingsToolBar;
    private String Lat, Lon, currentDate, currentTime;
    boolean isLocationFetched = false;
    private LocationManager locationManager;
    private FirebaseFirestore mDb;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        email = mAuth.getCurrentUser().getEmail();

        InitializeFields();

        userName.setEnabled(false);
        RetrieveUserInfo();

        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSettings();
            }
        });

        RetrieveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);

            }
        });


        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent CPIntent = new Intent(SettingsActivity.this, changePasswordActivity.class);
                startActivity(CPIntent);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentUserID = mAuth.getCurrentUser().getUid();
                getLocation();

                mAuth.signOut();

                SendUserToLoginActivity();
            }
        });






    }

    private void InitializeFields() {
        UpdateAccountSettings = (Button)findViewById(R.id.update_settings_button);
        changePassword = (Button)findViewById(R.id.update_Password_button);
        userName = (EditText)findViewById(R.id.set_user_name);
        userStatus = (EditText)findViewById(R.id.set_profile_status);
        userProfileImage = (CircleImageView) findViewById(R.id.set_profile_image);
        loadingBar = new ProgressDialog(this);

        SettingsToolBar = (Toolbar)findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Setting");

        logoutButton = (Button)findViewById(R.id.settings_logout_button);
        mDb = FirebaseFirestore.getInstance();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GalleryPick && resultCode==RESULT_OK && data!=null){
            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK){

                loadingBar.setTitle("set profile image");
                loadingBar.setMessage("Please wait your image is updating...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();


                StorageReference filePath = UserProfileImagesRef.child(currentUserID + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(SettingsActivity.this, "Profile Image uploaded successfully", Toast.LENGTH_SHORT).show();

                            String downloadUrl = task.getResult().getDownloadUrl().toString();

                            RootRef.child("Users").child(currentUserID).child("image")
                                    .setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful()){
                                                Toast.makeText(SettingsActivity.this, "Image Saved in database", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();

                                            }else{
                                                String message = task.getException().toString();
                                                Toast.makeText(SettingsActivity.this, "Error :" + message, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }

                                        }
                                    });
                        }else{
                            String message = task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error :" + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();

                        }

                    }
                });
            }

        }
    }

    private void UpdateSettings() {

        String setUserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();

        if(TextUtils.isEmpty(setUserName)){
            Toast.makeText(this, "Please write your user name first...", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(setStatus)){
            Toast.makeText(this, "Please write your status...", Toast.LENGTH_SHORT).show();
        }
        else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", setUserName);
            profileMap.put("status", setStatus);
            RootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                //SendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this, "Profile Updated Successfully....", Toast.LENGTH_SHORT).show();
                            }else{
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error : "+ message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

            User user = new User();
            user.setEmail(email);
            user.setUsername(setUserName);
            user.setUser_id(FirebaseAuth.getInstance().getUid());


            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .build();
            mDb.setFirestoreSettings(settings);

            DocumentReference newUserRef = mDb
                    .collection(getString(R.string.collection_users))
                    .document(FirebaseAuth.getInstance().getUid());

            newUserRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {


                    if(task.isSuccessful()){
                        SendUserToLandingActivity();
                        Toast.makeText(SettingsActivity.this, "Profile Updated Successfully....", Toast.LENGTH_SHORT).show();

                    }else{
                        View parentLayout = findViewById(android.R.id.content);
                        Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });


        }


    }

    private void RetrieveUserInfo() {
        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image")))){
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                            String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);
                            Picasso.get().load(retrieveProfileImage).into(userProfileImage);

                        }
                        else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus = dataSnapshot.child("status").getValue().toString();


                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);



                        }else {
                            userName.setEnabled(true);
                            Toast.makeText(SettingsActivity.this, "Please set and update your profile information....", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }



    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(SettingsActivity.this, LoginActivity.class );
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
    private void SendUserToLandingActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, LandingActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void sendLocation() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd,yyyy");
        currentDate = currentDateFormat.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
        currentTime = currentTimeFormat.format(calForTime.getTime());


        HashMap<String, Object> ContactInfoMap = new HashMap<>();
        ContactInfoMap.put("Latitude", Lat);
        ContactInfoMap.put("Longitude", Lon);
        ContactInfoMap.put("Date", currentDate);
        ContactInfoMap.put("Time", currentTime);
        UsersRef.child(currentUserID).child("Logout").updateChildren(ContactInfoMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            SendUserToLandingActivity();
                            Toast.makeText(SettingsActivity.this, "contact added to phone book successfully", Toast.LENGTH_SHORT).show();


                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    void getLocation() {

        try {
            if ((ContextCompat.checkSelfPermission(SettingsActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(SettingsActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)){
                ActivityCompat.requestPermissions(SettingsActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                return;

            }
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, (LocationListener) this);
        }
        catch(SecurityException e) {
            Log.e("error","crash"+e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e("error","location changed");


        Lat= String.valueOf(location.getLatitude());
        Lon = String.valueOf(location.getLongitude());
        if(!isLocationFetched==true) {

            isLocationFetched = true;

            sendLocation();
        }

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Toast.makeText(SettingsActivity.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderEnabled(String s) {
        Log.e("error","status changed");

    }

    @Override
    public void onProviderDisabled(String s) {
        Log.e("error","Provider enabled");

    }
}
