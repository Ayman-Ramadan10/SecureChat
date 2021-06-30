package com.w4po.securechat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsFragment extends Fragment {
    private ContactsManager manager;
    private boolean firstRun;

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        firstRun = true;
        View contactsView = inflater.inflate(R.layout.fragment_contacts, container, false);

        Context context = getContext();
        RecyclerView recyclerView = contactsView.findViewById(R.id.lstContacts);
        String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference childMonitorRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);

        manager = new ContactsManager(context, recyclerView, childMonitorRef, '1');

        return contactsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (firstRun)
            return;

        manager.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        firstRun = false;

        manager.onPause();
    }
}