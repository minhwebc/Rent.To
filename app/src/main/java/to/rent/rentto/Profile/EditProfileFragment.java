package to.rent.rentto.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;
import to.rent.rentto.Dialogs.ConfirmPasswordDialog;
import to.rent.rentto.Models.User;
import to.rent.rentto.Models.UserAccountSettings;
import to.rent.rentto.Models.UserSettings;
import to.rent.rentto.R;
import to.rent.rentto.Utils.FirebaseMethods;

import static android.app.Activity.RESULT_OK;

/**
 * Created by allencho on 2/15/18.
 */

public class EditProfileFragment extends Fragment {
    private static final String TAG = "EditProfileFragment";
    private static final int CHANGE_PROFILE_PIC = 1;


    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private String userID;

    //EditProfile Fragment widgets
    private EditText mDisplayName, mUsername, mWebsite, mDescription;
    private TextView mChangeProfilePhoto;
    private CircleImageView mProfilePhoto;

    //Button to change other settings (phone and email)
    private Button changeOtherSettingsButton;

    //variables
    private UserSettings mUserSettings;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v(TAG, "here in the profile");
        View view = inflater.inflate(R.layout.fragment_editprofile, container, false);
        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_photo);
        mDisplayName = (EditText) view.findViewById(R.id.display_name);
        mUsername = (EditText) view.findViewById(R.id.username);
        mWebsite = (EditText) view.findViewById(R.id.website);
        mDescription = (EditText) view.findViewById(R.id.description);
        mChangeProfilePhoto = (TextView) view.findViewById(R.id.changeProfilePhoto);
        changeOtherSettingsButton = (Button) view.findViewById(R.id.buttonOtherSettings);
        mFirebaseMethods = new FirebaseMethods(getActivity());

        //setProfileImage();
        setupFirebaseAuth();

        setupProfilePhotoClick();


        //back arrow for navigating back to "ProfileActivity"
        ImageView backArrow = (ImageView) view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back to ProfileActivity");
                getActivity().finish();
            }
        });
        ImageView checkmark = (ImageView) view.findViewById((R.id.saveChanges));
        checkmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to save changes.");
                saveProfileSettings();
            }
        });

        changeOtherSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ChangeOtherSettings.class);
                startActivity(intent);
            }
        });

        return view;
    }

    private void setupProfilePhotoClick() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: switching to ChangeProfilePictureActivity.");
                Intent intent = new Intent(getActivity(), ChangeProfilePictureActivity.class);
                startActivityForResult(intent, CHANGE_PROFILE_PIC);
            }
        };
        mProfilePhoto.setOnClickListener(listener);
        mChangeProfilePhoto.setOnClickListener(listener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            getActivity().recreate();
        }
    }

    //    private void setProfileImage() {
//        Log.d(TAG, "setProfileImage: setting profile image.");
//        String imgURL = "https://josephratliff.name/wp-content/uploads/2017/11/android-central.jpg";
//        Glide.with(getActivity())
//                .load(imgURL)
//                .into(mProfilePhoto);
//        //UniversalImageLoader.setImage(imgURL, mProfilePhoto, null, "https://");
//    }

    /**
     * Retrieves the data contained in the widgets and submits it to the database
     * Before doing so it checks to make sure the username chosen in unique
     */
    private void saveProfileSettings(){
        final String displayName = mDisplayName.getText().toString();
        final String username = mUsername.getText().toString();
        final String website = mWebsite.getText().toString();
        final String description = mDescription.getText().toString();
        Log.d(TAG, "saveProfileSettings: displayName: "+ displayName);
        Log.d(TAG, "saveProfileSettings: website: " + website);
        Log.d(TAG, "saveProfileSettings: description " + description);

        //case1: if the user made a change to their username
        if(!mUserSettings.getUser().getUsername().equals(username)){
            checkIfUsernameExists(username);
        }

        /**
         * change the rest of the settings that do not require uniqueness
         */
        String firebaseDisplayName = mUserSettings.getSettings().getDisplay_name();
        String firebaseWebsite = mUserSettings.getSettings().getWebsite();
        String firebaseDescription = mUserSettings.getSettings().getDescription();
        if(firebaseDisplayName == null || !mUserSettings.getSettings().getDisplay_name().equals(displayName)){
            Log.d(TAG, "saveProfileSettings: displayName: "+ displayName);
            //update displayname
            mFirebaseMethods.updateUserAccountSettings(displayName, null, null, 0);
        }
        if(firebaseWebsite == null || !mUserSettings.getSettings().getWebsite().equals(website)){
            Log.d(TAG, "saveProfileSettings: website: " + website);
            //update website
            mFirebaseMethods.updateUserAccountSettings(null, website, null, 0);
        }
        if(firebaseDescription == null || !mUserSettings.getSettings().getDescription().equals(description)){
            Log.d(TAG, "saveProfileSettings: description " + description);
            //update description
            mFirebaseMethods.updateUserAccountSettings(null, null, description, 0);
        }
    }

    /**
     * Check is @param username already exists in the database
     * @param username
     */
    private void checkIfUsernameExists(final String username) {
        Log.d(TAG, "checkIfUsernameExists: Checking if " + username + "already exists.");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    //add the username
                    mFirebaseMethods.updateUsername(username);
                    Toast.makeText(getActivity(), "saved username.", Toast.LENGTH_LONG).show();
                }
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()) {
                    if(singleSnapshot.exists()) {
                        Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH:  " + singleSnapshot.getValue(User.class).getUsername());
                        Toast.makeText(getActivity(), "That username aleady exists.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setProfileWidgets(UserSettings userSettings) {
        //Log.d(TAG, "setProfileWidgets: settings widgets with data retrieving from firebase database: " + userSettings.toString());
        Log.d(TAG, "setProfileWidgets: settings widgets with data retrieving from firebase database: " + userSettings.getSettings().getDisplay_name());

        mUserSettings = userSettings;
        //User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();

        //UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");
        Glide.with(getActivity())
                .load(settings.getProfile_photo())
                .into(mProfilePhoto);
        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
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
                    setProfileWidgets(mFirebaseMethods.getUserAccountSettings(dataSnapshot));
                } catch (Exception e) {

                }

                //retrieve images for the user in question

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
