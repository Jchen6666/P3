package com.example.jerrychen.p3;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class RegisterActivity extends AppCompatActivity implements View.OnFocusChangeListener {
    private EditText etEmail, etPassword, etConfirm;
    FirebaseAuth firebaseAuth;
    String password,email,passwordConfirm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        etEmail=findViewById(R.id.editTextEmail);
        etPassword=findViewById(R.id.editTextPassword);
        etConfirm=findViewById(R.id.editTextConfirm);
        firebaseAuth=FirebaseAuth.getInstance();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus){
        switch (v.getId()){
        }

    }
    public void buttonClickRegister(View view) {
        email=etEmail.getText().toString().trim();
        password=etPassword.getText().toString().trim();
        passwordConfirm=etConfirm.getText().toString().trim();
         if (!password.isEmpty()&& passwordConfirm.equals(password)) {
             firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                 @Override
                 public void onComplete(@NonNull Task<AuthResult> task) {
                     Exception exception = task.getException();
                     if (exception instanceof FirebaseAuthUserCollisionException) {
                         Toast.makeText(RegisterActivity.this, "email already being used", Toast.LENGTH_LONG).show();
                     }
                     if (task.isSuccessful()) {
                         Toast.makeText(RegisterActivity.this, "Account is created", Toast.LENGTH_LONG).show();
                     }
                 }
             });
         }
         else {
             Toast.makeText(RegisterActivity.this,"Please check your password",Toast.LENGTH_LONG).show();
         }
    }
}
