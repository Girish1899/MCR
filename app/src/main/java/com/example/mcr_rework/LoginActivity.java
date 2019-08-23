package com.example.mcr_rework;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.iid.FirebaseInstanceId;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity implements LocationListener {
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 401;
    private FirebaseAuth mAuth;
    private Button LoginButton, PhoneLoginButton;
    private ProgressDialog loadingBar;
    private LocationManager locationManager;
    private String Lat, Lon, currentDate, currentTime;
    private String currentUserID;

    private EditText UserEmail, UserPassword;
    private TextView NeedNewAccountLink, ForgotPasswordLink;
    private DatabaseReference UsersRef;
    boolean isLocationFetched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        InitializeFields();


        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SendUserToRegisterActivity();


            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllowUserToLogin();

            }
        });

        PhoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent phoneLoginIntent = new Intent(LoginActivity.this, PhoneLoginActivity.class);
                startActivity(phoneLoginIntent);
            }
        });

        ForgotPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent FPIntetnt = new Intent(LoginActivity.this, ForgetPasswordActivity.class);
                startActivity(FPIntetnt);
            }
        });
    }

    private void AllowUserToLogin() {

        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter Email.....", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter Password.....", Toast.LENGTH_SHORT).show();
        } else {

            loadingBar.setTitle("Signing In");
            loadingBar.setMessage("Please wait............");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                currentUserID = mAuth.getCurrentUser().getUid();
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();



                                UsersRef.child(currentUserID).child("device_token")
                                        .setValue(deviceToken)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    FirebaseUser user = mAuth.getCurrentUser();

                                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                                    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                                                            .build();
                                                    db.setFirestoreSettings(settings);

                                                    DocumentReference userRef = db.collection(getString(R.string.collection_users))
                                                            .document(user.getUid());

                                                    userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if(task.isSuccessful()){
                                                                DocumentSnapshot snapshot = task.getResult();
                                                                if (snapshot.exists()) {
                                                                Log.d("TAG", "onComplete: successfully set the user client.");
                                                                User user = task.getResult().toObject(User.class);
                                                                ((UserClient)(getApplicationContext())).setUser(user);
                                                                } else {
                                                                    Log.d("TAG", "No such document");
                                                                }
                                                            }
                                                        }
                                                    });

                                                    getLocation();
//                                                    sendLocation();





                                                    Toast.makeText(LoginActivity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                                                    //loadingBar.dismiss();

                                                }
                                            }
                                        });



                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(LoginActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }

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
        UsersRef.child(currentUserID).child("Login").updateChildren(ContactInfoMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            SendUserToSplashActivity();
                            Toast.makeText(LoginActivity.this, "contact added to phone book successfully", Toast.LENGTH_SHORT).show();


                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(LoginActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private void InitializeFields() {
        LoginButton = (Button) findViewById(R.id.login_button);

        PhoneLoginButton = (Button) findViewById(R.id.phone_login_button);
        UserEmail = (EditText) findViewById(R.id.login_email);
        UserPassword = (EditText) findViewById(R.id.login_password);
        NeedNewAccountLink = (TextView) findViewById(R.id.need_new_account_link);
        ForgotPasswordLink = (TextView) findViewById(R.id.forget_password_link);
        loadingBar = new ProgressDialog(this);
        ForgotPasswordLink = (TextView)findViewById(R.id.forget_password_link);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        Log.e("error", "permission location" + requestCode);
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("error", "permission granted");


                    getLocation();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.e("error", "permission denied");
                }
                return;
            }
            case 401: {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, (LocationListener) this);

            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    void getLocation() {

        try {
            if ((ContextCompat.checkSelfPermission(LoginActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(LoginActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)){
                ActivityCompat.requestPermissions(LoginActivity.this,
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
        loadingBar.dismiss();


    }

    @Override
    public void onProviderDisabled(String provider) {

        Toast.makeText(LoginActivity.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.e("error","status changed");

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.e("error","Provider enanled");

    }








    private void SendUserToSplashActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, SplashActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void SendUserToRegisterActivity() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }


}
