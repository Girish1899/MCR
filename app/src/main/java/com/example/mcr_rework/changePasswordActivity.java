package com.example.mcr_rework;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class changePasswordActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private EditText pass, rePass;
    private Button changePass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
         mAuth = FirebaseAuth.getInstance();
         user = mAuth.getCurrentUser();

         pass = (EditText)findViewById(R.id.password_Change);
         rePass = (EditText)findViewById(R.id.password_Change_verify);
         changePass = (Button)findViewById(R.id.password_change_button);


         changePass.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 String newPass = pass.getText().toString();
                 String newPassVeri = rePass.getText().toString();

                 if(newPass.equals(newPassVeri)){
                     user.updatePassword(newPass)
                             .addOnCompleteListener(new OnCompleteListener<Void>() {
                                 @Override
                                 public void onComplete(@NonNull Task<Void> task) {
                                     if (task.isSuccessful()) {
                                         Toast.makeText(changePasswordActivity.this, "Password has been updated Successfully", Toast.LENGTH_SHORT).show();
                                     }else{
                                         String message = task.getException().getMessage();
                                         Toast.makeText(changePasswordActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                                     }
                                 }
                             });
                 }
             }
         });

    }
}
