package com.w4po.securechat;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestsFragment extends Fragment {

    private View RequestsFragmentView;
    private RecyclerView myRequestsList;

    private DatabaseReference ChatRequestsRef, UsersRef, ContactsRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    public RequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestsFragmentView = inflater.inflate(R.layout.fragment_requests, container, false);


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");


        myRequestsList = RequestsFragmentView.findViewById(R.id.lstChatRequests);
        myRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return RequestsFragmentView;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contact> options =
                new FirebaseRecyclerOptions.Builder<Contact>()
                        .setQuery(ChatRequestsRef.child(currentUserID), Contact.class)
                        .build();


        FirebaseRecyclerAdapter<Contact, RequestsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contact, RequestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull Contact model) {
                        Button btnRequestAccept = holder.itemView.findViewById(R.id.btnRequestAcceptPT);
                        Button btnRequestCancel = holder.itemView.findViewById(R.id.btnRequestCancelPT);

                        btnRequestAccept.setVisibility(View.VISIBLE);
                        btnRequestCancel.setVisibility(View.VISIBLE);


                        final String list_user_id = getRef(position).getKey();

                        DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String type = dataSnapshot.getValue(String.class);

                                    if (type.equals("received")) {
                                        btnRequestAccept.setOnClickListener(v -> acceptContact(list_user_id));
                                        btnRequestCancel.setOnClickListener(v -> cancelContact(list_user_id, "Contact deleted"));

                                        UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild("image")) {
                                                    final String requestProfileImage = dataSnapshot.child("image").getValue(String.class);

                                                    Picasso.get().load(requestProfileImage).into(holder.profileImage);
                                                }

                                                final String requestUserName = dataSnapshot.child("name").getValue(String.class);

                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText("wants to connect with you.");


                                                holder.itemView.setOnClickListener(view -> {
                                                    CharSequence options1[] = new CharSequence[]
                                                            {
                                                                    "Accept",
                                                                    "Cancel"
                                                            };

                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                    builder.setTitle(requestUserName + "  Chat Request");

                                                    builder.setItems(options1, (dialogInterface, i) -> {
                                                        switch (i) {
                                                            case 0: {
                                                                acceptContact(list_user_id);
                                                                break;
                                                            }
                                                            case 1: {
                                                                cancelContact(list_user_id, "Contact deleted");
                                                                break;
                                                            }
                                                        }
                                                    });
                                                    builder.show();
                                                });

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    } else if (type.equals("sent")) {
                                        btnRequestCancel.setVisibility(View.INVISIBLE);
                                        btnRequestAccept.setText("Sent");
                                        btnRequestAccept.setOnClickListener(view -> cancelSentReq(list_user_id));

                                        UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild("image")) {
                                                    final String requestProfileImage = dataSnapshot.child("image").getValue(String.class);

                                                    Picasso.get().load(requestProfileImage).into(holder.profileImage);
                                                }

                                                final String requestUserName = dataSnapshot.child("name").getValue(String.class);

                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText("You have sent a request to " + requestUserName);


                                                holder.itemView.setOnClickListener(view -> cancelSentReq(list_user_id));

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        RequestsViewHolder holder = new RequestsViewHolder(view);
                        return holder;
                    }
                };

        myRequestsList.setAdapter(adapter);
        adapter.startListening();
    }

    private void acceptContact(String otherUID) {
        ContactsRef.child(currentUserID).child(otherUID).child("Contact")
                .setValue("Saved").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ContactsRef.child(otherUID).child(currentUserID).child("Contact")
                        .setValue("Saved").addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        ChatRequestsRef.child(currentUserID).child(otherUID)
                                .removeValue()
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        ChatRequestsRef.child(otherUID).child(currentUserID)
                                                .removeValue()
                                                .addOnCompleteListener(task3 -> {
                                                    if (task3.isSuccessful()) {
                                                        Helper.makeToast(getContext(), "New contact saved.", 0);
                                                    }
                                                });
                                    }
                                });
                    }
                });
            }
        });
    }
    private void cancelContact(String otherUID, String msg) {
        ChatRequestsRef.child(currentUserID).child(otherUID)
                .removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ChatRequestsRef.child(otherUID).child(currentUserID)
                                .removeValue()
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        Helper.makeToast(getContext(), msg, 0);
                                    }
                                });
                    }
                });
    }
    private void cancelSentReq(String otherUID) {
        CharSequence options12[] = new CharSequence[]
                {
                        "Cancel the request."
                };

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Are you sure?");

        builder.setItems(options12, (dialogInterface, i) -> {
            if (i == 0) {
                cancelContact(otherUID, "You have cancelled the chat request.");
            }
        });
        builder.show();
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;
        CircleImageView profileImage;
        Button AcceptButton, CancelButton;


        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);


            userName = itemView.findViewById(R.id.txtNamePT);
            userStatus = itemView.findViewById(R.id.txtUserStatusPT);
            profileImage = itemView.findViewById(R.id.imgUserPicPT);
            AcceptButton = itemView.findViewById(R.id.btnRequestAcceptPT);
            CancelButton = itemView.findViewById(R.id.btnRequestCancelPT);
        }
    }
}