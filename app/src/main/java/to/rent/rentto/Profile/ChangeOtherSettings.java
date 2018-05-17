package to.rent.rentto.Profile;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import to.rent.rentto.Models.UserSettings;
import to.rent.rentto.R;
import to.rent.rentto.Utils.FirebaseMethods;

public class ChangeOtherSettings extends AppCompatActivity {
    private String TAG = "ChangeOtherSettings";
    private EditText editTextEmail;
    private EditText editTextPhone;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private String userID;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_other_settings);
        init();
    }

    /** Sets on click listener for top toolbar
     *  Sets EditText fields for email and phoneNumber
     *  Sets up Firebase
     */
    private void init() {
        // Set on click listeners for top toolbar
        // back arrow for navigating back to "ProfileActivity"
        ImageView backArrow = (ImageView) findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Log.d(TAG, "onClick: navigating back to ProfileActivity");
            finish();
            }
        });

        // check mark arrow onclick to save profile setting changes
        ImageView checkmark = (ImageView) findViewById((R.id.saveChanges));
        checkmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to save changes.");
                saveProfileSettings();
            }
        });

        // Set editText fields
        editTextEmail = (EditText) findViewById(R.id.email);
        editTextPhone = (EditText) findViewById(R.id.phoneNumber);

        // Set up Firebase
        mFirebaseMethods = new FirebaseMethods(this);
        setupFirebaseAuth(); // sets up firebase and sets values for EditText for phone and email
    }

    /**
     * Sets the editText values for phone number and email to the values in firebase
     * Email and phone number should not be null
     * @param userSettings The account settings for the user. Includes email and phone number
     */
    private void setEditText(UserSettings userSettings) {
        String email = userSettings.getUser().getEmail();
        if(email != null) {
            editTextEmail.setText(email);
        }
        String phone = userSettings.getUser().getPhone_number();
        if(phone != null) {
            editTextPhone.setText(phone);
        }
    }

    /**
     * Saves profile settings
     * Will show toast and will not save if fields are empty
     * Will not save if phone number already exists in database
     * Will not save if username already exists
     */
    private void saveProfileSettings() {

    }

        /*
    ------------------------------------ Firebase ---------------------------------------------
     */

    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        userID = mAuth.getCurrentUser().getUid();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //retrieve user information from the database
                //try here so it doesn't break the code
                try {
                    setEditText(mFirebaseMethods.getUserAccountSettings(dataSnapshot));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
