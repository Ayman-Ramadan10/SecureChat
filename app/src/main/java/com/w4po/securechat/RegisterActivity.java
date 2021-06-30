package com.w4po.securechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {
    private Button btnRegister;
    private EditText txtRegisterEmail, txtRegisterPass;
    private TextView txtAlreadyHaveAccountLink;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        InitializeFields();
    }

    private void InitializeFields() {
        btnRegister = (Button) findViewById(R.id.btnRegister);
        txtRegisterEmail = (EditText) findViewById(R.id.txtRegisterEmail);
        txtRegisterPass = (EditText) findViewById(R.id.txtRegisterPass);
        txtAlreadyHaveAccountLink = (TextView) findViewById(R.id.txtAlreadyHaveAccountLink);
        loadingBar = new ProgressDialog(this);

        if (getIntent().getExtras() != null) {
            String email = getIntent().getExtras().getString("visit_user_email");
            String pass = getIntent().getExtras().getString("visit_user_password");

            if (email != null)
                txtRegisterEmail.setText(email);
            if (pass != null)
                txtRegisterPass.setText(pass);
        }

        txtAlreadyHaveAccountLink.setOnClickListener(v -> {
            String email = txtRegisterEmail.getText().toString();
            String pass = txtRegisterPass.getText().toString();

            Intent profileIntent = new Intent(RegisterActivity.this, LoginActivity.class);
            profileIntent.putExtra("visit_user_email", email);
            profileIntent.putExtra("visit_user_password", pass);
            startActivity(profileIntent);
        });
        btnRegister.setOnClickListener(v -> createAccount());
    }

    private void createAccount() {
        String email = txtRegisterEmail.getText().toString();
        String password = txtRegisterPass.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Helper.makeToast(this, "Please enter email...", 0);
        } else if (TextUtils.isEmpty(password)) {
            Helper.makeToast(this, "Please enter password...", 0);
        } else {
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait, while we wre creating new account for you...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            String currentUserID = mAuth.getCurrentUser().getUid();
                            rootRef.child("Users").child(currentUserID).setValue("");

                            rootRef.child("Users").child(currentUserID).child("device_token").setValue(deviceToken);

                            Helper.startActivity(RegisterActivity.this, MainActivity.class, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            finish();
                            Helper.makeToast(RegisterActivity.this, "Account Created Successfully...", 0);
                            loadingBar.dismiss();
                        } else {
                            String message = task.getException().toString();
                            Helper.makeToast(RegisterActivity.this, "Error : " + message, 0);
                            loadingBar.dismiss();
                        }
                    });
        }
    }


}