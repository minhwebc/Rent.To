package to.rent.rentto.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import to.rent.rentto.R;

public class CategoryFragment extends android.support.v4.app.Fragment {
    private static final String TAG = "AddTitleFragment";
    final String[] categoryValues= {"Manual Tools","Sports Equipment", "Cookware", "Videogames", "Electronics", "Movies", "Parking Spot", "Party Supplies", "Other"};
    final String[] conditionValues= {"New", "Used - Like New", "Used - Very Good", "Used - Good", "Used - Acceptable", "Bad", "Other"};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_category, container, false);
        Log.d(TAG, "inside of CategoryFragment.java onCreateView");


        // Hooks up to category picker
        NumberPicker categoryNP = view.findViewById(R.id.categoryPicker);
        NumberPicker conditionNP = view.findViewById(R.id.conditionPicker);


        //Populate NumberPicker values from String array values
        //Set the minimum value of NumberPicker
        categoryNP.setMinValue(0); //from array first value
        conditionNP.setMinValue(0); //from array first value
        //Specify the maximum value/number of NumberPicker
        categoryNP.setMaxValue(categoryValues.length-1); //to array last value
        conditionNP.setMaxValue(conditionValues.length-1); //to array last value

        //Specify the NumberPicker data source as array elements
        categoryNP.setDisplayedValues(categoryValues);
        conditionNP.setDisplayedValues(conditionValues);


        //Gets whether the selector wheel wraps when reaching the min/max value.
        categoryNP.setWrapSelectorWheel(true);
        conditionNP.setWrapSelectorWheel(true);
        return view;
    }
}
