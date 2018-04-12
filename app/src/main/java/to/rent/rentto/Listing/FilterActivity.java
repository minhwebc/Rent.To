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
import android.widget.Spinner;

import to.rent.rentto.R;


public class FilterActivity extends AppCompatActivity {

    private static final String RESULT_KEY_MESSAGE = "Result";
    private static final String TAG = "FilterFragment";
    private final String[] categoryValues = {"Manual Tools", "Motor Tools", "Sports Equipment", "Cookware", "Videogames", "Electronics", "Movies", "Parking Spot", "Party Supplies", "Other"};

    private Context mContext;
    private Spinner spinner;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        Log.d(TAG, "inside of ItemsListActivity.java");
        mContext = FilterActivity.this;

        loadSpinner();
        setupApplyButton();
    }

    private void loadSpinner(){
        spinner = (Spinner) findViewById(R.id.spinnerCategory);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, categoryValues);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setSelection(0);
    }

    private void setupApplyButton(){
        android.support.design.widget.FloatingActionButton btn = (android.support.design.widget.FloatingActionButton) findViewById(R.id.fab);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                String result = spinner.getSelectedItem().toString();

                Intent intent = new Intent();
                intent.putExtra(RESULT_KEY_MESSAGE, result);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

    public static String getResult(Intent intent){
        return intent.getStringExtra(RESULT_KEY_MESSAGE);
    }

}
