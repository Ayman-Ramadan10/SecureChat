package com.w4po.securechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private ValueEventListener lastSeenVEListener;
    private String otherUserID, otherUserName, otherUserImage, otherUserPK, currentUserID;

    private TextView txtUserName, txtUserLastSeen;
    private CircleImageView imgUserImage;

    private Toolbar ChatToolBar;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef, currentChatRef, otherChatRef;

    private ImageButton btnSendMessage;
    private EditText txtInputMessage;

    private final List<Message> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView recyclerMessagesV;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initialize() {
        ChatToolBar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        txtUserName = findViewById(R.id.custom_profile_name);
        txtUserLastSeen = findViewById(R.id.custom_user_last_seen);
        imgUserImage = findViewById(R.id.custom_profile_image);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        txtInputMessage = findViewById(R.id.txtInputMessage);

        findViewById(R.id.custom_chat_bar).setOnClickListener(v -> {
            Intent profileIntent = new Intent(ChatActivity.this, ProfileActivity.class);
            profileIntent.putExtra("visit_user_id", otherUserID);
            startActivity(profileIntent);
        });

        linearLayoutManager = new LinearLayoutManager(this);
        messageAdapter = new MessageAdapter(this, messagesList, otherUserImage);

        recyclerMessagesV = findViewById(R.id.private_messages_list_of_users);
        recyclerMessagesV.setLayoutManager(linearLayoutManager);
        recyclerMessagesV.setAdapter(messageAdapter);

        btnSendMessage.setOnClickListener(view -> SendMessage());

        txtUserName.setText(otherUserName);
        Picasso.get().load(otherUserImage).placeholder(R.drawable.profile_image).into(imgUserImage);

        if (otherUserPK == null) {
            txtInputMessage.setEnabled(false);
            btnSendMessage.setEnabled(false);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Warning! please ask the user to activate PGP Encryption first!");
            CharSequence options[] = new CharSequence[]{"OK"};
            builder.setItems(options, (dialogInterface, i) -> onBackPressed());
            builder.setOnCancelListener((d) -> onBackPressed());
            builder.show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        otherUserID = getIntent().getExtras().getString("visit_user_id");
        otherUserName = getIntent().getExtras().getString("visit_user_name");
        otherUserImage = getIntent().getExtras().getString("visit_image");
        otherUserPK = getIntent().getExtras().getString("visit_user_PK");

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        currentChatRef = rootRef.child("Messages").child(currentUserID).child(otherUserID);
        otherChatRef = rootRef.child("Messages").child(otherUserID).child(currentUserID);

        initialize();
        DisplayLastSeen();


        List<Message> tmpMsgs = Helper.getMessages(currentUserID, otherUserID);
        if (tmpMsgs != null) {
            messagesList.addAll(tmpMsgs);
            messageAdapter.notifyDataSetChanged();
            recyclerMessagesV.smoothScrollToPosition(recyclerMessagesV.getAdapter().getItemCount());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rootRef.child("Users").child(otherUserID).removeEventListener(lastSeenVEListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentChatRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Message message = dataSnapshot.getValue(Message.class);

                if (!messagesList.contains(message)) {

                    String decryptedMsg = "Error!";
                    try {
                        decryptedMsg = Helper.decrypt(message.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    message.setMessage(decryptedMsg);

                    Helper.insertMessage(message);
                    currentChatRef.child(message.getMessageID()).removeValue();

                    messagesList.add(message);

                    messageAdapter.notifyDataSetChanged();

                    recyclerMessagesV.smoothScrollToPosition(recyclerMessagesV.getAdapter().getItemCount());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void DisplayLastSeen() {
        lastSeenVEListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String state = dataSnapshot.child("userState").child("state").getValue(String.class);
                if (state != null) {
                    Long timestamp = dataSnapshot.child("userState").child("timestamp").getValue(Long.class);
                    String timeStr = DateFormat.getDateTimeInstance().format(new Date(timestamp));

                    if (state.equals("online")) {
                        txtUserLastSeen.setText("online");
                    } else if (state.equals("offline")) {
                        txtUserLastSeen.setText("Last Seen: " + timeStr);
                    }
                } else {
                    txtUserLastSeen.setText("offline");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        rootRef.child("Users").child(otherUserID).addValueEventListener(lastSeenVEListener);
    }

    private void SendMessage() {
        String messageText = txtInputMessage.getText().toString();

        if (TextUtils.isEmpty(messageText)) {
            Helper.makeToast(this, "first write your message...", 0);
        } else {
            String messagePushID = otherChatRef.push().getKey();

            try {
                Message message = new Message(messagePushID, currentUserID, otherUserID, messageText);

                messageText = Helper.encrypt(messageText, otherUserPK);

                Map messageTextBody = new HashMap();
                messageTextBody.put("messageID", messagePushID);
                messageTextBody.put("from", currentUserID);
                messageTextBody.put("to", otherUserID);
                messageTextBody.put("message", messageText);
                messageTextBody.put("timestamp", ServerValue.TIMESTAMP);

                DatabaseReference timestampRef = otherChatRef.child(messagePushID).child("timestamp");
                timestampRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        message.setTimestamp(snapshot.getValue(Long.class));
                        timestampRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                otherChatRef.child(messagePushID).updateChildren(messageTextBody).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        txtInputMessage.setText("");

                        Helper.insertMessage(message);
                        messagesList.add(message);
                        messageAdapter.notifyDataSetChanged();
                        recyclerMessagesV.smoothScrollToPosition(recyclerMessagesV.getAdapter().getItemCount());

//                        Helper.makeToast(ChatActivity.this, "Message Sent Successfully...", 0);
                    } else {
                        Helper.makeToast(ChatActivity.this, "Error", 0);
                    }
                });
            } catch (Exception ex) {
                Helper.makeToast(ChatActivity.this, "Error", 0);
            }

        }
    }
}