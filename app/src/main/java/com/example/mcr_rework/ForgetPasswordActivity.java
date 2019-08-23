package com.example.mcr_rework;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPasswordActivity extends AppCompatActivity {

    private EditText resetPassEmail;
    private Button resetPassButton;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        mAuth = FirebaseAuth.getInstance();

        resetPassEmail = (EditText)findViewById(R.id.password_reset_email);
        resetPassButton = (Button)findViewById(R.id.password_reset_button);

        resetPassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.sendPasswordResetEmail(resetPassEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(ForgetPasswordActivity.this, "Password sent to your Registered Email", Toast.LENGTH_SHORT).show();

                        }else{
                            String message = task.getException().getMessage();
                            Toast.makeText(ForgetPasswordActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }
}
