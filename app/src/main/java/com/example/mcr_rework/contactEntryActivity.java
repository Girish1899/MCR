package com.example.mcr_rework;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.util.HashMap;

public class contactEntryActivity extends AppCompatActivity {

    private EditText _nameText, _emailText, _mobileText, _addressText;
    private Button _addContactButton;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef, PhoneRef, ContactNameRef, ContactKeyRef, UsersRef;
    String bid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_entry);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        PhoneRef = FirebaseDatabase.getInstance().getReference().child("PhoneBook");
        UsersRef.child(currentUserID).child("bId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String business_id=dataSnapshot.getValue(String.class);
                ContactNameRef = PhoneRef.child("bid").child("currentUserID").child(business_id);


//                Log.e("business id",dataSnapshot.getValue(String.class)+"is the value");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        _nameText = findViewById(R.id.input_name);
        _emailText = findViewById(R.id.input_email);
        _mobileText = findViewById(R.id.input_mobile);
        _addressText = findViewById(R.id.input_address);

        _addContactButton = findViewById(R.id.btn_add_contact);

        _addContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addContact();
            }
        });


    }
    private void addContact() {


        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String mobile = _mobileText.getText().toString();
        String address = _addressText.getText().toString();
        String ContactKey = ContactNameRef.push().getKey();

        if (TextUtils.isEmpty(name)){
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(mobile)){
            Toast.makeText(this, "Please enter a contact number", Toast.LENGTH_SHORT).show();
        }else{
            HashMap<String, Object> UserContactKey = new HashMap<>();
            ContactNameRef.updateChildren(UserContactKey);

            ContactKeyRef = ContactNameRef.child(ContactKey);

            HashMap<String, Object> ContactInfoMap = new HashMap<>();
            ContactInfoMap.put("name", name);
            ContactInfoMap.put("email", email);
            ContactInfoMap.put("mobile", mobile);
            ContactInfoMap.put("address", address);
            ContactKeyRef.updateChildren(ContactInfoMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                Toast.makeText(contactEntryActivity.this, "contact added to phone book successfully", Toast.LENGTH_SHORT).show();
                                reset();

                            }else{
                                String message = task.getException().toString();
                                Toast.makeText(contactEntryActivity.this, "Error : "+ message, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

        }

    }

    private void reset() {
        _nameText.setText("");
        _emailText.setText("");
        _mobileText.setText("");
        _addressText.setText("");
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    bid = dataSnapshot.child("bId").getValue().toString();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
