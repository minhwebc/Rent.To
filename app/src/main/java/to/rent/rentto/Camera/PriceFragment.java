package to.rent.rentto.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import to.rent.rentto.R;

public class PriceFragment extends android.support.v4.app.Fragment {
    private static final String TAG = "PriceFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_price, container, false);
        Log.d(TAG, "inside of PriceFragment.java onCreateView");

        String[] values = {"Hour", "Day", "Week", "Month", "Year"};

        // Hooks up to category picker
        NumberPicker np = view.findViewById(R.id.timePicker);

        //Populate NumberPicker values from String array values
        //Set the minimum value of NumberPicker
        np.setMinValue(0); //from array first value
        //Specify the maximum value/number of NumberPicker
        np.setMaxValue(values.length-1); //to array last value

        //Specify the NumberPicker data source as array elements
        np.setDisplayedValues(values);
        np.setValue(1); // starts at first index (day)
        //Gets whether the selector wheel wraps when reaching the min/max value.
        np.setWrapSelectorWheel(true);
        return view;
    }
}
