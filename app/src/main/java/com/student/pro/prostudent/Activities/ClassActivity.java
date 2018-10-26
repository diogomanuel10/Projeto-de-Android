package com.student.pro.prostudent.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.student.pro.prostudent.Adapters.AdapterNote;
import com.student.pro.prostudent.Adapters.AdapterTicket;
import com.student.pro.prostudent.Comparators.CustomCompareNotes;
import com.student.pro.prostudent.Objects.Notes;
import com.student.pro.prostudent.Objects.Tickets;
import com.student.pro.prostudent.R;

import java.util.ArrayList;
import java.util.Collections;

public class ClassActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    //Toolbar
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolbar;
    //Firebase
    private DatabaseReference mDB_Disciplines, mDB_Notes, mDB_Tickets;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("notes_read");

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    //Variables
    private String user_id, discipline, disc_key, status;
    private String TAG = "ClassLog";
    //Elements
    private RecyclerView mRecyclerNotes, mRecyclerTickets;
    private AdapterTicket adapter;
    private Button ticket_add, note_add;

    /*
    Broadcast to finish activity from another activity when navigating to other pages via the Hamburguer menu
    therefore breaking the current navigation path
     */
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        Log.d(TAG, "onDestroy:");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class);
        Log.d(TAG, "onCreate: ");
        broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent intent) {
                Log.d(TAG, "RECEIVED");
                String action = intent.getAction();
                if (action.equals("finish_class")) {
                    Log.d(TAG, "onReceive: I was finished");
                    finish();
                }
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter("finish_class"));

        //Check for required extras
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            discipline = intent.getStringExtra("Discipline");
            disc_key = intent.getStringExtra("ID");
            status = intent.getStringExtra("Status");
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
        toolbarTitle.setText(discipline);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (status.equals("student")) {
            NavigationView nav = findViewById(R.id.nav_view);
            Menu menu = nav.getMenu();
            menu.findItem(R.id.nav_notes).setVisible(false);

        } else {
            NavigationView nav = findViewById(R.id.nav_view);
            Menu menu = nav.getMenu();
            menu.findItem(R.id.nav_tickets).setVisible(false);
            menu.findItem(R.id.nav_class).setVisible(false);
        }

        //Current user
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        user_id = user.getUid();

        //Database Instances
        mDB_Disciplines = FirebaseDatabase.getInstance().getReference("courses/course/0/ucs");
        mDB_Notes = FirebaseDatabase.getInstance().getReference().child("notes");
        mDB_Tickets = FirebaseDatabase.getInstance().getReference().child("tickets");


        getUserData();
        //Functions to load Notes and Tickets onto Recycler views
        getKey();
        getNotes();
        getTickets();
        //Elements & Click Listeners
        ticket_add = findViewById(R.id.ticket_add);
        ticket_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ClassActivity.this, TicketActivity.class);
                intent.putExtra("ID", disc_key);
                intent.putExtra("Discipline", discipline);
                startActivity(intent);
            }
        });
        note_add = findViewById(R.id.note_add);
        note_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ClassActivity.this, NoteActivity.class);
                intent.putExtra("ID", disc_key);
                intent.putExtra("Discipline", discipline);
                startActivity(intent);
            }
        });
        if (TextUtils.equals(status, "professor")) {
            ticket_add.setVisibility(View.GONE);
        } else {
            note_add.setVisibility(View.GONE);
        }
    }
    private DatabaseReference mDB_user;
    private void getUserData() {
        mDB_user = FirebaseDatabase.getInstance().getReference("users");

        mDB_user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String username, email, name, surname, url;
                username = dataSnapshot.child(user_id).child("username").getValue().toString();

                url = dataSnapshot.child(user_id).child("url").getValue().toString();
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

    private void getKey() {
        mDB_Disciplines.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postsnapshot : dataSnapshot.getChildren()) {
                    if (dataSnapshot.getValue().toString().equals(disc_key) &&
                            postsnapshot.child("short").getValue().toString().equals(discipline)) {
                        /*
                           A "security" check is done to confirm the Discipline authenticity against
                           the database with both ID and TAG
                           We could use the inherited ID from the previous page, but since we need to perform
                           this step we will use the ID retrieved from the database, because this way we can avoid
                           checking if the result was positive and just keep going.
                         */
                        disc_key = postsnapshot.getKey();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //Retrieve the Notes data from the Database
    private void getNotes() {
        final ArrayList<Notes> notes = new ArrayList<>();
        mDB_Notes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String title, content, date, id_disc, tag_disc;
                    for (DataSnapshot postpostSnap : postSnapshot.getChildren()) {
                        if (disc_key.equals(postpostSnap.child("id_disc").getValue().toString()) &&
                                discipline.equals(postpostSnap.child("tag_disc").getValue().toString())) {
                            title = postpostSnap.child("title").getValue().toString();
                            content = postpostSnap.child("content").getValue().toString();
                            tag_disc = postpostSnap.child("tag_disc").getValue().toString();
                            id_disc = postpostSnap.child("id_disc").getValue().toString();
                            date = postpostSnap.child("date").getValue().toString();
                            Notes note = new Notes(title, content, tag_disc, id_disc, date);
                            note.setNote_id(postpostSnap.getKey());
                            notes.add(note);
                        }
                    }
                }
                initRecyclerNote(notes);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //Initialize Notes Recycler View
    private void initRecyclerNote(ArrayList<Notes> notes) {

        Collections.sort(notes, new CustomCompareNotes());

        mRecyclerNotes = findViewById(R.id.notes_recyler);
        // Collections.sort(notes,new CustomCompareNotes());
        // Creating our adapter which inherits our Array and context
        AdapterNote adapter = new AdapterNote(notes, this, status);

        //Display message for an empty recycler
        TextView empty = findViewById(R.id.empty_view);
        if (adapter.getItemCount() == 0) {
            empty.setVisibility(View.VISIBLE);
        } else {
            empty.setVisibility(View.GONE);
        }
        //Setting the adapter to the recycler
        mRecyclerNotes.setAdapter(adapter);
        //Setting the layout manager
        mRecyclerNotes.setLayoutManager(new LinearLayoutManager(this));
    }

    private void getTickets() {
        mDB_Tickets.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Tickets> tickets = new ArrayList<>();
                for (DataSnapshot postsnapshot : dataSnapshot.getChildren()) {
                    String ititle, icontent, iuser_id, iprivate, idate, id_disc, tag_disc, iurl, ticket_id, isolved;
                    for (DataSnapshot postpostsnap : postsnapshot.getChildren())
                        if (status.equals("professor")) {
                            if (disc_key.equals(postpostsnap.child("id_disc").getValue().toString()) &&
                                    discipline.equals(postpostsnap.child("tag_disc").getValue().toString())) {
                                ititle = postpostsnap.child("title").getValue().toString();
                                icontent = postpostsnap.child("content").getValue().toString();
                                iprivate = postpostsnap.child("sprivate").getValue().toString();
                                iuser_id = postpostsnap.child("user_id").getValue().toString();
                                tag_disc = postpostsnap.child("tag_disc").getValue().toString();
                                id_disc = postpostsnap.child("id_disc").getValue().toString();
                                idate = postpostsnap.child("date").getValue().toString();
                                iurl = postpostsnap.child("url").getValue().toString();
                                isolved = postpostsnap.child("solved").getValue().toString();
                                Tickets ticket = new Tickets(ititle, icontent, iprivate,
                                        iuser_id, id_disc, tag_disc, idate, iurl, isolved);
                                ticket.setTicket_id(postpostsnap.getKey());
                                tickets.add(ticket);
                            }
                        } else {
                            if (postpostsnap.child("sprivate").getValue().toString().equals("false")) {
                                if (disc_key.equals(postpostsnap.child("id_disc").getValue().toString()) &&
                                        discipline.equals(postpostsnap.child("tag_disc").getValue().toString())) {
                                    ititle = postpostsnap.child("title").getValue().toString();
                                    icontent = postpostsnap.child("content").getValue().toString();
                                    iprivate = postpostsnap.child("sprivate").getValue().toString();
                                    iuser_id = postpostsnap.child("user_id").getValue().toString();
                                    tag_disc = postpostsnap.child("tag_disc").getValue().toString();
                                    id_disc = postpostsnap.child("id_disc").getValue().toString();
                                    idate = postpostsnap.child("date").getValue().toString();
                                    iurl = postpostsnap.child("url").getValue().toString();
                                    isolved = postpostsnap.child("solved").getValue().toString();
                                    Tickets ticket = new Tickets(ititle, icontent, iprivate,
                                            iuser_id, id_disc, tag_disc, idate, iurl, isolved);
                                    ticket.setTicket_id(postpostsnap.getKey());
                                    tickets.add(ticket);
                                }
                            } else if (postpostsnap.child("user_id").getValue().toString().equals(user_id) && postpostsnap.child("sprivate").getValue().toString().equals("true")) {
                                if (disc_key.equals(postpostsnap.child("id_disc").getValue().toString()) &&
                                        discipline.equals(postpostsnap.child("tag_disc").getValue().toString())) {
                                    ititle = postpostsnap.child("title").getValue().toString();
                                    icontent = postpostsnap.child("content").getValue().toString();
                                    iprivate = postpostsnap.child("sprivate").getValue().toString();
                                    iuser_id = postpostsnap.child("user_id").getValue().toString();
                                    tag_disc = postpostsnap.child("tag_disc").getValue().toString();
                                    id_disc = postpostsnap.child("id_disc").getValue().toString();
                                    idate = postpostsnap.child("date").getValue().toString();
                                    iurl = postpostsnap.child("url").getValue().toString();
                                    isolved = postpostsnap.child("solved").getValue().toString();
                                    Tickets ticket = new Tickets(ititle, icontent, iprivate,
                                            iuser_id, id_disc, tag_disc, idate, iurl, isolved);
                                    ticket.setTicket_id(postpostsnap.getKey());
                                    tickets.add(ticket);
                                }
                            }
                        }

                }
                if(status.equals("student"))
                {
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ClassActivity.this);
                    final Boolean mPref = sharedPref.getBoolean("tickets_solved", false);
                    for (int i = 0; i < tickets.size(); i++) {
                        if (mPref && tickets.get(i).getSolved().toString().equals("false") && !tickets.get(i).getUser_id().toString().equals(user_id)) {
                            Log.d(TAG, "Removed Ticket");
                            tickets.remove(i);
                            i=i-1;
                        }
                    }
                }
                else
                {
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ClassActivity.this);
                    final Boolean mPref = sharedPref.getBoolean("tickets_solved_professor", false);
                    for (int i = 0; i < tickets.size(); i++) {
                        
                        if (mPref && tickets.get(i).getSolved().toString().equals("true")) {
                            tickets.remove(i);
                            i=i-1;
                        }
                    }
                }
                initRecyclerTicket(tickets);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initRecyclerTicket(ArrayList<Tickets> tickets) {
        TextView empty = findViewById(R.id.empty_view_tickets);

        mRecyclerTickets = findViewById(R.id.tickets_recycler);

        adapter = new AdapterTicket(tickets, this, status);

        if (adapter.getItemCount() == 0)

        {
            empty.setVisibility(View.VISIBLE);
        } else

        {
            empty.setVisibility(View.GONE);
        }
        mRecyclerTickets.setAdapter(adapter);
        mRecyclerTickets.setLayoutManager(new

                LinearLayoutManager(this));
    }


    private void setNavigationViewListener() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.navigation_menu, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Management of navigation view item clicks.
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

        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendtoHome() {
        Intent intent = new Intent(ClassActivity.this, HomeActivity.class);
        intent.putExtra("Status", status);
        startActivity(intent);
        finish();
    }

    private void sendtoFav() {
        Intent intent = new Intent(ClassActivity.this, FavoritesActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendtoNotes() {
        Intent intent = new Intent(ClassActivity.this, MyNotesActivity.class);
        startActivity(intent);
        finish();    }

    private void sendtoTickets() {
        Intent intent = new Intent(ClassActivity.this, MyTicketsActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendtoProfile() {
        Intent intent = new Intent(ClassActivity.this, ProfileActivity.class);
        intent.putExtra("Status", status);
        startActivity(intent);
        finish();
    }

    private void sendtoSettings() {
        Intent intent;
        if (status.equals("student")) {
            intent = new Intent(ClassActivity.this, SettingsActivity.class);

        } else {
            intent = new Intent(ClassActivity.this, SettingsActivityProfessor.class);

        }
        startActivity(intent);
    }

    private void sendtoLogin() {
        mAuth.signOut();
        Intent intent = new Intent(ClassActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public void onBackPressed() {
        // Checks preferences to determine main screen and acts accordingly
        if (status.equals("student")) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String mPref = sharedPref.getString("home_list", "0");
            switch (mPref) {
                case "0":
                    sendtoHome();
                    break;
                case "1":
                    sendtoFav();
                    break;
                case "2":
                    sendtoTickets();
                    break;
            }
        } else {
            sendtoHome();
        }
    }

}
