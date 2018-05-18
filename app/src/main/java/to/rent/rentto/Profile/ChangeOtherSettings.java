package to.rent.rentto.Profile;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import to.rent.rentto.Models.User;
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
    private String oldEmail;
    private String oldPhone;

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
            oldEmail = email;
            editTextEmail.setText(email);
        }
        String phone = userSettings.getUser().getPhone_number();
        if(phone != null) {
            oldPhone = phone;
            editTextPhone.setText(phone);
        }
    }

    /**
     * Saves profile settings
     * Will show toast and will not save if fields are empty
     * Will not save if phone number already exists in database
     * Will not save if email already exists
     * Will not make changes if both edit texts are unchanged
     */
    private void saveProfileSettings() {
        if(editTextEmail.getText().length() == 0 || editTextPhone.getText().length() == 0) {
            Toast.makeText(this, "Please enter a valid phone number and email", Toast.LENGTH_SHORT).show();
        } else {
            String email = editTextEmail.getText().toString();
            String phone = editTextPhone.getText().toString();
            boolean changed = false;
            if(oldEmail == null || !email.equals(oldEmail)) {
                saveEmail(email);
                changed = true;
            }
            if(oldPhone == null || !phone.equals(oldPhone)) {
                savePhone(phone); // checks and saves phone number if unique
                changed = true;
            }
            if(changed) {
                Toast.makeText(this, "Saved Successfull", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /**
     * Checks and saves email to firebase if it is unique
     * @param email
     */
    private void saveEmail(final String email) {
        Query query = myRef.child("users");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean unique = true;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    if(user.getEmail().equals(email)){
                        unique = false;
                        Log.d(TAG, "we found a matching email");
                    }
                } // there were no matching phone numbers
                if(unique) {
                    saveEmailHelper(email);
                } else {
                    Log.d(TAG, "Cannot change email " + email);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Saves the email to firebase
     * Email should already be checked for uniqueness and validity
     * @param email
     */
    private void saveEmailHelper(String email) {
        Log.d(TAG, "The email can be changed " + email);
        mFirebaseMethods.updateEmail(email);
    }

    /**
     * Checks if the phone number is unique
     * Saves the firenumber in firebase if unique
     * @param phone The phone number to be checked
     */
    private void savePhone(final String phone) {
        Query query = myRef.child("users");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean unique = true;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    if(user.getPhone_number().equals(phone)){
                        unique = false;
                        Log.d(TAG, "we found a matching phone number");
                    }
                } // there were no matching phone numbers
                if(unique) {
                    savePhoneHelper(phone);
                } else {
                    Log.d(TAG, "Cannot change phone number " + phone);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Saves the phonenumber
     * Phone number should already be checked for validity and uniqueness
     * @param phone
     */
    private void savePhoneHelper(String phone) {
        Log.d(TAG, "The phone number can be changed" + phone);
        mFirebaseMethods.updatePhoneNumber(phone);
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
