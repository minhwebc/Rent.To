package to.rent.rentto.Login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import to.rent.rentto.Models.User;
import to.rent.rentto.Models.UserAccountSettings;
import to.rent.rentto.R;
import to.rent.rentto.Utils.DeviceID;
import to.rent.rentto.Utils.FirebaseMethods;
import to.rent.rentto.Utils.StringManipulation;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private Context mContext;
    private String email, username, password;
    private EditText mEmail, mPassword, mUsername;
    private TextView loadingPleaseWait;
    private Button btnRegister;
    private ProgressBar mProgressBar;
    private TextView loginLink;

    //firebase
    private FirebaseAuth mAuth;

    private String append = "";
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseMethods firebaseMethods;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private DeviceID deviceIDHelper;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mContext = RegisterActivity.this;
        firebaseMethods = new FirebaseMethods(mContext);
        deviceIDHelper = new DeviceID(mContext);
        Log.d(TAG, "onCreate: started.");

        initWidgets();
        setupFirebaseAuth();
        init();
    }

    private void init(){
        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, LoginActivity.class);
                startActivity(intent);
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = mEmail.getText().toString();
                username = mUsername.getText().toString();
                password = mPassword.getText().toString();
                if(checkInputs(email, username, password)){
                    mProgressBar.setVisibility(View.VISIBLE);
                    loadingPleaseWait.setVisibility(View.VISIBLE);
                    Intent intent = new Intent(mContext, PolicyAgreementActivity.class);
                    startActivityForResult(intent, 5);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 5) { // Showed them the policy agreement
            if(resultCode == RESULT_OK) { // They agreed
                Log.d(TAG, "Got activity result, going to register");
                initWidgets();
                checkIfUsernameExists(username);
            } else if(resultCode == RESULT_CANCELED) {
                Log.d(TAG, "Got activity result, result canceled");
                initWidgets();
            }
        }
    }

    private void register() {
        firebaseMethods.registerNewEmail(email, password, username);
    }

    private boolean checkInputs(String email, String username, String password){
        Log.d(TAG, "checkInputs: checking inputs for null values.");
        if(email.equals("") || username.equals("") || password.equals("")){
            Toast.makeText(mContext, "All fields must be filled out.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    /**
     * Initialize the activity widgets
     */
    private void initWidgets(){
        Log.d(TAG, "initWidgets: Initializing Widgets.");
        mEmail = (EditText) findViewById(R.id.input_email);
        mUsername = (EditText) findViewById(R.id.input_username);
        btnRegister = (Button) findViewById(R.id.btn_register);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        loadingPleaseWait = (TextView) findViewById(R.id.loadingPleaseWait);
        mPassword = (EditText) findViewById(R.id.input_password);
        loginLink = (TextView) findViewById(R.id.link_login);
        mContext = RegisterActivity.this;
        mProgressBar.setVisibility(View.GONE);
        loadingPleaseWait.setVisibility(View.GONE);

    }

    private boolean isStringNull(String string){
        Log.d(TAG, "isStringNull: checking string if null.");

        if(string.equals("")){
            return true;
        }
        else{
            return false;
        }
    }

     /*
    ------------------------------------ Firebase ---------------------------------------------
     */

    /**
     * Check is @param username already exists in the database
     * @param username
     */
    private void checkIfUsernameExists(final String username) {
        Log.d(TAG, "checkIfUsernameExists: Checking if  " + username + " already exists.");
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    if (singleSnapshot.exists()){
                        Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH: " + singleSnapshot.getValue(User.class).getUsername());
                        append = myRef.push().getKey().substring(3,10);
                        Log.d(TAG, "onDataChange: username already exists. Appending random string to name: " + append);
                    }
                }

                String mUsername = "";
                mUsername = username + append;
                //add new user to the database
                final String finalMUsername = mUsername;
                reference.child("seen_device_ids").child(deviceIDHelper.getDeviceID()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean deviceExisted = true;
                        Log.d(TAG, "seen device: FOUND A device: ");

                        if(dataSnapshot.exists()){
                            deviceExisted = dataSnapshot.getValue(boolean.class);
                        } else {
                            deviceExisted = false;
                        }
                        if (deviceExisted) {
                            Toast.makeText(mContext, "Device has been seen before, you can't register more account on this device", Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            mAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                                            // If sign in fails, display a message to the user. If sign in succeeds
                                            // the auth state listener will be notified and logic to handle the
                                            // signed in user can be handled in the listener.
                                            if (!task.isSuccessful()) {
                                                Toast.makeText(mContext, "Error registering users", Toast.LENGTH_SHORT).show();
                                            }
                                            else if(task.isSuccessful()){
                                                //send verificaton email
                                                final String userID = mAuth.getCurrentUser().getUid();
                                                mAuth.getCurrentUser().sendEmailVerification();
                                                Log.d(TAG, "onComplete: Authstate changed: " + userID);
                                                reference.child("seen_phone_numbers").child(deviceIDHelper.getPhoneNumber()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        Boolean phoneExisted = true;
                                                        if(dataSnapshot.exists()){
                                                            phoneExisted = dataSnapshot.getValue(Boolean.class);
                                                        } else {
                                                            phoneExisted = false;
                                                        }
                                                        if (phoneExisted) {
                                                            Toast.makeText(mContext, "Phone number have been registered before, you can register more account on this device", Toast.LENGTH_SHORT).show();
                                                            return;
                                                        }else {
                                                            reference.child("seen_device_ids").child(deviceIDHelper.getDeviceID()).setValue(true, new DatabaseReference.CompletionListener() {
                                                                @Override
                                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                    reference.child("seen_phone_numbers").child(deviceIDHelper.getPhoneNumber()).setValue(true, new DatabaseReference.CompletionListener() {
                                                                        @Override
                                                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                            User user = new User( userID,  deviceIDHelper.getPhoneNumber(),  email,  StringManipulation.condenseUsername(finalMUsername), 0.0, 0, deviceIDHelper.getDeviceID());
                                                                            myRef.child(mContext.getString(R.string.dbname_users))
                                                                                    .child(userID)
                                                                                    .setValue(user);

                                                                            UserAccountSettings settings = new UserAccountSettings(
                                                                                    "No Description",
                                                                                    finalMUsername,
                                                                                    0,
                                                                                    "",
                                                                                    StringManipulation.condenseUsername(finalMUsername),
                                                                                    "No Website"
                                                                            );

                                                                            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                                                                                    .child(userID)
                                                                                    .setValue(settings);
                                                                            reference.child("users").child(mAuth.getCurrentUser().getUid()).child("messages_this_user_can_see").push().setValue("welcomeMessage", new DatabaseReference.CompletionListener() {
                                                                                @Override
                                                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                                    Toast.makeText(mContext, "Signup successful. Sending verification email.", Toast.LENGTH_SHORT).show();
                                                                                    mAuth.signOut();
                                                                                }
                                                                            });
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                            }

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //checkIfUsernameExists(username);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    finish();

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
