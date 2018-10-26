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
import com.student.pro.prostudent.Adapters.AdapterNote;

import com.student.pro.prostudent.Adapters.AdapterNoteHome;
import com.student.pro.prostudent.Adapters.SectionedAdapter;
import com.student.pro.prostudent.Comparators.CustomCompareNotes;
import com.student.pro.prostudent.Comparators.CustomCompareNotes2;
import com.student.pro.prostudent.Objects.Course_Class;
import com.student.pro.prostudent.Objects.Disciplines;
import com.student.pro.prostudent.Objects.Notes;
import com.student.pro.prostudent.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.student.pro.prostudent.Activities.HomeActivity.containsId;

public class MyNotesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolbar;
    private DatabaseReference mDatabase, mDB_Professor, mDB_Notes;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String UserID, TAG = "NotesHomeLog";
    private RecyclerView mRecyclerNotes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mynotes);

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
        toolbarTitle.setText(R.string.title_my_notes);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        UserID = user.getUid();
        mDB_Notes = FirebaseDatabase.getInstance().getReference().child("notes");
        mDB_Professor = FirebaseDatabase.getInstance().getReference("professors_ucs/" + UserID + "/course");


        NavigationView nav = findViewById(R.id.nav_view);
        Menu menu = nav.getMenu();
        menu.findItem(R.id.nav_notes).setVisible(false);
        getUserData();
        getCoursesProfessor();

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



    //Fetch the courses associated with the professor
    private void getCoursesProfessor() {
        mDB_Professor.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Course_Class> courses = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    for (DataSnapshot postpostSnap : postSnapshot.child("ucs").getChildren()) {
                        Course_Class course = new Course_Class();
                        course.setCourse_id(postSnapshot.getKey());
                        course.setClass_id(postpostSnap.child("id_disc").getValue().toString());
                        courses.add(course);
                    }
                }
                getClassesProfessor(courses);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    //Fetch classes associated with all the professor courses
    private void getClassesProfessor(final ArrayList<Course_Class> courses) {

        mDatabase = FirebaseDatabase.getInstance().getReference("courses/course/");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Disciplines> ucs = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    //Saves time but not needed
                    if (containsId(courses, postSnapshot.getKey())) {

                        for (int i = 0; i < courses.size(); i++) {
                            for (DataSnapshot postpostSnap : postSnapshot.child("ucs").getChildren()) {
                                if (courses.get(i).getClass_id().equals(postpostSnap.getKey()) && courses.get(i).getCourse_id().toString().equals(postSnapshot.getKey())) {
                                    String name, year, tag, id;
                                    name = postpostSnap.child("name").getValue().toString();
                                    year = postpostSnap.child("year").getValue().toString();
                                    tag = postpostSnap.child("short").getValue().toString();
                                    id = postpostSnap.getKey().toString();
                                    Disciplines uc = new Disciplines(name, year, tag, id);
                                    ucs.add(uc);
                                    break;
                                }
                            }
                        }
                    }
                }
                getNotes(ucs);
                mDatabase.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //Retrieve the Notes data from the Database
    private void getNotes(final ArrayList<Disciplines> ucs) {
        final ArrayList<Notes> notes = new ArrayList<>();
        mDB_Notes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String title, content, date, id_disc, tag_disc;
                    for (DataSnapshot postpostSnap : postSnapshot.getChildren()) {
                        for (int i = 0; i < ucs.size(); i++) {
                            if (ucs.get(i).getId().toString().equals(postpostSnap.child("id_disc").getValue().toString()) &&
                                    ucs.get(i).getTag().toString().equals(postpostSnap.child("tag_disc").getValue().toString())) {
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

        Collections.sort(notes, new CustomCompareNotes2());

        Log.d(TAG, "initRecyclerNote: = "+notes.get(0).getTag_disc());
        Log.d(TAG, String.valueOf(notes.size()));
        mRecyclerNotes = findViewById(R.id.recycler_mnotes);
        // Collections.sort(notes,new CustomCompareNotes());
        // Creating our adapter which inherits our Array and context
        AdapterNoteHome adapter = new AdapterNoteHome(notes, this, "professor");
        List<SectionedAdapter.Section> sections =
                new ArrayList<>();

        sections.add(new SectionedAdapter.Section(0, "My Notes"));
        SectionedAdapter.Section[] dummy = new SectionedAdapter.Section[sections.size()];
        SectionedAdapter mSectionedAdapter = new
                SectionedAdapter(this, R.layout.header_section, R.id.section_header, adapter);
        mSectionedAdapter.setSections(sections.toArray(dummy));
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

        Intent intent = new Intent(MyNotesActivity.this, HomeActivity.class);
        intent.putExtra("Status", "student");
        startActivity(intent);
        finish();
    }

    private void sendtoFav() {
        Intent intent = new Intent(MyNotesActivity.this, FavoritesActivity.class);
        startActivity(intent);
        finish();
    }


    private void sendtoNotes() {
    }

    private void sendtoProfile() {
        Intent intent = new Intent(MyNotesActivity.this, ProfileActivity.class);
        intent.putExtra("Status", "student");
        startActivity(intent);
        finish();
    }

    private void sendtoSettings() {
        Intent intent = new Intent(MyNotesActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void sendtoLogin() {
        mAuth.signOut();
        Intent intent = new Intent(MyNotesActivity.this, LoginActivity.class);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(MyNotesActivity.this);
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
