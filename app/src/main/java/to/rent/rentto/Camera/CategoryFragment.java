package to.rent.rentto.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import to.rent.rentto.R;

public class CategoryFragment extends android.support.v4.app.Fragment {
    private static final String TAG = "AddTitleFragment";
    final String[] categoryValues= {"Manual Tools", "Motor Tools", "Sports Equipment", "Cookware", "Videogames", "Electronics", "Movies", "Parking Spot", "Party Supplies", "Other"};
    final String[] conditionValues= {"New", "Used - Like New", "Used - Very Good", "Used - Good", "Used - Acceptable", "Bad"};
    TextView conditionTextView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_category, container, false);
        Log.d(TAG, "inside of CategoryFragment.java onCreateView");

        // Hook up user controlled items
        initiateCategorySpinner(view);
        initiateConditionSeekBar(view);
        return view;
    }

    private void initiateCategorySpinner(View view) {
        Spinner spinner = (Spinner) view.findViewById(R.id.spinnerCategory);

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, categoryValues);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
    }

    // Hooks up conditionSeeker SeekBar
    // conditionTextView TextView updates when bar is used
    private void initiateConditionSeekBar(View view) {
        // Hook up condition SeekBar and conditionTextView
        SeekBar conditionSeeker = (SeekBar) view.findViewById(R.id.conditionSeeker);
        conditionTextView = (TextView) view.findViewById(R.id.conditionTextView);
        conditionSeeker.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int stepSize = 20; // how much the seekbar increments by each teime
                progress = (progress/stepSize)*stepSize;
                seekBar.setProgress(progress);
                progressChangedValue = progress;
                setTextView();
            }

            // Updates the condition textview
            private void setTextView() {
                String condition = conditionValues[0];
                switch (progressChangedValue) {
                    case 0:
                        condition = conditionValues[0];
                        break;
                    case 20:
                        condition = conditionValues[1];
                        break;
                    case 40:
                        condition = conditionValues[2];
                        break;
                    case 60:
                        condition = conditionValues[3];
                        break;
                    case 80:
                        condition = conditionValues[4];
                        break;
                    case 100:
                        condition = conditionValues[5];
                        break;
                }
                conditionTextView.setText(condition);
            }

            // These need to be implemented for OnSeekBarChangeListener
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }
}
