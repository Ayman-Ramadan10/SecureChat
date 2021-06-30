package com.w4po.securechat;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ChatsFragment extends Fragment {
    private ContactsManager manager;
    private boolean firstRun;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        firstRun = true;
        View chatsView = inflater.inflate(R.layout.fragment_chats, container, false);

        Context context = getContext();
        RecyclerView recyclerView = chatsView.findViewById(R.id.lstChats);
        String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference childMonitorRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(currentUserID);

        manager = new ContactsManager(context, recyclerView, childMonitorRef, '0');

        ArrayList<String> chatUIDs = Helper.getChatsUIDs(currentUserID);
        if (chatUIDs != null) {
            for (int i = 0; i < chatUIDs.size(); i++)
                manager.onNewChild(chatUIDs.get(i));
        }

        return chatsView;
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