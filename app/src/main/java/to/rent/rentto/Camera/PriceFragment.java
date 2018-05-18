package to.rent.rentto.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import java.math.BigDecimal;
import java.text.NumberFormat;
import to.rent.rentto.R;

public class PriceFragment extends android.support.v4.app.Fragment {
    private static final String TAG = "PriceFragment";
    private String[] values = {"Hour", "Day", "Week", "Month", "Year"};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_price, container, false);
        Log.d(TAG, "inside of PriceFragment.java onCreateView");

        // Set up price EditText
        initiatePriceEditText(view);

        // Set up Time spinner
        initiateTimeSpinner(view);

        return view;
    }

    /**
     * Sets up spinner for time
     * Default value is String "Day"
     * @param v
     */
    private void initiateTimeSpinner(View v) {
        Spinner spinner = (Spinner) v.findViewById(R.id.timeSpinner);

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, values);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        // set spinner default value to 0 index
        spinner.setSelection(1);
    }

    /**
     * Sets up Price EditText
     * @param v
     */
    private void initiatePriceEditText(View v) {
        final EditText editTextPrice = (EditText) v.findViewById(R.id.editTextPrice);
//        editTextPrice.addTextChangedListener(new DecimalFilter(editTextPrice, getActivity()));
        editTextPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editTextPrice == null) return;
                String inputString = editable.toString();
                editTextPrice.removeTextChangedListener(this);
                String cleanString = inputString.toString().replaceAll("[$,.]", "");
                BigDecimal bigDecimal = new BigDecimal(cleanString).setScale(2, BigDecimal.ROUND_FLOOR).divide(new BigDecimal(100), BigDecimal.ROUND_FLOOR);
                String  converted = NumberFormat.getCurrencyInstance().format(bigDecimal);
                editTextPrice.setText(converted);
                editTextPrice.setSelection(converted.length());
                editTextPrice.addTextChangedListener(this);
            }
        });
    }
}
