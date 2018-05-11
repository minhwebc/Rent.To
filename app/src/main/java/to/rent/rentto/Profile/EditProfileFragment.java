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
import to.rent.rentto.Utils.DeviceID;
import to.rent.rentto.Utils.FirebaseMethods;

import static android.app.Activity.RESULT_OK;

/**
 * Created by allencho on 2/15/18.
 */

public class EditProfileFragment extends Fragment implements
    ConfirmPasswordDialog.OnConfirmPasswordListener{
    private static final String TAG = "EditProfileFragment";
    private static final int CHANGE_PROFILE_PIC = 1;

    @Override
    public void onConfirmPassword(String password) {
        Log.d(TAG, "onConfirmPassword: got the password: " + password);

        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider
                .getCredential(mAuth.getCurrentUser().getEmail(), password);

        ///////////////////// Prompt the user to re-provide their sign-in credentials
        mAuth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "User re-authenticated.");

                            ///////////////////////check to see if the email is not already present in the database
                            mAuth.fetchProvidersForEmail(mEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                                    if(task.isSuccessful()){
                                        try{
                                            if(task.getResult().getProviders().size() == 1){
                                                Log.d(TAG, "onComplete: that email is already in use.");
                                                Toast.makeText(getActivity(), "That email is already in use", Toast.LENGTH_SHORT).show();
                                            }
                                            else{
                                                Log.d(TAG, "onComplete: That email is available.");

                                                //////////////////////the email is available so update it
                                                mAuth.getCurrentUser().updateEmail(mEmail.getText().toString())
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d(TAG, "User email address updated.");
                                                                    Toast.makeText(getActivity(), "Email updated", Toast.LENGTH_SHORT).show();
                                                                    mFirebaseMethods.updateEmail(mEmail.getText().toString());
                                                                }
                                                            }
                                                        });
                                            }
                                        }catch (NullPointerException e){
                                            Log.e(TAG, "onComplete: NullPointerException: "  +e.getMessage() );
                                        }
                                    }
                                }
                            });





                        }else{
                            Log.d(TAG, "onComplete: re-authentication failed.");
                        }

                    }
                });
    }

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private String userID;

    //EditProfile Fragment widgets
    private EditText mDisplayName, mUsername, mWebsite, mDescription, mEmail, mPhoneNumber;
    private TextView mChangeProfilePhoto;
    private CircleImageView mProfilePhoto;


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
        mEmail = (EditText) view.findViewById(R.id.email);
        mPhoneNumber = (EditText) view.findViewById(R.id.phoneNumber);
        mChangeProfilePhoto = (TextView) view.findViewById(R.id.changeProfilePhoto);
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
        final String email = mEmail.getText().toString();

        Long phoneNumber = new Long(0);
        try {
            phoneNumber = Long.parseLong(mPhoneNumber.getText().toString());
        } catch(Exception e) {
            Toast.makeText(getActivity(), "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        final Long checkPhone = phoneNumber;

        Query query = myRef.child("users");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean exists = false;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    if(user.getPhone_number().equals(Long.toString(checkPhone))){
                        exists = true;
                        break;
                    }
                }
                if (exists) {
                    Toast.makeText(getActivity(), "Phone number already exists", Toast.LENGTH_SHORT).show();
                } else {
                    String newPhone = Long.toString(checkPhone);
                    System.out.println("My new phone number is " + newPhone);
                    myRef.child("users").child(mAuth.getCurrentUser().getUid()).child("phone_number").setValue(newPhone);
                    mFirebaseMethods.updateUserAccountSettings(null, null, null, checkPhone);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Log.d(TAG, "saveProfileSettings: displayName: "+ displayName);
        Log.d(TAG, "saveProfileSettings: website: " + website);
        Log.d(TAG, "saveProfileSettings: description " + description);
        Log.d(TAG, "saveProfileSettings: phoneNumber: " + phoneNumber);

        //case1: if the user made a change to their username
        if(!mUserSettings.getUser().getUsername().equals(username)){

            checkIfUsernameExists(username);
        }
        //case2: if the user made a change to their email
        if(!mUserSettings.getUser().getEmail().equals(email)){

            // step1) Reauthenticate
            //          -Confirm the password and email
            ConfirmPasswordDialog dialog = new ConfirmPasswordDialog();
            dialog.show(getFragmentManager(), getString(R.string.confirm_password_dialog));
            dialog.setTargetFragment(EditProfileFragment.this, 1);


            // step2) check if the email already is registered
            //          -'fetchProvidersForEmail(String email)'
            // step3) change the email
            //          -submit the new email to the database and authentication
        }

        /**
         * change the rest of the settings that do not require uniqueness
         */
        String firebaseDisplayName = mUserSettings.getSettings().getDisplay_name();
        String firebaseWebsite = mUserSettings.getSettings().getWebsite();
        String firebaseDescription = mUserSettings.getSettings().getDescription();
        String firebasePhoneNumber = mUserSettings.getUser().getPhone_number();

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
        if(firebasePhoneNumber == null || !(mUserSettings.getUser().getPhone_number().equals(phoneNumber))) {

                //.getUser().getPhone_number()().equals(phoneNumber)){
            Log.d(TAG, "saveProfileSettings: phoneNumber: " + phoneNumber);
            //update phoneNumber
            mFirebaseMethods.updateUserAccountSettings(null, null, null, phoneNumber);
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
        mEmail.setText(userSettings.getUser().getEmail());
        mPhoneNumber.setText(String.valueOf(userSettings.getUser().getPhone_number()));
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
