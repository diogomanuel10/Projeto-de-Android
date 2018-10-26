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
import com.student.pro.prostudent.Adapters.AdapterStudent;
import com.student.pro.prostudent.Adapters.SectionedAdapter;
import com.student.pro.prostudent.Comparators.CustomCompareDiscipline;
import com.student.pro.prostudent.Objects.Disciplines;
import com.student.pro.prostudent.Objects.Favorite;
import com.student.pro.prostudent.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolbar;
    private DatabaseReference mDatabase, mDB_Student;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String UserID, TAG = "Favlog";
    private RecyclerView mView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

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
        toolbarTitle.setText(R.string.title_favorites);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        UserID = user.getUid();
        mDB_Student = FirebaseDatabase.getInstance().getReference("students_courses");


        NavigationView nav = findViewById(R.id.nav_view);
        Menu menu = nav.getMenu();
        menu.findItem(R.id.nav_notes).setVisible(false);

        getUserData();
        getFav();
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

    // My Favorites --------------------------------------------------------------------------------

    private void getFav() {
        DatabaseReference mDB_Fav = FirebaseDatabase.getInstance().getReference("users_fav").child(UserID);

        mDB_Fav.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Favorite> favorites = new ArrayList<>();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Favorite favorite = new Favorite(postSnapshot.child("tag").getValue().toString(), postSnapshot.child("id").getValue().toString());
                    favorites.add(favorite);
                }
                getFavCourseStudent(favorites);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void getFavCourseStudent(final ArrayList<Favorite> favorites) {
        mDB_Student.child(UserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getFavClassesStudent(dataSnapshot.child("course_id").getValue().toString(), favorites);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getFavClassesStudent(String course, final ArrayList<Favorite> favorites) {
        mDatabase = FirebaseDatabase.getInstance().getReference("courses/course/" + course + "/ucs");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int cnt1 = 0, cnt2 = 0, cnt3 = 0;
                ArrayList<Disciplines> ucs = new ArrayList<Disciplines>();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String name, year, tag, id;
                    for (int i = 0; i < favorites.size(); i++) {
                        if (favorites.get(i).getId().equals(postSnapshot.getKey()) && favorites.get(i).getTag().equals(postSnapshot.child("short").getValue().toString())) {
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
                            if (postSnapshot.child("year").getValue().toString().equals("3")) {
                                cnt3++;
                            }
                            Disciplines uc = new Disciplines(name, year, tag, id);
                            ucs.add(uc);
                            break;
                        }
                    }
                }
                initRecycleFavorite(ucs, cnt1, cnt2, cnt3);
                mDatabase.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void initRecycleFavorite(ArrayList<Disciplines> ucs, int cnt1, int cnt2, int cnt3) {
        Collections.sort(ucs, new CustomCompareDiscipline());

        mView = findViewById(R.id.recycler_favorite);
        AdapterStudent adapter = new AdapterStudent(ucs, this);

        List<SectionedAdapter.Section> sections =
                new ArrayList<>();

        /*
        Combinations possible with favorites
        Creates sections as needed
         */

        //111
        if (cnt1 > 0 && cnt2 > 0 && cnt3 > 0) {
            sections.add(new SectionedAdapter.Section(0, "1st Year"));
            sections.add(new SectionedAdapter.Section(cnt1, "2nd Year"));
            sections.add(new SectionedAdapter.Section(cnt2 + cnt1, "3rd Year"));
        }
        //110
        if (cnt1 > 0 && cnt2 > 0 && cnt3 == 0) {
            sections.add(new SectionedAdapter.Section(0, "1st Year"));
            sections.add(new SectionedAdapter.Section(cnt1, "2nd Year"));
        }
        //101
        if (cnt1 > 0 && cnt2 == 0 && cnt3 > 0) {
            sections.add(new SectionedAdapter.Section(0, "1st Year"));
            sections.add(new SectionedAdapter.Section(cnt1, "3rd Year"));
        }
        //100
        if (cnt1 > 0 && cnt2 == 0 && cnt3 == 0) {
            sections.add(new SectionedAdapter.Section(0, "1st Year"));
        }
        //011
        if (cnt1 == 0 && cnt2 > 0 && cnt3 > 0) {
            sections.add(new SectionedAdapter.Section(0, "2nd Year"));
            sections.add(new SectionedAdapter.Section(cnt2, "3rd Year"));
        }

        //010
        if (cnt1 == 0 && cnt2 > 0 && cnt3 == 0) {
            sections.add(new SectionedAdapter.Section(0, "2nd Year"));
        }
        //001
        if (cnt1 == 0 && cnt2 == 0 && cnt3 > 0) {
            sections.add(new SectionedAdapter.Section(0, "3rd Year"));
        }

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
        Intent intent = new Intent(FavoritesActivity.this, HomeActivity.class);
        intent.putExtra("Status", "student");
        startActivity(intent);
        finish();
    }

    private void sendtoFav() {
        getFav();
    }


    private void sendtoTickets() {
        Intent intent = new Intent(FavoritesActivity.this, MyTicketsActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendtoProfile() {
        Intent intent = new Intent(FavoritesActivity.this, ProfileActivity.class);
        intent.putExtra("Status","student");
        startActivity(intent);
        finish();
    }

    private void sendtoSettings() {
        Intent intent = new Intent(FavoritesActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void sendtoLogin() {
        mAuth.signOut();
        Intent intent = new Intent(FavoritesActivity.this, LoginActivity.class);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(FavoritesActivity.this);
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
            case "2":
                sendtoTickets();
                break;
        }
    }
}
