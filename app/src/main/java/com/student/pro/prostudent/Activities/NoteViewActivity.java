package com.student.pro.prostudent.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.student.pro.prostudent.Objects.Notes;
import com.student.pro.prostudent.R;

import java.util.ArrayList;

public class NoteViewActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    //Toolbar & Nav
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolbar;
    //Elements
    private TextView title, message;
    private Switch readS;
    //Firebase
    private DatabaseReference mDatabase, mDB_Notes;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    //Variables
    private String TAG = "NoteViewLog";
    private String UserID, note_key,read,status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_view);
        //Check for Extras
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            note_key = intent.getStringExtra("ID");
            read= intent.getStringExtra("Read");
            status=intent.getStringExtra("Status");
        } else {
             /*
              This page needs to inherit information about the discipline selected
              if no information is found the user is returned to the home page
             */
            sendtoHome();
        }

        //Toolbar & Drawer
        setNavigationViewListener();
        mToolbar = findViewById(R.id.nav_action);
        setSupportActionBar(mToolbar);
        mDrawerLayout = findViewById(R.id.drawerlayout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView toolbarTitle = null;
        for (int i = 0; i < mToolbar.getChildCount(); ++i) {
            View child = mToolbar.getChildAt(i);
            if (child instanceof TextView) {
                toolbarTitle = (TextView) child;
                break;
            }
        }
        toolbarTitle.setText(R.string.label_notes);          getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Database Ref
        mDatabase = FirebaseDatabase.getInstance().getReference("notes_read").child(note_key);
        //Database Structure
        /*
        Notes_read
        |___Note ID
            |___Content (UserID = false or true)
         */
        mDB_Notes = FirebaseDatabase.getInstance().getReference("notes");
        //Database Structure
        /*
        Note
        |___UserID
            |___NoteID
                |___Content
         */

        //User
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        UserID = user.getUid().toString();
        //Elements
        readS = findViewById(R.id.seen_note);
        title = findViewById(R.id.title_note);
        message = findViewById(R.id.message_note);
        //Fetch note data
        getNote();
        getUserData();
        //If the note is read sets the slider button to checked
        if(status.equals("student"))
        { if(TextUtils.equals(read,"true"))
        {
            readS.setChecked(true);
        }
            readS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (isChecked) {
                        mDatabase.child(UserID).setValue("true");
                    } else {
                        mDatabase.child(UserID).removeValue();

                    }
                }
            });

        }
        else
        {
            readS.setVisibility(View.GONE);
        }
    }
    private DatabaseReference mDB_user;
    private void getUserData() {
        mDB_user = FirebaseDatabase.getInstance().getReference("users");

        mDB_user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String username, email, name, surname, url;
                username = dataSnapshot.child(UserID).child("username").getValue().toString();

                url = dataSnapshot.child(UserID).child("url").getValue().toString();
                updateUI(username, url);

            }
            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private void updateUI(String username, String url) {
        NavigationView nav = findViewById(R.id.nav_view);

        View hView =  nav.getHeaderView(0);
        TextView nav_user = hView.findViewById(R.id.header_user);
        ImageView profile_img = hView.findViewById(R.id.setup_image_header);
        nav_user.setText(username);
        Picasso.get()
                .load(url)
                .placeholder(R.drawable.default_icon)
                .error(R.drawable.default_icon)
                .into(profile_img);
    }

    private void getNote() {
        mDB_Notes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Notes> notes = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot postpostSnap : postSnapshot.getChildren()) {
                        if (postpostSnap.getKey().equals(note_key)) {
                            title.setText(postpostSnap.child("title").getValue().toString());
                            message.setText(postpostSnap.child("content").getValue().toString());
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
    //Toolbar --------------------------------------------------------------------------------------
    private void setNavigationViewListener() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation_menu, menu);
        return true;
    }
    // Handle navigation view item clicks here.
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                sendtoHome();
                break;
            case R.id.nav_class:
                sendtoFav();
                break;
            case R.id.nav_notes:
                sendtoNotes();
                break;
            case R.id.nav_tickets:
                sendtoTickets();
                break;
            case R.id.nav_account:
                sendtoProfile();
                break;
            case R.id.nav_settings:
                sendtoSettings();
                break;
            case R.id.nav_logout:
                sendtoLogin();
                break;

        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

      /*
        Menu Button Disabled - Can be enabled in the future.
        if (mToggle.onOptionsItemSelected(item)) {
            return false;
        }*/
        return super.onOptionsItemSelected(item);
    }


    private void sendtoHome() {
        //Finishes Class Activity that was left Open
        Intent intent_finish = new Intent("finish_class");
        sendBroadcast(intent_finish);
        //Redirects to Home screen
        Intent intent = new Intent(NoteViewActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendtoFav() {

        //Redirects to Home screen with a request to show Favorite Disciplines
        Intent intent = new Intent(NoteViewActivity.this, HomeActivity.class);
        intent.putExtra("Favorite_request",true);
        startActivity(intent);
        finish();    }

    private void sendtoNotes() {
        //Finishes Class Activity that was left Open
        Intent intent_finish = new Intent("finish_class");
        sendBroadcast(intent_finish);
        //Send to my notes
    }

    private void sendtoTickets() {
        //Finishes Class Activity that was left Open
        Intent intent_finish = new Intent("finish_class");
        sendBroadcast(intent_finish);
        //Send to my tickets
    }

    private void sendtoProfile() {
        //Finishes Class Activity that was left Open
        Intent intent_finish = new Intent("finish_class");
        sendBroadcast(intent_finish);
        //Redirects to profile screen
        Intent intent = new Intent(NoteViewActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    private void sendtoSettings() {
        //Finishes Class Activity that was left Open
        Intent intent_finish = new Intent("finish_class");
        sendBroadcast(intent_finish);
        //Redirects to Settings Screen
        Intent intent = new Intent(NoteViewActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void sendtoLogin() {
        //Finishes Class Activity that was left Open
        Intent intent_finish = new Intent("finish_class");
        sendBroadcast(intent_finish);
        //Removes Auth Token from the device
        mAuth.signOut();
        //Redirects to Login screen
        Intent intent = new Intent(NoteViewActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    //Toolbar End ----------------------------------------------------------------------------------
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
