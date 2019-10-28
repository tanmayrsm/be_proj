package com.example.myapplication;

import android.content.Intent;
import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    EditText edEmail ,edPass;
    Button login;
    FirebaseAuth auth;
    TextView newUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edEmail = findViewById(R.id.email);
        edPass = findViewById(R.id.password);

        newUser = findViewById(R.id.new_user);

        login = findViewById(R.id.submit);
        auth = FirebaseAuth.getInstance();

        newUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
    public void login(View v){
        String email = edEmail.getText().toString();
        String pass = edPass.getText().toString();

        if(!email.equals("") && !pass.equals("")){
            auth.signInWithEmailAndPassword(email,pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Intent intent = new Intent(LoginActivity.this ,MainActivity.class);
                                startActivity(intent);
                            }
                            else{
                                String error2 = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "something Fishy: "+error2, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
