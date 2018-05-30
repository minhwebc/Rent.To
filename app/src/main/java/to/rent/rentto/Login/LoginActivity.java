package to.rent.rentto.Login;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import to.rent.rentto.Home.HomeActivity;
import to.rent.rentto.R;
import to.rent.rentto.Utils.DeviceID;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final Boolean CHECK_IF_VERIFIED = true;
    private final int granted = PackageManager.PERMISSION_GRANTED;
    private final String camera = Manifest.permission.CAMERA;
    private final String location = Manifest.permission.ACCESS_FINE_LOCATION;
    private final String phone = Manifest.permission.READ_PHONE_STATE;

    //firebase
    private FirebaseAuth mAuth;

    private Context mContext;
    private ProgressBar mProgressBar;
    private EditText mEmail, mPassword;
    private TextView mPleaseWait;
    private FloatingActionButton floatingActionButton;
    private String phoneNumber;
    private String deviceID;
    private Button btnLogin;
    private TextView linkSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        String emailLink = "";
        if(intent.getData() != null) {
            emailLink = intent.getData().toString();
        }

        // Confirm the link is a sign-in with email link.
        if (auth.isSignInWithEmailLink(emailLink)) {
            String email; // retrieve this from wherever you stored it
            // The client SDK will parse the code from the link for you.
            auth.signInWithEmailLink("sazeng@uw.edu", emailLink)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Successfully signed in with email link!");
                                AuthResult result = (AuthResult) task.getResult();
                                // You can access the new user via result.getUser()
                                // Additional user info profile *not* available via:
                                // result.getAdditionalUserInfo().getProfile() == null
                                // You can check if the user is new or existing:
                                // result.getAdditionalUserInfo().isNewUser()
                                Log.d(TAG, "user id : " + mAuth.getCurrentUser().getUid());
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Log.e(TAG, "Error signing in with email link: "
                                        + task.getException().getMessage());
                                Toast.makeText(mContext,  "Failed logging in with email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        btnLogin = (Button) findViewById(R.id.btn_login);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mPleaseWait = (TextView) findViewById(R.id.pleaseWait);
        mEmail = (EditText) findViewById(R.id.input_email);
        mPassword = (EditText) findViewById(R.id.input_password);
        linkSignUp = (TextView) findViewById(R.id.link_signup);
        mContext = LoginActivity.this;
        Log.d(TAG, "onCreate: started.");
        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setVisibility(View.GONE);
//        floatingActionButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(LoginActivity.this, VideoActivity.class);
//                startActivity(intent);
//            }
//        });
        mPleaseWait.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        setupFirebaseAuth();
        if(checkSelfPermission(camera) != granted || checkSelfPermission(location) != granted || checkSelfPermission(phone) != granted) {
            Toast.makeText(mContext, "Rent.to needs permission to start", Toast.LENGTH_SHORT).show();
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, "Cannot log in without permissions", Toast.LENGTH_SHORT).show();
                }
            });
            linkSignUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, "Cannot sign up without permissions", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            init();
            DeviceID deviceIDHelp = new DeviceID(mContext);
            phoneNumber = deviceIDHelp.getPhoneNumber();
            Log.d(TAG, "phonenumber is " + phoneNumber);
            deviceID = deviceIDHelp.getDeviceID();
            Log.d(TAG, "deviceid is " + deviceID);
        }
        hideKeyboardOnOutsideTouch();
    }

    /**
     * Hides keyboard when touching outside of edit text or keyboard
     */
    private void hideKeyboardOnOutsideTouch() {
        findViewById(R.id.loginActivityLinearLayout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if(getCurrentFocus() == null || getCurrentFocus().getWindowToken() == null) {
                    return true;
                }
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        TextView loginEmail = (TextView) findViewById(R.id.loginEmailLink);
        loginEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActionCodeSettings actionCodeSettings =
                        ActionCodeSettings.newBuilder()
                                // URL you want to redirect back to. The domain (www.example.com) for this
                                // URL must be whitelisted in the Firebase Console.
                                .setUrl("https://localhost/login")
                                // This must be true
                                .setHandleCodeInApp(true)
                                .setIOSBundleId("to.rent.rentto")
                                .setAndroidPackageName(
                                        "to.rent.rentto",
                                        true, /* installIfNotAvailable */
                                        "12"    /* minimumVersion */)
                                .build();

                FirebaseAuth auth = FirebaseAuth.getInstance();
                String email = mEmail.getText().toString();
                if(email.isEmpty()) {
                    Toast.makeText(mContext, "Please enter in your email to the email input", Toast.LENGTH_SHORT).show();
                    return;
                }
                auth.sendSignInLinkToEmail(email, actionCodeSettings)
                        .addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(mContext, "Email link sent", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    @Override
    public void onBackPressed() {
        // does nothing
        // Because user should not be able to go back
        // when not authenticated
    }

    public boolean validate() {
        boolean valid = true;

        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmail.setError("enter a valid email address");
            valid = false;
        } else {
            mEmail.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            mPassword.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            mPassword.setError(null);
        }

        return valid;
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

    private void init(){

        //initialize the button for logging in
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to log in.");

                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();
                if(isStringNull(email) || isStringNull(password) || !validate()){
                    Toast.makeText(mContext, "There are incorrect or unfilled fields", Toast.LENGTH_SHORT).show();
                }else{
                    Log.d(TAG, "email " + email);
                    Log.d(TAG, "password " + password);
                    mProgressBar.setVisibility(View.VISIBLE);
                    mPleaseWait.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    // If sign in fails, display a message to the user. If sign in succeeds
                                    // the auth state listener will be notified and logic to handle the
                                    // signed in user can be handled in the listener.
                                    if (!task.isSuccessful()) {
                                        Log.w(TAG, "signInWithEmail:failed", task.getException());

                                        Toast.makeText(LoginActivity.this, getString(R.string.auth_failed),
                                                Toast.LENGTH_SHORT).show();
                                        mProgressBar.setVisibility(View.GONE);
                                        mPleaseWait.setVisibility(View.GONE);
                                    }
                                    else{
                                        try{
                                            if(CHECK_IF_VERIFIED){
                                                if(user.isEmailVerified()){
                                                    Log.d(TAG, "onComplete: success. email is verified.");
                                                    FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("phone_number").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            String userPhoneNumber = null;
                                                            try{
                                                                userPhoneNumber = dataSnapshot.getValue(String.class);
                                                            }catch (Exception e) {
                                                                mAuth.signOut();
                                                                Log.e(TAG, "onComplete: NullPointerException: " + e.getMessage());
                                                            }
//                                                            Toast.makeText(mContext, userPhoneNumber, Toast.LENGTH_SHORT).show();
                                                            Log.d(TAG, "phone's number: " + getPhoneNumber());
                                                            Log.d(TAG, "user phone number " + userPhoneNumber);
                                                            if (!userPhoneNumber.equals(getPhoneNumber())) {
                                                                Toast.makeText(mContext, "Phone number of this phone is not associated with this account", Toast.LENGTH_SHORT).show();
                                                                mAuth.signOut();
                                                                mProgressBar.setVisibility(View.GONE);
                                                                mPleaseWait.setVisibility(View.GONE);
                                                            } else {
                                                                FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("deviceID").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                        String userDeviceID = null;
                                                                        try{
                                                                            userDeviceID = dataSnapshot.getValue(String.class);
                                                                        }catch (Exception e) {
                                                                            mAuth.signOut();
                                                                            Log.e(TAG, "onComplete: NullPointerException: " + e.getMessage());
                                                                        }
//                                                                        Toast.makeText(mContext, userDeviceID, Toast.LENGTH_SHORT).show();
                                                                        if (!userDeviceID.equals(getDeviceID())) {
                                                                            Toast.makeText(mContext, "Device ID of this phone is not associated with this account", Toast.LENGTH_SHORT).show();
                                                                            mProgressBar.setVisibility(View.GONE);
                                                                            mPleaseWait.setVisibility(View.GONE);
                                                                            mAuth.signOut();
                                                                        } else {
//                                                                            Toast.makeText(mContext, "Login Successfully", Toast.LENGTH_SHORT).show();
                                                                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                                                            startActivity(intent);
                                                                            finish();
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                    }
                                                                });
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                                }else{
                                                    Toast.makeText(mContext, "Email is not verified \n check your email inbox. We are sending you a new verification link", Toast.LENGTH_SHORT).show();
                                                    mProgressBar.setVisibility(View.GONE);
                                                    mPleaseWait.setVisibility(View.GONE);
                                                    mAuth.getCurrentUser().sendEmailVerification();
                                                    mAuth.signOut();
                                                }
                                            }
                                            else{
                                                Log.d(TAG, "onComplete: success. email is verified.");
                                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                                startActivity(intent);
                                            }

                                        }catch (NullPointerException e){
                                            Log.e(TAG, "onComplete: NullPointerException: " + e.getMessage() );
                                        }
                                    }
                                }
                            });
                }

            }
        });

        linkSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to register screen");
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

         /*
         If the user is logged in then navigate to HomeActivity and call 'finish()'
          */
        if(mAuth.getCurrentUser() != null){
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private String getDeviceID(){
        return deviceID;
    }

    private String getPhoneNumber(){
        return phoneNumber;
    }
    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();
    }

}