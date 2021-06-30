package com.w4po.securechat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private Button btnUpdateSettings;
    private EditText txtUsername, txtStatus;
    private CircleImageView imgProfile;

    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private StorageReference userProfileImagesRef;
    private ProgressDialog loadingBar;

    private Toolbar settingsToolBar;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        initialize();
        retrieveUserInfo();
    }

    private void initialize() {
        btnUpdateSettings = findViewById(R.id.btnUpdateSettings);
        txtUsername = findViewById(R.id.txtUsername);
        txtStatus = findViewById(R.id.txtStatus);
        imgProfile = findViewById(R.id.imgProfile);
        loadingBar = new ProgressDialog(this);
        settingsToolBar = findViewById(R.id.settings_toolbar);

        txtUsername.setVisibility(android.view.View.INVISIBLE);
        btnUpdateSettings.setOnClickListener(v -> updateSettings());
        imgProfile.setOnClickListener(v -> {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        });

        setSupportActionBar(settingsToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult cActivityResult = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait, your profile image is updating...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = cActivityResult.getUri();

                StorageReference filePath = userProfileImagesRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Helper.makeToast(SettingsActivity.this, "Profile Image uploaded Successfully...", 0);

                        UploadTask.TaskSnapshot taskSnapshot = task.getResult();
                        if (taskSnapshot.getMetadata() != null) {
                            if (taskSnapshot.getMetadata().getReference() != null) {
                                Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                result.addOnSuccessListener(uri -> {

                                    String downloadedUrl = uri.toString();

                                    rootRef.child("Users").child(currentUserID).child("image")
                                            .setValue(downloadedUrl)
                                            .addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    Picasso.get().load(downloadedUrl).into(imgProfile);
                                                    Helper.makeToast(SettingsActivity.this, "Image save in Database, Successfully...", 0);
                                                } else {
                                                    String message = task1.getException().toString();
                                                    Helper.makeToast(SettingsActivity.this, "Error: " + message, 0);
                                                }
                                            });
                                });
                            } else {
                                Helper.makeToast(SettingsActivity.this, "Error: ", 0);
                            }
                        } else {
                            Helper.makeToast(SettingsActivity.this, "Error: ", 0);
                        }
                        loadingBar.dismiss();
                    } else {
                        String message = task.getException().toString();
                        Helper.makeToast(SettingsActivity.this, "Error: " + message, 0);
                        loadingBar.dismiss();
                    }
                });
            }
        }
    }

    private void retrieveUserInfo() {
        final UserKey[] userKeys = {Helper.getUserKeys(currentUserID)};
        ProgressDialog loadingBar = new ProgressDialog(this);
        loadingBar.setTitle("Generating PGP Keys");
        loadingBar.setMessage("Please wait....");
        loadingBar.setCanceledOnTouchOutside(false);

        if (userKeys[0] == null)
            loadingBar.show();

        Query query = rootRef.child("Users").child(currentUserID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("name")) {
                        String retrieveUserName = dataSnapshot.child("name").getValue(String.class);
                        String retrievesStatus = dataSnapshot.child("status").getValue(String.class);

                        txtUsername.setText(retrieveUserName);
                        txtStatus.setText(retrievesStatus);
                    } else {
                        txtUsername.setVisibility(android.view.View.VISIBLE);
                        Helper.makeToast(SettingsActivity.this, "Please set & update your profile information...", 0);
                    }

                    String retrieveProfileImage = dataSnapshot.child("image").getValue(String.class);
                    if (retrieveProfileImage != null) {
                        Picasso.get().load(retrieveProfileImage).into(imgProfile);
                    }

                    String firebasePK = dataSnapshot.child("publicKey").getValue(String.class);
                    if (firebasePK == null || userKeys[0] == null) {
                        if (!loadingBar.isShowing()) {
                            loadingBar.show();
                        }

                        new Thread(() -> {
                            userKeys[0] = Helper.generateNewKeys(currentUserID);

                            rootRef.child("Users").child(currentUserID).child("publicKey").setValue(userKeys[0].publicKey)
                                    .addOnCompleteListener(task -> {
                                        Helper.insertUserKeys(userKeys[0]);
                                        loadingBar.dismiss();
                                    });
                        }).start();
                    } else {
                        if (!firebasePK.equals(userKeys[0].publicKey)) {
                            rootRef.child("Users").child(currentUserID).child("publicKey").setValue(userKeys[0].publicKey);
                        }
                    }
                } else {
                    Helper.makeToast(SettingsActivity.this, "Error.", 0);
                }
                query.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateSettings() {
        String setUserName = txtUsername.getText().toString();
        String setStatus = txtStatus.getText().toString();

        if (TextUtils.isEmpty(setUserName)) {
            Helper.makeToast(this, "Please write your user name first....", 0);
        }
        if (TextUtils.isEmpty(setStatus)) {
            Helper.makeToast(this, "Please write your status....", 0);
        } else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", setUserName);
            profileMap.put("status", setStatus);
            rootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Helper.startActivity(SettingsActivity.this, MainActivity.class, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            finish();
                            Helper.makeToast(SettingsActivity.this, "Profile Updated Successfully...", 0);
                        } else {
                            String message = task.getException().toString();
                            Helper.makeToast(SettingsActivity.this, "Error: " + message, 0);
                        }
                    });
        }
    }
}