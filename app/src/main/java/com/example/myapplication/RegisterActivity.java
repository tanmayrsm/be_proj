package com.example.myapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.Models.Chats;
import com.example.myapplication.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {
    EditText name ,email ,password;
    Button reg;
    FirebaseAuth auth;
    DatabaseReference reference ,reference2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        name = findViewById(R.id.reg_name);
        email = findViewById(R.id.reg_email);
        password = findViewById(R.id.reg_pass);

        auth  =FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference2 = FirebaseDatabase.getInstance().getReference().child("Calls");

        reg = findViewById(R.id.register);

        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth = FirebaseAuth.getInstance();
                reference = FirebaseDatabase.getInstance().getReference().child("Users");
                reference2 = FirebaseDatabase.getInstance().getReference().child("Calls");

                final String name_str = name.getText().toString();
                final String email_str= email.getText().toString();
                final String pass_str = password.getText().toString();

                auth.createUserWithEmailAndPassword(email_str,pass_str).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            User user = new User(name_str,email_str,pass_str,firebaseUser.getUid());
                            reference.child(firebaseUser.getUid()).setValue(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(RegisterActivity.this, "User successfully added", Toast.LENGTH_SHORT).show();
                                                Intent i = new Intent(RegisterActivity.this ,MainActivity.class);
                                                startActivity(i);
                                                finish();
                                            }
                                            else{
                                                String error = task.getException().getMessage();
                                                Toast.makeText(RegisterActivity.this, "User not registered" + error, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                            String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                            String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                            String res = currentDate + " " + currentTime;

                            DatabaseReference hammand = FirebaseDatabase.getInstance().getReference()
                                    .child("Chats");

                            Chats chat = new Chats(" ", res);

                            hammand.child(firebaseUser.getUid()).setValue(chat)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (!task.isSuccessful()) {
                                                Toast.makeText(RegisterActivity.this, "Speech gya nhi dB me", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                        else{
                            String error2 = task.getException().getMessage();
                            Toast.makeText(RegisterActivity.this, "something Fishy: "+error2, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }
}
