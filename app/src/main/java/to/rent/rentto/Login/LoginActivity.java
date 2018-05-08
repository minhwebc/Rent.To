package to.rent.rentto.Login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import to.rent.rentto.Home.HomeActivity;
import to.rent.rentto.R;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final Boolean CHECK_IF_VERIFIED = false;
    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;

    //firebase
    private FirebaseAuth mAuth;

    private Context mContext;
    private ProgressBar mProgressBar;
    private EditText mEmail, mPassword;
    private TextView mPleaseWait;
    private FloatingActionButton floatingActionButton;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private boolean mVerificationInProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        updateUIButtons();
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mPleaseWait = (TextView) findViewById(R.id.pleaseWait);
        mEmail = (EditText) findViewById(R.id.input_email);
        mPassword = (EditText) findViewById(R.id.input_password);
        mContext = LoginActivity.this;
        Log.d(TAG, "onCreate: started.");
//        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
//        floatingActionButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(LoginActivity.this, VideoActivity.class);
//                startActivity(intent);
//            }
//        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);

                //signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }

                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

            }
        };

        mPleaseWait.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);

        setupFirebaseAuth();
        init();
    }

    private PhoneAuthCredential grabPhoneCodeInfo(){
        EditText phoneNumberField = (EditText) findViewById(R.id.phoneNumberField);
        String code = phoneNumberField.getText().toString();
        if (TextUtils.isEmpty(code)) {
            phoneNumberField.setError("Cannot be empty.");
            return null;
        }
        return verifyPhoneNumberWithCode(mVerificationId, code);
    }

    private PhoneAuthCredential verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        //signInWithPhoneAuthCredential(credential);
        return credential;
    }

    // [START sign_in_with_phone]
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            Toast.makeText(LoginActivity.this, "Phone number verification success",
                                    Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                            // [START_EXCLUDE]
                            updateUI(STATE_SIGNIN_SUCCESS);
                            // [END_EXCLUDE]

                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                // [START_EXCLUDE silent]
                                //mVerificationField.setError("Invalid code.");
                                // [END_EXCLUDE]
                            }
                            // [START_EXCLUDE silent]
                            // Update UI
                            updateUI(STATE_SIGNIN_FAILED);
                            // [END_EXCLUDE]
                        }
                    }
                });
    }

    private void updateUIButtons(){
        final TextView phoneLoginLink = (TextView) findViewById(R.id.phoneLoginLink);
        final TextView backLoginLink = (TextView) findViewById(R.id.backLoginLink);
        backLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.emailInput).setVisibility(View.VISIBLE);
                findViewById(R.id.paswordInput).setVisibility(View.VISIBLE);
                phoneLoginLink.setVisibility(View.VISIBLE);
                TextInputLayout phoneNumberLayout = (TextInputLayout) findViewById(R.id.phoneNumber);
                phoneNumberLayout.setVisibility(View.GONE);
                findViewById(R.id.phoneDirect).setVisibility(View.GONE);
                //findViewById(R.id.verifyLink).setVisibility(View.GONE);
                findViewById(R.id.resendLink).setVisibility(View.GONE);
                backLoginLink.setVisibility(View.GONE);

            }
        });

        Button verifyLinkButton = (Button) findViewById(R.id.verifyLink);
        verifyLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                grabPhoneCodeInfo();
            }
        });
        Button resendLinkButton = (Button) findViewById(R.id.resendLink);
        resendLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        phoneLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextInputLayout phoneNumberLayout = (TextInputLayout) findViewById(R.id.phoneNumber);
                phoneNumberLayout.setVisibility(View.VISIBLE);
                backLoginLink.setVisibility(View.VISIBLE);
                phoneLoginLink.setVisibility(View.GONE);
                findViewById(R.id.emailInput).setVisibility(View.GONE);
                findViewById(R.id.paswordInput).setVisibility(View.GONE);
                findViewById(R.id.phoneDirect).setVisibility(View.VISIBLE);
                //findViewById(R.id.verifyLink).setVisibility(View.VISIBLE);
                findViewById(R.id.resendLink).setVisibility(View.VISIBLE);
                startPhoneNumberVerification("2069811465");
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
        Button btnLogin = (Button) findViewById(R.id.btn_login);
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
                                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                                    startActivity(intent);
                                                }else{
                                                    Toast.makeText(mContext, "Email is not verified \n check your email inbox.", Toast.LENGTH_SHORT).show();
                                                    mProgressBar.setVisibility(View.GONE);
                                                    mPleaseWait.setVisibility(View.GONE);
                                                    mAuth.signOut();
                                                }
                                            }
                                            else{
                                                Toast.makeText(mContext, "Email and Password verification successfull", Toast.LENGTH_SHORT).show();

                                                AuthCredential credential = grabPhoneCodeInfo();
                                                mAuth.signInWithCredential(credential)
                                                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d(TAG, "linkWithCredential:success");
                                                                    //updateUI(user);
                                                                    Toast.makeText(mContext, "Phone verification successful", Toast.LENGTH_SHORT).show();
                                                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                                                    startActivity(intent);
                                                                    finish();
                                                                } else {
                                                                    Log.w(TAG, "linkWithCredential:failure", task.getException());
                                                                    Toast.makeText(mContext, "Authentication failed.",
                                                                            Toast.LENGTH_SHORT).show();
                                                                    //updateUI(null);
                                                                }

                                                                // ...
                                                            }
                                                        });

                                                Log.d(TAG, "onComplete: success. email is verified.");

                                            }

                                        }catch (NullPointerException e){
                                            Log.e(TAG, "onComplete: NullPointerException: " + e.getMessage() );
                                        }
                                    }

                                    // ...
                                }
                            });
                }

            }
        });

        TextView linkSignUp = (TextView) findViewById(R.id.link_signup);
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

    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        mVerificationInProgress = true;
    }

    private void updateUI(int uiState) {
        switch (uiState) {
            case STATE_INITIALIZED:
            case STATE_CODE_SENT:
            case STATE_VERIFY_FAILED:
            case STATE_VERIFY_SUCCESS:
            case STATE_SIGNIN_FAILED:
            case STATE_SIGNIN_SUCCESS:
        }
    }
    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();
    }

}
