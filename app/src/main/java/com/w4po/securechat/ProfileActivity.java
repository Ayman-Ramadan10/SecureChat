package com.w4po.securechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private String receiverUserID, senderUserID, current_State;

    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button btnSendMessageRequest, btnDeclineMessageRequest;

    private DatabaseReference userRef, chatRequestRef, contactsRef, notificationRef;
    private FirebaseAuth mAuth;

    public ProfileActivity() {
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar mToolbar = findViewById(R.id.profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("");

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        senderUserID = mAuth.getCurrentUser().getUid();

        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_user_name);
        userProfileStatus = findViewById(R.id.visit_profile_status);
        btnSendMessageRequest = findViewById(R.id.send_message_request_button);
        btnDeclineMessageRequest = findViewById(R.id.decline_message_request_button);
        current_State = "new";

        RetrieveUserInfo();
    }


    private void RetrieveUserInfo() {
        userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userName = dataSnapshot.child("name").getValue(String.class);
                    String userStatus = dataSnapshot.child("status").getValue(String.class);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    if (dataSnapshot.hasChild("image")) {
                        String userImage = dataSnapshot.child("image").getValue(String.class);

                        Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    }

                    ManageChatRequests();
                } else {
                    Helper.makeToast(ProfileActivity.this, "Error!", 0);
                }

                userRef.child(receiverUserID).removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void ManageChatRequests() {
        chatRequestRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(receiverUserID)) {
                    String request_type = dataSnapshot.child(receiverUserID).child("request_type").getValue(String.class);

                    if (request_type.equals("sent")) {
                        current_State = "request_sent";
                        btnSendMessageRequest.setText("Cancel Chat Request");
                    } else if (request_type.equals("received")) {
                        current_State = "request_received";
                        btnSendMessageRequest.setText("Accept Chat Request");

                        btnDeclineMessageRequest.setVisibility(View.VISIBLE);
                        btnDeclineMessageRequest.setEnabled(true);

                        btnDeclineMessageRequest.setOnClickListener(view -> CancelChatRequest());
                    }
                } else {
                    contactsRef.child(senderUserID)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(receiverUserID)) {
                                        current_State = "friends";
                                        btnSendMessageRequest.setText("Remove this Contact");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        if (!senderUserID.equals(receiverUserID)) {
            btnSendMessageRequest.setOnClickListener(view -> {
                btnSendMessageRequest.setEnabled(false);

                if (current_State.equals("new")) {
                    SendChatRequest();
                }
                if (current_State.equals("request_sent")) {
                    CancelChatRequest();
                }
                if (current_State.equals("request_received")) {
                    AcceptChatRequest();
                }
                if (current_State.equals("friends")) {
                    RemoveSpecificContact();
                }
            });
        } else {
            btnSendMessageRequest.setVisibility(View.INVISIBLE);
        }
    }


    private void RemoveSpecificContact() {
        contactsRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactsRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                btnSendMessageRequest.setEnabled(true);
                                                current_State = "new";
                                                btnSendMessageRequest.setText("Send Message");

                                                btnDeclineMessageRequest.setVisibility(View.INVISIBLE);
                                                btnDeclineMessageRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    private void AcceptChatRequest() {
        contactsRef.child(senderUserID).child(receiverUserID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        contactsRef.child(receiverUserID).child(senderUserID)
                                .child("Contacts").setValue("Saved")
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        chatRequestRef.child(senderUserID).child(receiverUserID)
                                                .removeValue()
                                                .addOnCompleteListener(task11 -> {
                                                    if (task11.isSuccessful()) {
                                                        chatRequestRef.child(receiverUserID).child(senderUserID)
                                                                .removeValue()
                                                                .addOnCompleteListener(task111 -> {
                                                                    btnSendMessageRequest.setEnabled(true);
                                                                    current_State = "friends";
                                                                    btnSendMessageRequest.setText("Remove this Contact");

                                                                    btnDeclineMessageRequest.setVisibility(View.INVISIBLE);
                                                                    btnDeclineMessageRequest.setEnabled(false);
                                                                });
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }


    private void CancelChatRequest() {
        chatRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        chatRequestRef.child(receiverUserID).child(senderUserID)
                                .removeValue()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        btnSendMessageRequest.setEnabled(true);
                                        current_State = "new";
                                        btnSendMessageRequest.setText("Send Message");

                                        btnDeclineMessageRequest.setVisibility(View.INVISIBLE);
                                        btnDeclineMessageRequest.setEnabled(false);
                                    }
                                });
                    }
                });
    }


    private void SendChatRequest() {
        chatRequestRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        chatRequestRef.child(receiverUserID).child(senderUserID)
                                .child("request_type").setValue("received")
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        HashMap<String, String> chatNotificationMap = new HashMap<>();
                                        chatNotificationMap.put("from", senderUserID);
                                        chatNotificationMap.put("type", "request");

                                        notificationRef.child(receiverUserID).push()
                                                .setValue(chatNotificationMap)
                                                .addOnCompleteListener(task11 -> {
                                                    if (task11.isSuccessful()) {
                                                        btnSendMessageRequest.setEnabled(true);
                                                        current_State = "request_sent";
                                                        btnSendMessageRequest.setText("Cancel Chat Request");
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }
}