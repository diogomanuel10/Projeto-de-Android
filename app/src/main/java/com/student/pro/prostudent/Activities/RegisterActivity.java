package com.student.pro.prostudent.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.student.pro.prostudent.Objects.Users;
import com.student.pro.prostudent.R;

public class RegisterActivity extends AppCompatActivity {
    //Elements
    private EditText usernameText, emailText, email_confirmText, passText, pass_confirmText, nameText, surnameText;
    private Button confirmBut;
    private ProgressBar registerprogress;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    //Firebase
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    //Variables
    private String TAG = "RegisterLog", currentStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //User
        mAuth = FirebaseAuth.getInstance();
        //Instances
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        //Elements
        usernameText = findViewById(R.id.reg_username);
        emailText = findViewById(R.id.reg_email);
        email_confirmText = findViewById(R.id.reg_confirm_email);
        passText = findViewById(R.id.reg_pass);
        pass_confirmText = findViewById(R.id.reg_confirm_pass);
        nameText = findViewById(R.id.reg_name);
        surnameText = findViewById(R.id.reg_surname);
        confirmBut = findViewById(R.id.reg_confirm);
        radioGroup = findViewById(R.id.radioGroup);

        confirmBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerprogress = (ProgressBar) findViewById(R.id.reg_prog);

                String username = usernameText.getText().toString();
                String email = emailText.getText().toString();
                String email_confirm = email_confirmText.getText().toString();
                String pass = passText.getText().toString();
                String pass_confirm = pass_confirmText.getText().toString();
                String name = nameText.getText().toString();
                String surname = surnameText.getText().toString();
                String url = "empty";
                radioButton = radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
                currentStatus = radioButton.getText().toString().toLowerCase();
                //image url is set to empty by default, the user must select a picture while editing his profile

                //If all the required fields aren't empty
                if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(email) &&
                        !TextUtils.isEmpty(email_confirm) && !TextUtils.isEmpty(pass) &&
                        !TextUtils.isEmpty(pass_confirm) && !TextUtils.isEmpty(name) &&
                        !TextUtils.isEmpty(surname) && !TextUtils.isEmpty(currentStatus)) {
                    registerprogress.setVisibility(View.VISIBLE);
                    //Show the progress bar
                    //Check for if emails match
                    if (!TextUtils.equals(email, email_confirm)) {
                        //emails don't match

                    }
                    //Check if passwords match
                    if (!TextUtils.equals(pass, pass_confirm)) {
                        //passwords don't match
                    }
                    //User input was accepted
                    else {
                        //Calls the function to create a new account while passing the required parameters
                        Users user = new Users(username, email, name, surname, url, currentStatus);

                        createAccount(user, pass);
                    }
                }
            }
        });
    }


    private void createAccount(final Users userdb, String pass) {
        mAuth.createUserWithEmailAndPassword(userdb.getEmail(), pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            String UserID = mAuth.getCurrentUser().getUid();
                            mDatabase.child(UserID).setValue(userdb);
                            updateUI(user);
                            registerprogress.setVisibility(View.INVISIBLE);

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(RegisterActivity.this, R.string.authentication_failed, Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            sendtoHome();
        }
    }

    private void sendtoHome() {
        //Finishes Class Activity that was left Open
        Intent intent_finish = new Intent("finish_login");
        sendBroadcast(intent_finish);

        Intent intent = null;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String mPref = sharedPref.getString("home_list", "0");
        if (currentStatus.equals("student")) {
            switch (mPref) {
                case "0":
                    intent = new Intent(RegisterActivity.this, HomeActivity.class);
                    break;
                case "1":
                    intent = new Intent(RegisterActivity.this, FavoritesActivity.class);
                    break;
                case "2":
                    intent = new Intent(RegisterActivity.this, MyTicketsActivity.class);
                    break;
            }
            intent.putExtra("Status", currentStatus);
            startActivity(intent);
            finish();
        }
        else
        {
            intent = new Intent(RegisterActivity.this, HomeActivity.class);
            intent.putExtra("Status", currentStatus);
            startActivity(intent);
            finish();
        }

        Toast.makeText(RegisterActivity.this, R.string.authentication_success, Toast.LENGTH_SHORT).show();
        finish();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
