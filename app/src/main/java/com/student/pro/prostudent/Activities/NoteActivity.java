package com.student.pro.prostudent.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class NoteActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    //Toolbar & Nav
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolbar;
    //Elements
    private EditText title, message;
    private Button sendBut;
    private ProgressBar note_bar;
    //Firebase
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    //Variables
    private String TAG = "NoteLog";
    private String UserID, discipline, disc_key, note_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        //Check For Extras
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            discipline = intent.getStringExtra("Discipline");
            disc_key = intent.getStringExtra("ID");

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
        toolbarTitle.setText(R.string.label_notes);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        //Database Ref
        mDatabase = FirebaseDatabase.getInstance().getReference("notes");
        //User
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        UserID = user.getUid().toString();
        //Elements
        note_bar = findViewById(R.id.note_bar);
        sendBut = findViewById(R.id.note_send);
        title = findViewById(R.id.note_title);
        message = findViewById(R.id.note_content);
        sendBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendNote();
            }
        });
        getUserData();
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

    private void sendNote() {
        //Set progress bar Visible
        note_bar.setVisibility(View.VISIBLE);

        String ititle, icontent, idate;
        //Check for valid Inputs
        if (!title.getText().toString().isEmpty() && !message.getText().toString().isEmpty()) {

            //Get current system date
            Date currentDate = Calendar.getInstance().getTime();
            //Define date format
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            //Parse date to defined format
            idate = sdf.format(currentDate).toString();
            //Get user input
            ititle = title.getText().toString();
            icontent = message.getText().toString();
            //Instantiate Notes Object
            Notes note = new Notes(ititle, icontent, discipline, disc_key, idate);
            //Push new key under current user and set Note value
            mDatabase.child(UserID).push().setValue(note);
            //Set progress bar invisible
            note_bar.setVisibility(View.INVISIBLE);
            //Finish activity
            finish();
        }

    }
    //Toolbar --------------------------------------------------------------------------------------
    private void setNavigationViewListener() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    //Inflate custom Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation_menu, menu);
        return true;
    }


    //Menu Item click management
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
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
        Intent intent = new Intent(NoteActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendtoFav() {
        //Redirects to Home screen with a request to show Favorite Disciplines
        Intent intent = new Intent(NoteActivity.this, HomeActivity.class);
        intent.putExtra("Favorite_request",true);
        startActivity(intent);
        finish();
    }

    private void sendtoNotes() {
        //Send to my notes
    }

    private void sendtoTickets() {
        //Send to my tickets
    }

    private void sendtoProfile() {
        Intent intent = new Intent(NoteActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    private void sendtoSettings() {
        Intent intent = new Intent(NoteActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void sendtoLogin() {
        mAuth.signOut();
        Intent intent = new Intent(NoteActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    //Toolbar End-----------------------------------------------------------------------------------
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

}
