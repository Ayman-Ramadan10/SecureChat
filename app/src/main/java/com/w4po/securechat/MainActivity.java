package com.w4po.securechat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private TabsAccessorAdapter mTabsAccessorAdapter;

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        mToolbar = findViewById(R.id.toolbar_main_page);
        setSupportActionBar(mToolbar);

        mViewPager = findViewById(R.id.main_tabs_pager);
        mTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mTabsAccessorAdapter);

        mTabLayout = findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (currentUser == null) {
            Helper.startActivity(this, LoginActivity.class, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else {
            updateUserStatus("online");
            currentUserID = currentUser.getUid();
            VerifyUserExistence();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);
//        MenuCompat.setGroupDividerEnabled(menu, true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        int id = item.getItemId();
        if (id == R.id.main_logout_option) {
            updateUserStatus("offline");
            mAuth.signOut();
            Helper.startActivity(this, LoginActivity.class, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else if (id == R.id.main_settings_option) {
            Helper.startActivity(MainActivity.this, SettingsActivity.class, null);
        }
//        else if (id == R.id.main_create_group_option) {
//            requestNewGroup();
//        }
        else if (id == R.id.main_find_friends_option) {
            Helper.startActivity(MainActivity.this, FindFriendsActivity.class, null);
        }

        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
//
//        if (currentUser != null) {
//            updateUserStatus("offline");
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (currentUser != null) {
            updateUserStatus("offline");
        }
    }

    private void VerifyUserExistence() {
        Query query = rootRef.child("Users").child(currentUserID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserKey userKeys = Helper.getUserKeys(currentUserID);
                    Boolean hasName = dataSnapshot.child("name").exists();
                    String firebasePK = dataSnapshot.child("publicKey").getValue(String.class);
                    Boolean hasPK = firebasePK != null && userKeys != null;

//                    if (hasName && hasPK && firebasePK.equals(userKeys.publicKey)) {
//                        Helper.makeToast(MainActivity.this, "Welcome", 0);
//                    } else {
                    if (!hasName || !hasPK || !firebasePK.equals(userKeys.publicKey)) {
                        Helper.startActivity(MainActivity.this, SettingsActivity.class, null);
                    }
                } else {
                    Helper.startActivity(MainActivity.this, SettingsActivity.class, null);
                }

                query.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateUserStatus(String state) {
        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("timestamp", ServerValue.TIMESTAMP);
        onlineStateMap.put("state", state);

        rootRef.child("Users").child(currentUserID).child("userState").updateChildren(onlineStateMap);
    }
}