package com.student.pro.prostudent.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.student.pro.prostudent.Adapters.AdapterProfessor;
import com.student.pro.prostudent.Adapters.AdapterStudent;
import com.student.pro.prostudent.Adapters.SectionedAdapter;
import com.student.pro.prostudent.Comparators.CustomCompareDiscipline;
import com.student.pro.prostudent.Objects.Course_Class;
import com.student.pro.prostudent.Objects.Disciplines;
import com.student.pro.prostudent.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolbar;
    private DatabaseReference mDatabase, mDB_Professor, mDB_Student, mDB_Tickets;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String UserID, TAG = "Homelog", mPref = "", currentStatus;
    private RecyclerView mView;

    private TextView emptyHome;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent intent) {
                String action = intent.getAction();
                if (action.equals("finish_home")) {
                    Log.d(TAG, "onReceive: I was deleted" );
                    finish();
                }
            }
        };
        HomeActivity.this.registerReceiver(broadcastReceiver, new IntentFilter("finish_home"));

        setNavigationViewListener();
        mToolbar = findViewById(R.id.nav_action);
        setSupportActionBar(mToolbar);
        mDrawerLayout = findViewById(R.id.drawerlayout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        UserID = user.getUid();
        mDB_Professor = FirebaseDatabase.getInstance().getReference("professors_ucs/" + UserID + "/course");
        mDB_Student = FirebaseDatabase.getInstance().getReference("students_courses");
        mDB_Tickets = FirebaseDatabase.getInstance().getReference().child("tickets");
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            currentStatus = intent.getStringExtra("Status");
        }
        if (currentStatus.equals("student")) {
            NavigationView nav = findViewById(R.id.nav_view);
            Menu menu = nav.getMenu();
            menu.findItem(R.id.nav_notes).setVisible(false);

        } else {
            NavigationView nav = findViewById(R.id.nav_view);
            Menu menu = nav.getMenu();
            menu.findItem(R.id.nav_tickets).setVisible(false);
            menu.findItem(R.id.nav_class).setVisible(false);
        }
        getUserData();
        getStatus();
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


    //Act based on status
    private void getStatus() {
        if (TextUtils.equals(currentStatus, "student")) {
            getCourseStudent();

        } else {
            getCoursesProfessor();
        }
        TextView toolbarTitle = null;
        for (int i = 0; i < mToolbar.getChildCount(); ++i) {
            View child = mToolbar.getChildAt(i);

            // assuming that the title is the first instance of TextView
            // you can also check if the title string matches
            if (child instanceof TextView) {
                toolbarTitle = (TextView)child;
                break;
            }
        }
        toolbarTitle.setText(R.string.title_home);
    }

    //Fetch course associated with student
    private void getCourseStudent() {
        mDB_Student.child(UserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try{
                    getClassesStudent(dataSnapshot.child("course_id").getValue().toString());
                }
                catch (NullPointerException e)
                {
                    emptyHome = findViewById(R.id.empty_view_home);
                    emptyHome.setVisibility(View.VISIBLE);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //Retrieve classes associated with student course
    private void getClassesStudent(String course) {
        mDatabase = FirebaseDatabase.getInstance().getReference("courses/course/" + course + "/ucs");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int cnt1 = 0, cnt2 = 0;
                ArrayList<Disciplines> ucs = new ArrayList<Disciplines>();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String name, year, tag, id;

                    name = postSnapshot.child("name").getValue().toString();
                    year = postSnapshot.child("year").getValue().toString();
                    tag = postSnapshot.child("short").getValue().toString();
                    id = postSnapshot.getKey().toString();
                    if (postSnapshot.child("year").getValue().toString().equals("1")) {
                        cnt1++;
                    }
                    if (postSnapshot.child("year").getValue().toString().equals("2")) {
                        cnt2++;
                    }
                    Disciplines uc = new Disciplines(name, year, tag, id);
                    ucs.add(uc);
                }

                initRecycleStudent(ucs, cnt1, cnt2);
                mDatabase.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    //Start the recycler view for the student
    private void initRecycleStudent(ArrayList<Disciplines> ucs, int cnt1, int cnt2) {
        Collections.sort(ucs, new CustomCompareDiscipline());

        mView = findViewById(R.id.recycler_class);
        AdapterStudent adapter = new AdapterStudent(ucs, this);
        List<SectionedAdapter.Section> sections =
                new ArrayList<>();

        sections.add(new SectionedAdapter.Section(0, "1st Year"));
        sections.add(new SectionedAdapter.Section(cnt1, "2nd Year"));
        sections.add(new SectionedAdapter.Section(cnt2 + cnt1, "3rd Year"));
        SectionedAdapter.Section[] dummy = new SectionedAdapter.Section[sections.size()];
        SectionedAdapter mSectionedAdapter = new
                SectionedAdapter(this, R.layout.header_section, R.id.section_header, adapter);
        mSectionedAdapter.setSections(sections.toArray(dummy));

        mView.setAdapter(mSectionedAdapter);
        mView.setLayoutManager(new LinearLayoutManager(this));
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
                initRecycleProfessor(ucs);
                mDatabase.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static boolean containsId(ArrayList<Course_Class> courses, String id) {
        for (Course_Class object : courses) {
            if (object.getCourse_id().equals(id)) {
                return true;
            }
        }
        return false;
    }

    private void initRecycleProfessor(ArrayList<Disciplines> ucs) {
        Collections.sort(ucs, new CustomCompareDiscipline());

        mView = findViewById(R.id.recycler_class);
        AdapterProfessor adapter = new AdapterProfessor(ucs, this);

        List<SectionedAdapter.Section> sections =
                new ArrayList<>();

        sections.add(new SectionedAdapter.Section(0, "My Disciplines"));
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
        //getMenuInflater().inflate(R.menu.navigation_menu, menu);
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

        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendtoHome() {
        getStatus();
    }

    private void sendtoFav() {
        Intent intent = new Intent(HomeActivity.this, FavoritesActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendtoNotes() {
        Intent intent = new Intent(HomeActivity.this, MyNotesActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendtoTickets() {
        Intent intent = new Intent(HomeActivity.this, MyTicketsActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendtoProfile() {
        Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
        intent.putExtra("Status",currentStatus);
        startActivity(intent);
        finish();
    }

    private void sendtoSettings() {
        Intent intent;
        if (currentStatus.equals("student")) {
            intent = new Intent(HomeActivity.this, SettingsActivity.class);

        } else {
            intent = new Intent(HomeActivity.this, SettingsActivityProfessor.class);

        }
        startActivity(intent);
    }

    private void sendtoLogin() {
        mAuth.signOut();
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public void onBackPressed() {
        // Checks preferences to determine main screen and acts accordingly
        if(currentStatus.equals("student"))
        {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String mPref = sharedPref.getString("home_list", "0");
            switch (mPref) {
                case "0":
                    AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
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
                case "1":
                    sendtoFav();
                    break;
                case "2":
                    sendtoTickets();
                    break;
            }
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
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
        }

    }
}
