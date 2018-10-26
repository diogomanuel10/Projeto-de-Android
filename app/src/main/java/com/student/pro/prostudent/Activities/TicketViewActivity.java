package com.student.pro.prostudent.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.student.pro.prostudent.Adapters.AdapterChat;
import com.student.pro.prostudent.Objects.Chat;
import com.student.pro.prostudent.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TicketViewActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolbar;
    private DatabaseReference mDatabase, mDB_Ticket, mDB_Ticket_Response;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String UserID, TAG = "TicketViewLog";
    private String message, ticket_id, ticket_Uid, status, date, solved;
    private RecyclerView mView;
    private EditText response;
    private TextView title;
    private Button sendRes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_view);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            message = intent.getStringExtra("Message");
            ticket_id = intent.getStringExtra("TicketID");
            ticket_Uid = intent.getStringExtra("UserID");
            status = intent.getStringExtra("Status");
            date = intent.getStringExtra("Date");
            solved = intent.getStringExtra("Solved");
        } else {
             /*
              This page needs to inherit information about the discipline selected
              if no information is found the user is returned to the home page
             */
            sendtoHome();
        }

        //Toolbar
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
        toolbarTitle.setText(R.string.label_tickets);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //User
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        UserID = user.getUid();
        //Instances
        mDB_Ticket = FirebaseDatabase.getInstance().getReference("tickets_chat").child(ticket_id);
        mDatabase = FirebaseDatabase.getInstance().getReference("users").child(UserID);
        //Elements
        title = findViewById(R.id.title_ticket);
        title.setText(intent.getStringExtra("Title"));
        response = findViewById(R.id.response_content);
        sendRes = findViewById(R.id.response_send);
        getUserData();

        Chat message1 = new Chat(message, date, "student");
        getChat(message1);
        if (solved.equals("false")) {
            sendRes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendResponse();
                    InputMethodManager inputManager = (InputMethodManager)
                            getSystemService(TicketViewActivity.this.INPUT_METHOD_SERVICE);

                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);

                }
            });

        } else {
            response.setVisibility(View.GONE);
            sendRes.setVisibility(View.GONE
            );
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

    private void getChat(final Chat message1) {
        mDB_Ticket.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Chat> messages = new ArrayList<>();
                messages.add(message1);
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    String content, date, sender;
                    content = postSnapshot.child("content").getValue().toString();
                    date = postSnapshot.child("date").getValue().toString();
                    sender = postSnapshot.child("sender").getValue().toString();
                    Chat message = new Chat(content, date, sender);
                    messages.add(message);

                }
                initRecycler(messages);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void initRecycler(ArrayList<Chat> messages) {
        mView = findViewById(R.id.ticket_view_recycler);
        AdapterChat adapter = new AdapterChat(messages, this, status);
        mView.setAdapter(adapter);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        mView.setLayoutManager(manager);

    }

    private void sendResponse() {

        final String[] sender = new String[1];
        if (!response.getText().toString().isEmpty()) {
            Date currentDate = Calendar.getInstance().getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            final String idate = sdf.format(currentDate).toString();
            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    sender[0] = dataSnapshot.child("status").getValue().toString();
                    Chat message = new Chat(response.getText().toString(), idate, sender[0]);
                    mDB_Ticket.push().setValue(message);
                    response.setText("");

                    mDatabase.removeEventListener(this);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            });

        }

    }

    private void setNavigationViewListener() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation_menu, menu);
        return true;
    }

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

       /* if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }

    private void sendtoHome() {
        //Finishes Class Activity that was left Open
        Intent intent_finish = new Intent("finish_class");
        sendBroadcast(intent_finish);
        Intent intent = new Intent(TicketViewActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendtoFav() {
        //Finishes Class Activity that was left Open
        Intent intent_finish = new Intent("finish_class");
        sendBroadcast(intent_finish);
        //send to favorites
    }

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
        Intent intent = new Intent(TicketViewActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    private void sendtoSettings() {
        //Finishes Class Activity that was left Open
        Intent intent_finish = new Intent("finish_class");
        sendBroadcast(intent_finish);
        Intent intent = new Intent(TicketViewActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void sendtoLogin() {
        //Finishes Class Activity that was left Open
        Intent intent_finish = new Intent("finish_class");
        sendBroadcast(intent_finish);
        mAuth.signOut();
        Intent intent = new Intent(TicketViewActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public void onBackPressed() {
        finish();
    }
}
