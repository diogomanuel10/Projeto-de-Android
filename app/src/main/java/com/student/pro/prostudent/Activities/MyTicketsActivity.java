package com.student.pro.prostudent.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.student.pro.prostudent.Adapters.AdapterTicketHome;
import com.student.pro.prostudent.Adapters.SectionedAdapter;
import com.student.pro.prostudent.Objects.Tickets;
import com.student.pro.prostudent.R;

import java.util.ArrayList;
import java.util.List;

public class MyTicketsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolbar;
    private DatabaseReference mDB_Tickets;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String UserID, TAG = "Favlog";
    private RecyclerView mView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mytickets);

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
        toolbarTitle.setText(R.string.title_my_tickets);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        UserID = user.getUid();
        mDB_Tickets = FirebaseDatabase.getInstance().getReference().child("tickets");

        NavigationView nav = findViewById(R.id.nav_view);
        Menu menu = nav.getMenu();
        menu.findItem(R.id.nav_notes).setVisible(false);

        getUserData();
        getTickets();
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

    private void getTickets() {
        Log.d(TAG, "getTickets:");
        mDB_Tickets.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Tickets> tickets = new ArrayList<>();

                for (DataSnapshot postsnapshot : dataSnapshot.getChildren()) {
                    String ititle, icontent, iuser_id, iprivate, idate, id_disc, tag_disc, iurl, ticket_id,isolved;
                    for (DataSnapshot postpostsnap : postsnapshot.getChildren())

                        if (UserID.equals(postpostsnap.child("user_id").getValue().toString())) {
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
                                    iuser_id, id_disc, tag_disc, idate, iurl,isolved);
                            ticket.setTicket_id(postpostsnap.getKey());
                            tickets.add(ticket);
                        }
                }
                Log.d(TAG, "onDataChange: GET TICKET DATACHANGE");
                initRecyclerTicket(tickets);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initRecyclerTicket(ArrayList<Tickets> tickets) {
        mView = findViewById(R.id.recycler_mtickets);

        AdapterTicketHome adapter = new AdapterTicketHome(tickets, this, "student");

        List<SectionedAdapter.Section> sections =
                new ArrayList<>();
        sections.add(new SectionedAdapter.Section(0, "My tickets"));
        SectionedAdapter.Section[] dummy = new SectionedAdapter.Section[sections.size()];
        SectionedAdapter mSectionedAdapter = new
                SectionedAdapter(this, R.layout.header_section, R.id.section_header, adapter);
        mSectionedAdapter.setSections(sections.toArray(dummy));

        mView.setAdapter(mSectionedAdapter);
        mView.setLayoutManager(new LinearLayoutManager(this));
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

        Intent intent = new Intent(MyTicketsActivity.this, HomeActivity.class);
        intent.putExtra("Status", "student");
        startActivity(intent);
        finish();
    }

    private void sendtoFav() {
        Intent intent = new Intent(MyTicketsActivity.this, FavoritesActivity.class);
        startActivity(intent);
        finish();
    }


    private void sendtoTickets() {
        getTickets();
    }

    private void sendtoProfile() {
        Intent intent = new Intent(MyTicketsActivity.this, ProfileActivity.class);
        intent.putExtra("Status","student");
        startActivity(intent);
        finish();
    }

    private void sendtoSettings() {
        Intent intent = new Intent(MyTicketsActivity.this, SettingsActivity.class);
        startActivity(intent);
     }

    private void sendtoLogin() {
        mAuth.signOut();
        Intent intent = new Intent(MyTicketsActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public void onBackPressed() {
        // Checks preferences to determine main screen and acts accordingly
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
                AlertDialog.Builder builder = new AlertDialog.Builder(MyTicketsActivity.this);
                builder.setTitle("Do you want to log out?");
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.action_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        sendtoLogin();
                    }
                });
                builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
                break;
        }
    }
}
