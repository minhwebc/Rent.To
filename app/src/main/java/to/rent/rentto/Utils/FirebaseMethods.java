package to.rent.rentto.Utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import to.rent.rentto.Models.User;
import to.rent.rentto.Models.UserAccountSettings;
import to.rent.rentto.Models.UserSettings;
import to.rent.rentto.R;

/**
 * Created by Quan Nguyen on 2/14/2018.
 */

public class FirebaseMethods {
    private static final String TAG = "FirebaseMethods";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageReference;
    private String userID;
    private Context mContext;
    private DeviceID deviceIDHelper;

    public FirebaseMethods(Context context) {
        deviceIDHelper = new DeviceID(context);
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mContext = context;

        if(mAuth.getCurrentUser() != null){
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    /**
     * Update 'user_account_settings' node for the current user
     * @param displayName
     * @param website
     * @param description
     * @param phoneNumber
     */
    public void updateUserAccountSettings(String displayName, String website, String description, long phoneNumber){

        Log.d(TAG, "updateUserAccountSettings: updating user account settings.");

        if(displayName != null){
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_display_name))
                    .setValue(displayName);
        }


        if(website != null) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_website))
                    .setValue(website);
        }

        if(description != null) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_description))
                    .setValue(description);
        }

        if(phoneNumber != 0) {
            myRef.child(mContext.getString(R.string.dbname_users))
                    .child(userID)
                    .child(mContext.getString(R.string.field_phone_number))
                    .setValue(Long.toString(phoneNumber));
        }
    }

    /**
     * Updates the phone number in firebase for the current user
     * @param phoneNumber
     */
    public void updatePhoneNumber(String phoneNumber) {
        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_phone_number))
                .setValue(phoneNumber);
    }

        /**
         * update username in the 'users' node and 'user_account_settings' node
         * @param username
         */
    public void updateUsername(String username) {
        Log.d(TAG, "updateUsername: updating username to: " + username);
        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);
    }

    /**
     * update the email in the 'user's' node
     * @param email
     */
    public void updateEmail(String email){
        Log.d(TAG, "updateEmail: upadting email to: " + email);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_email))
                .setValue(email);

    }

    public void sendVerificationEmail(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null){
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                            }else{
                                Toast.makeText(mContext, "Could not send verification email.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    /**
     * Register a new email and password to Firebase Authentication
     * @param email
     * @param password
     * @param username
     */
    public void registerNewEmail(final String email, String password, final String username){
        if(deviceIDHelper.getPhoneNumber().equals("0") || deviceIDHelper.getPhoneNumber().length() < 2) {
            Toast.makeText(mContext,  "Could not sign up. Rent.to could not find your phone number.", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(mContext, "Error registering user", Toast.LENGTH_SHORT).show();
                            } else if (task.isSuccessful()) {
                                //send verificaton email
                                sendVerificationEmail();

                                userID = mAuth.getCurrentUser().getUid();
                                Toast.makeText(mContext, "Success",
                                        Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "onComplete: Authstate changed: " + userID);
                            }
                        }
                    });
        }
    }

    /**
     * Add information to the users nodes
     * Add information to the user_account_settings node
     * @param email
     * @param username
     * @param description
     * @param website
     * @param profile_photo
     */
    public void addNewUser(String userID, String email, String username, String description, String website, String profile_photo){


    }

    /**
     * Retrieves the account settings for the user currently logged in
     * Database: user_account_settings
     * @return
     */
    public UserSettings getUserAccountSettings(DataSnapshot dataSnapshot) {
        String authorUID = mAuth.getCurrentUser().getUid();
        return getUserAccountSettings(dataSnapshot, authorUID);
    }

    /**
     * Retrieves the account settings for the user with the given UID
     * Database: user_account_settings
     * @return
     */
    public UserSettings getUserAccountSettings(DataSnapshot dataSnapshot, String authorUID) {
        Log.d(TAG, "getUserAccountSettings: retrieving user account settings from firebase");
        Log.d(TAG, "currentUserID: " + mAuth.getCurrentUser().getUid());
        UserAccountSettings settings  = new UserAccountSettings();
        User user = new User();
        for(DataSnapshot ds: dataSnapshot.getChildren()){
            if(ds.getKey().equals(mContext.getString(R.string.dbname_user_account_settings))) {
                UserAccountSettings hello = ds.child(authorUID).getValue(UserAccountSettings.class);
                Log.d(TAG, "account setting is : " + ds.child(authorUID).child("username").getValue());
                try {
                    settings.setUsername(
                            ds.child(authorUID)
                                    .getValue(UserAccountSettings.class)
                                    .getUsername()
                    );
                    settings.setDisplay_name(
                            ds.child(authorUID)
                                .getValue(UserAccountSettings.class)
                                .getDisplay_name()
                    );
                    settings.setProfile_photo(
                            ds.child(authorUID)
                                    .getValue(UserAccountSettings.class)
                                    .getProfile_photo()
                    );
                    settings.setPosts(
                            ds.child(authorUID)
                                    .getValue(UserAccountSettings.class)
                                    .getPosts()
                    );
                    settings.setWebsite(
                            ds.child(authorUID)
                                .getValue(UserAccountSettings.class)
                                .getWebsite()
                    );
                    settings.setDescription(
                            ds.child(authorUID)
                                .getValue(UserAccountSettings.class)
                                .getDescription()
                    );

                    //user account settings
                    Log.d(TAG, "getUserAccountSettings: retrieved user_account_settings information: " + settings.toString());
                } catch (NullPointerException e) {
                    Log.e(TAG, "getUserAccountSettings: NullPointerException: " + e.getMessage());
                }
            }
                // users node
                if(ds.getKey().equals(mContext.getString(R.string.dbname_users))) {
                    Log.d(TAG, "getUserAccountSettings: datasnapshot: " + ds);

                    user.setUsername(
                            (String) ds.child(authorUID).child("username").getValue()
                    );
                    user.setEmail(
                            ds.child(authorUID)
                                    .getValue(User.class)
                                    .getEmail()
                    );
                    user.setPhone_number(
                            ds.child(authorUID)
                                    .getValue(User.class)
                                    .getPhone_number()
                    );
                    user.setUser_id(
                            ds.child(authorUID)
                                    .getValue(User.class)
                                    .getUser_id()
                    );
                    user.setRating(
                            ds.child(authorUID)
                                .getValue(User.class)
                                .getRating()
                    );
                    user.setTotalRating(
                            ds.child(authorUID)
                                .getValue(User.class)
                                .getTotalRating()
                    );
                    Log.d(TAG, "getUserAccountSettings: retrieved users information: " + user.toString());
                }
        }
        return new UserSettings(user, settings);
    }
}
