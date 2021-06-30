package com.w4po.securechat;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private Button btnLogin, btnPhoneLogin;
    private EditText txtLoginEmail, txtLoginPass;
    private TextView txtNewAccountLink;

    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    static {
        Helper.initialize();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        initializeFields();
    }

    private void initializeFields() {
        btnLogin = findViewById(R.id.btnLogin);
        btnPhoneLogin = findViewById(R.id.btnPhoneLogin);
        txtLoginEmail = findViewById(R.id.txtLoginEmail);
        txtLoginPass = findViewById(R.id.txtLoginPass);
        txtNewAccountLink = findViewById(R.id.txtNewAccountLink);
        loadingBar = new ProgressDialog(this);

        if (getIntent().getExtras() != null) {
            String email = getIntent().getExtras().getString("visit_user_email");
            String pass = getIntent().getExtras().getString("visit_user_password");

            if (email != null)
                txtLoginEmail.setText(email);
            if (pass != null)
                txtLoginPass.setText(pass);
        }

        btnLogin.setOnClickListener(v -> login());
        btnPhoneLogin.setOnClickListener(v -> Helper.startActivity(LoginActivity.this, PhoneLoginActivity.class, null));
        txtNewAccountLink.setOnClickListener(v -> {
            String email = txtLoginEmail.getText().toString();
            String pass = txtLoginPass.getText().toString();

            Intent profileIntent = new Intent(LoginActivity.this, RegisterActivity.class);
            profileIntent.putExtra("visit_user_email", email);
            profileIntent.putExtra("visit_user_password", pass);
            startActivity(profileIntent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Helper.startActivity(this, MainActivity.class, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            finish();
        }
    }

    private void login() {
        String email = txtLoginEmail.getText().toString();
        String password = txtLoginPass.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Helper.makeToast(this, "Please enter email...", 0);
        } else if (TextUtils.isEmpty(password)) {
            Helper.makeToast(this, "Please enter password...", 0);
        } else {
            loadingBar.setTitle("Sign In");
            loadingBar.setMessage("Please wait....");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            signInEmailPass(email, password);
        }
    }

    private void signInEmailPass(String email, String pass) {
        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                String currentUserId = mAuth.getCurrentUser().getUid();
                String deviceToken = FirebaseInstanceId.getInstance().getToken();

                usersRef.child(currentUserId).child("device_token")
                        .setValue(deviceToken)
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful())
                            {
                                Helper.startActivity(LoginActivity.this, MainActivity.class, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                finish();
                                Helper.makeToast(LoginActivity.this, "Logged in Successful...", 0);
                                loadingBar.dismiss();
                            }
                        });
            }
            else
            {
                String message = task.getException().toString();
                Helper.makeToast(LoginActivity.this, "Error : " + message, 0);
                loadingBar.dismiss();
            }
        });
    }
}