package com.w4po.securechat;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ContactsManager {
    private DatabaseReference childMonitorRef, usersRef;

    private ChildEventListener newContactCEListener;
    private HashMap<String, ValueEventListener> contactsVEListeners;

    private Context context;
    private ArrayList<Contact> contactsList;
    private ContactAdapter contactAdapter;

    public ContactsManager (Context context, RecyclerView recyclerView, DatabaseReference childMonitorRef, char type) {
        this.context = context;
        contactsList = new ArrayList<>();
        contactsVEListeners = new HashMap<>();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        this.childMonitorRef = childMonitorRef;

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        contactAdapter = new ContactAdapter(context, contactsList, type);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(contactAdapter);

        Drawable divider = context.getResources().getDrawable(R.drawable.divider);
        RecyclerView.ItemDecoration itemDecorator = new SimpleDividerItemDecoration(divider, 250,1030);
        recyclerView.addItemDecoration(itemDecorator);

        newContactCEListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String uid = snapshot.getKey();
                onNewChild(uid);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        childMonitorRef.addChildEventListener(newContactCEListener);
    }

    public void onPause() {
        childMonitorRef.removeEventListener(newContactCEListener);

        String[] keys = contactsVEListeners.keySet().toArray(new String[contactsVEListeners.size()]);
        for (String uid : keys) {
            usersRef.child(uid).removeEventListener(contactsVEListeners.get(uid));
        }
    }

    public void onResume() {
        childMonitorRef.addChildEventListener(newContactCEListener);

        String[] keys = contactsVEListeners.keySet().toArray(new String[contactsVEListeners.size()]);
        for (String uid : keys) {
            usersRef.child(uid).addValueEventListener(contactsVEListeners.get(uid));
        }
    }


    public void onNewChild(String uid) {
        if (!contactsVEListeners.containsKey(uid)) {
            ValueEventListener userVEListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Contact contact = snapshot.getValue(Contact.class);
                    if (contact != null) {
                        int index = contactsList.indexOf(contact);

                        if (index == -1)
                            contactsList.add(contact);
                        else
                            contactsList.set(index, contact);

                        contactAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            contactsVEListeners.put(uid, userVEListener);
            usersRef.child(uid).addValueEventListener(userVEListener);
        }
    }
}
