package to.rent.rentto.Listing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import to.rent.rentto.R;


public class FilterActivity extends AppCompatActivity {

    private static final String CATEGORY_RESULT_KEY_MESSAGE = "Category_Result";
    private static final String DISTANCE_RESULT_KEY_MESSAGE = "Distance_Result";
    private static final String TAG = "FilterFragment";
    private final String[] categoryValues = {"Manual Tools", "Motor Tools", "Sports Equipment", "Cookware", "Videogames", "Electronics", "Movies", "Parking Spot", "Party Supplies", "Other"};
    final String[] distanceValues= {"5 mi", "10 mi", "20 mi", "30 mi", "40 mi", "50 mi"};

    private TextView distanceTextView;
    private Context mContext;
    private Spinner spinner;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        Log.d(TAG, "inside of ItemsListActivity.java");
        mContext = FilterActivity.this;

        loadSpinner();
        loadDistanceSeekBar();
        setupApplyButton();
    }

    private void loadSpinner(){
        spinner = (Spinner) findViewById(R.id.spinnerCategory);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, categoryValues);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setSelection(0);
    }

    private void loadDistanceSeekBar() {
        // Hook up condition SeekBar and conditionTextView
        SeekBar distanceSeekBar = (SeekBar) findViewById(R.id.conditionSeeker);

        distanceTextView = (TextView) findViewById(R.id.textView10);

        distanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int stepSize = 20; // How much the seekbar increments
                progress = (progress/stepSize) * stepSize;
                seekBar.setProgress(progress);
                progressChangedValue = progress;
                setTextView();
            }

            // Updates the condition textview
            private void setTextView() {
                String distance = distanceValues[3] + "";
                switch (progressChangedValue) {
                    case 0:
                        distance = distanceValues[0] + "";
                        break;
                    case 20:
                        distance = distanceValues[1] + "";
                        break;
                    case 40:
                        distance = distanceValues[2] + "";
                        break;
                    case 60:
                        distance = distanceValues[3] + "";
                        break;
                    case 80:
                        distance = distanceValues[4] + "";
                        break;
                    case 100:
                        distance = distanceValues[5] + "";
                        break;
                }
                distanceTextView.setText(distance);
            }



            // These need to be implemented for OnSeekBarChangeListener
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        // Set seekbar default index to 0
        distanceSeekBar.setProgress(60);
        distanceTextView.setText(distanceValues[3] + "");

    }

    private void setupApplyButton(){
        android.support.design.widget.FloatingActionButton btn = (android.support.design.widget.FloatingActionButton) findViewById(R.id.fab);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                String category = spinner.getSelectedItem().toString();
                Double distance = Double.parseDouble((distanceTextView.toString()));
                Intent intent = new Intent();
                intent.putExtra(CATEGORY_RESULT_KEY_MESSAGE, category);
                intent.putExtra(DISTANCE_RESULT_KEY_MESSAGE, distance);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

    public static String getCategory(Intent intent){
        return intent.getStringExtra(CATEGORY_RESULT_KEY_MESSAGE);
    }

    public static double getDistance(Intent intent) {
        return intent.getDoubleExtra(DISTANCE_RESULT_KEY_MESSAGE, 20);
    }

}