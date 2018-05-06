package to.rent.rentto.Login;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import to.rent.rentto.R;

/**
 * PolicyAgreementActivity
 * User can scroll through the policy
 * User can press buttons to accept or decline the agreement
 * If declined, will return RESULT_CANCELED
 * If accepted, will return RESULT_OK
 * If backpress, will return RESULT_CANCELED
 */

public class PolicyAgreementActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy_agreement);
        initButtons();
    }

    /**
     * Initializes the elements on screen
     * Makes policy TextView scrollable
     * Adds on click listeners to accept and decline buttons
     * Accept button OnClick returns RESULT_OK and finishes activity
     * Decline button OnClick returns RESULT_CANCELED and finishes activity
     */
    private void initButtons() {
        Button accept = (Button) findViewById(R.id.button_accept);
        Button decline = (Button) findViewById(R.id.button_decline);
        TextView textView = (TextView) findViewById(R.id.policyTextView);
        textView.setMovementMethod(new ScrollingMovementMethod());
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                AlertDialog.Builder builder = new AlertDialog.Builder(PolicyAgreementActivity.this);
                builder.setMessage("I have read and agreed to Rent.to terms and conditions")
                        .setTitle("Terms and Conditions")
                        .setCancelable(true)
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setPositiveButton("Agree", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });


                AlertDialog alert = builder.create();
                alert.show();
            }
        });
        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                AlertDialog.Builder builder = new AlertDialog.Builder(PolicyAgreementActivity.this);
                builder.setMessage("You need to agree to the terms and conditions to use Rent.to. \nAre you sure you want to decline?")
                        .setTitle("Important")
                        .setCancelable(true)
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                        })
                        .setPositiveButton("Yes, I am sure", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });


                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }


}
