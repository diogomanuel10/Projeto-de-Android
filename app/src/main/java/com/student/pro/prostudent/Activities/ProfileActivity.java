package com.student.pro.prostudent.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.student.pro.prostudent.R;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    //Toolbar & Nav
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolbar;
    //Variables
    private Uri filePath = null, imageURL = null;
    private String UserID, currentStatus;
    private static int RESULT_LOAD_IMAGE = 1;
    private static final String TAG = "ProfileLog";
    //Firebase
    private StorageReference mStorageRef;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    //Elements
    private CircleImageView setupImage;
    private TextView nameText, emailText, usernameText, surnameText, topusername;
    private EditText nameEdit, emailEdit, usernameEdit, surnameEdit, passEdit, confirmEdit;
    private ViewSwitcher email_s, username_s, name_s, surname_s;
    private Button emailB, userB, nameB, surnameB, save_settings;
    private ProgressBar profile_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        //Toolbar & Navbar
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
        toolbarTitle.setText(R.string.title_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            currentStatus = intent.getStringExtra("Status").toString();
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



        /*
        ----Elements Initialization----
         */
        nameText = findViewById(R.id.profile_name);
        emailText = findViewById(R.id.profile_email);
        usernameText = findViewById(R.id.profile_username);
        surnameText = findViewById(R.id.profile_surname);
        topusername = findViewById(R.id.profile_uname);
        nameEdit = findViewById(R.id.profile_name2);
        emailEdit = findViewById(R.id.profile_email2);
        usernameEdit = findViewById(R.id.profile_username2);
        surnameEdit = findViewById(R.id.profile_surname2);
        passEdit = findViewById(R.id.profile_password);
        confirmEdit = findViewById(R.id.profile_confirm_pass);
        email_s = findViewById(R.id.switch_email);
        username_s = findViewById(R.id.switch_username);
        name_s = findViewById(R.id.switch_name);
        surname_s = findViewById(R.id.switch_surname);
        emailB = findViewById(R.id.but_email);
        userB = findViewById(R.id.but_user);
        nameB = findViewById(R.id.but_name);
        surnameB = findViewById(R.id.but_surname);
        save_settings = findViewById(R.id.profile_save);
        setupImage = findViewById(R.id.setup_image_header);
        profile_bar = findViewById(R.id.profile_bar);

        //Instances
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        //User
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        UserID = user.getUid().toString();


        getUserData();

        /*
        ----- Click Listeners ----
         */
        //Switch between TextView & EditText

        userB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (username_s.getCurrentView() != usernameText) {
                    username_s.showPrevious();
                    userB.setBackgroundResource(R.drawable.ic_action_edit);

                } else {
                    username_s.showNext();
                    userB.setBackgroundResource(R.drawable.ic_action_edit_on);
                }
            }
        });
        emailB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (email_s.getCurrentView() != emailText) {
                    email_s.showPrevious();
                    emailB.setBackgroundResource(R.drawable.ic_action_edit);

                } else {
                    email_s.showNext();
                    emailB.setBackgroundResource(R.drawable.ic_action_edit_on);
                }
            }
        });
        nameB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name_s.getCurrentView() != nameText) {
                    name_s.showPrevious();
                    nameB.setBackgroundResource(R.drawable.ic_action_edit);

                } else {
                    name_s.showNext();
                    nameB.setBackgroundResource(R.drawable.ic_action_edit_on);
                }
            }
        });
        surnameB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (surname_s.getCurrentView() != surnameText) {
                    surname_s.showPrevious();
                    surnameB.setBackgroundResource(R.drawable.ic_action_edit);

                } else {
                    surname_s.showNext();
                    surnameB.setBackgroundResource(R.drawable.ic_action_edit_on);
                }
            }
        });

        //Save Changes
        save_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Show progress bar
                profile_bar.setVisibility(View.VISIBLE);
                //Validate Inputs
                if (!emailEdit.getText().toString().isEmpty() || !passEdit.getText().toString().isEmpty()) {
                    //Create Alert Dialog to ask for current password
                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                    builder.setTitle(R.string.prompt_old_pass);
                    final EditText input = new EditText(ProfileActivity.this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    input.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    builder.setView(input);
                    builder.setPositiveButton(R.string.action_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (emailEdit.getText() != null && passEdit.getText() != null) {
                                String m_Text = input.getText().toString();
                                setUserData(m_Text);
                            }
                        }
                    });
                    builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            profile_bar.setVisibility(View.GONE);

                        }
                    });
                    builder.show();
                } else {
                    setUserData("");
                }
            }
        });

        /*
        Storage Permissions
        Image Selection
        Image Upload
         */
        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Checks if build version is greater than Marshmallow
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //Checks for reading permission
                    if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        //Asks for permission
                        ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        chooseImage();
                    }

                }
                //If build is lower the permission is asked in the play store
                else {
                    chooseImage();
                }
            }
        });
    }


    private void setUserData(String oldpass) {
           /*
           To change the user password or email we need to ask for the current password
           After we manage to successfuly change the information at FirebaseAuth we change the information in the Database
         */
        if (!passEdit.getText().toString().isEmpty() && !confirmEdit.getText().toString().isEmpty() && passEdit.length() >= 6 && passEdit.getText().toString().equals(confirmEdit.getText().toString())) {
            final String email = user.getEmail().toString();
            //Progresso de alteração de palavra passe
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(R.string.authenticating);
            progressDialog.show();

            AuthCredential credential = EmailAuthProvider.getCredential(email, oldpass);
            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        //Change Password
                        user.updatePassword(passEdit.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                //Failed
                                if (!task.isSuccessful()) {
                                    progressDialog.dismiss();
                                    Toast.makeText(ProfileActivity.this, R.string.password_failed, Toast.LENGTH_SHORT).show();
                                }
                                //Success
                                else {
                                    progressDialog.dismiss();
                                    Toast.makeText(ProfileActivity.this, R.string.password_success, Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
                    }
                    //Auth Failed
                    else {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, R.string.password_failed_2, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        if (!emailEdit.getText().toString().isEmpty()) {
            final String email = user.getEmail().toString();
            //Progresso de alteração de palavra passe
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(R.string.authenticating);
            progressDialog.show();

            AuthCredential credential = EmailAuthProvider.getCredential(email, oldpass);
            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        //Alteração da palavra passe
                        user.updateEmail(emailEdit.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                //Failed
                                if (!task.isSuccessful()) {
                                    profile_bar.setVisibility(View.INVISIBLE);
                                    progressDialog.dismiss();
                                    Toast.makeText(ProfileActivity.this, R.string.email_failed, Toast.LENGTH_SHORT).show();
                                }
                                //Success
                                else {
                                    progressDialog.dismiss();
                                    mDatabase.child(UserID).child("email").setValue(emailEdit.getText().toString());

                                    Toast.makeText(ProfileActivity.this, R.string.email_success, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    //Auth Failed
                    else {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, R.string.email_failed_2, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        if (!nameEdit.getText().toString().isEmpty()) {
            mDatabase.child(UserID).child("name").setValue(nameEdit.getText().toString());
        }
        if (!usernameEdit.getText().toString().isEmpty()) {
            mDatabase.child(UserID).child("username").setValue(usernameEdit.getText().toString());
        }
        if (!surnameEdit.getText().toString().isEmpty()) {
            mDatabase.child(UserID).child("surname").setValue(surnameEdit.getText().toString());
        } else {
            profile_bar.setVisibility(View.INVISIBLE);
            sendtoHome();
        }
        profile_bar.setVisibility(View.INVISIBLE);
        sendtoHome();

    }

    private void getUserData() {
        profile_bar.setVisibility(View.VISIBLE);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String username, email, name, surname, url;
                username = dataSnapshot.child(UserID).child("username").getValue().toString();
                email = dataSnapshot.child(UserID).child("email").getValue().toString();
                name = dataSnapshot.child(UserID).child("name").getValue().toString();
                surname = dataSnapshot.child(UserID).child("surname").getValue().toString();
                url = dataSnapshot.child(UserID).child("url").getValue().toString();
                updateUI(username, email, name, surname, url);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                profile_bar.setVisibility(View.INVISIBLE);
            }
        });
    }

    //Atualização dos dados do utilizador nos campos a editar
    private void updateUI(String username, String email, String name, String surname, String url) {
        this.nameText.setHint(name);
        this.usernameText.setHint(username);
        this.emailText.setHint(email);
        this.surnameText.setHint(surname);
        this.topusername.setText(username);
        Picasso.get()
                .load(url)
                .placeholder(R.drawable.default_icon)
                .error(R.drawable.default_icon)
                .into(setupImage);

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
        profile_bar.setVisibility(View.INVISIBLE);
        Toast.makeText(ProfileActivity.this, R.string.load_profile, Toast.LENGTH_SHORT).show();
    }

    //Criação da atividade para escolher a imagem
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        String mystring = this.getResources().getString(R.string.select_img).toString();
        startActivityForResult(Intent.createChooser(intent, mystring), RESULT_LOAD_IMAGE);
    }

    //Gestão do resultado da atividade
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                //Atualiza a foto de perfil na app
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                setupImage.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
            uploadImage();
        }
    }

    //Upload de Imagem para a FireStorage
    private void uploadImage() {
        //Verifica se o ficheiro foi escolhido
        if (filePath != null) {
            //Inicialização de caixa de progresso de upload
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(R.string.upload_progress);
            progressDialog.show();
            /*
            Incio do upload
            Imagem é guardada na pasta images com o nome (UserID.jpg)
            Após a tarefa ser concluida (com ou sem sucessso) a caixa de progresso é dispensada
             */
            StorageReference ref = mStorageRef.child("images/" + UserID + ".jpg");
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageURL = taskSnapshot.getDownloadUrl();
                            Toast.makeText(ProfileActivity.this, R.string.upload_success, Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            mDatabase.child(UserID).child("url").setValue(imageURL.toString());

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this, R.string.upload_failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage(R.string.upload_sent + (int) progress + "%");
                        }
                    });
        }
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
        Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
        intent.putExtra("Status", currentStatus);
        startActivity(intent);
        finish();
    }

    private void sendtoFav() {
        Intent intent = new Intent(ProfileActivity.this, FavoritesActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendtoNotes() {
        //Send to my notes
    }

    private void sendtoTickets() {
        Intent intent = new Intent(ProfileActivity.this, MyTicketsActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendtoProfile() {
        Intent intent = new Intent(ProfileActivity.this, ProfileActivity.class);
        intent.putExtra("Status", currentStatus);
        startActivity(intent);
        finish();
    }

    private void sendtoSettings() {
        Intent intent;
        if (currentStatus.equals("student")) {
            intent = new Intent(ProfileActivity.this, SettingsActivity.class);

        } else {
            intent = new Intent(ProfileActivity.this, SettingsActivityProfessor.class);

        }
        startActivity(intent);
    }

    private void sendtoLogin() {
        mAuth.signOut();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public void onBackPressed() {
        // Checks preferences to determine main screen and acts accordingly
        if (currentStatus.equals("student")) {
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
