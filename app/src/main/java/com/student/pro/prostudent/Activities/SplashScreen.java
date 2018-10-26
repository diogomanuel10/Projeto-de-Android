package com.student.pro.prostudent.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.student.pro.prostudent.R;

public class SplashScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDB_User;
    private String UserID;
    private String[] currentStatus = new String[1];
    private FirebaseUser user;
    private boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        currentStatus[0] = "";

        if (user != null) {
            UserID = user.getUid();
            mDB_User = FirebaseDatabase.getInstance().getReference("users").child(UserID).child("status");
            getStatus();
            flag = true;
        } else {
            flag = false;
        }

        final AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setDuration(1000);
        alphaAnimation.setRepeatCount(15);
        alphaAnimation.setRepeatMode(Animation.REVERSE);
        findViewById(R.id.text_splash).startAnimation(alphaAnimation);

        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //User token not found
                if (!flag) {
                    Log.d("SPLASHLOG", "NO TOKEN");

                    sendtoLogin();
                }
                //Current status is still unknown
                else if (currentStatus[0].equals("")) {
                    Log.d("SPLASHLOG", "NO STATUS");
                    //Repeats request
                    if (user != null) {
                        UserID = user.getUid();
                        mDB_User = FirebaseDatabase.getInstance().getReference("users").child(UserID).child("status");
                        getStatus();
                        flag = true;
                    } else {
                        flag = false;
                    }
                    //Wait longer for status fetch
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            Log.d("SPLASHLOG", "WAIT LONGER");

                            if (flag) {
                                sendtoHome();
                            } else {
                                sendtoLogin();
                            }
                        }
                    }, 8000);
                }

                //User Token found
                else if (flag) {
                    Log.d("SPLASHLOG", "FOUND TOKEN");

                    sendtoHome();
                }


            }
        }, 3000);
    }

    //Retrieve User status
    private void getStatus() {
        mDB_User.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue().toString().equals("student")) {
                    currentStatus[0] = "student";
                } else {
                    currentStatus[0] = "professor";
                }
                mDB_User.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    private void sendtoLogin() {
        Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendtoHome() {
        Intent intent = null;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String mPref = sharedPref.getString("home_list", "0");

        if (currentStatus[0].equals("student")) {
            switch (mPref) {
                case "0":
                    intent = new Intent(SplashScreen.this, HomeActivity.class);

                    break;
                case "1":
                    intent = new Intent(SplashScreen.this, FavoritesActivity.class);

                    break;
                case "2":
                    intent = new Intent(SplashScreen.this, MyTicketsActivity.class);

                    break;
            }
            intent.putExtra("Status", currentStatus[0].toString());
            startActivity(intent);
            finish();
        } else if (currentStatus[0].equals("professor")) {
            intent = new Intent(SplashScreen.this, HomeActivity.class);
            intent.putExtra("Status", currentStatus[0].toString());
            startActivity(intent);
            finish();

        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}

