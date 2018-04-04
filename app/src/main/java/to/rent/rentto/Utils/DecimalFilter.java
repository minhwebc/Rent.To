package to.rent.rentto.Utils;

import android.app.Activity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;

public class DecimalFilter implements TextWatcher {

    int count= -1 ;
    EditText et;
    Activity activity;

    public DecimalFilter(EditText edittext, Activity activity) {
        et = edittext;
        this.activity = activity;
    }

    public void afterTextChanged(Editable s) {

        if (s.length() > 0) {
            String str = et.getText().toString();
            et.setOnKeyListener(new OnKeyListener() {

                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_DEL) {
                        count--;
                        InputFilter[] fArray = new InputFilter[1];
                        fArray[0] = new InputFilter.LengthFilter(6);//Re sets the maxLength of edittext to 100.
                        et.setFilters(fArray);
                    }
                    return false;
                }
            });

            char t = str.charAt(s.length() - 1);

            if (t == '.') {
                count = 0;
            }

            if (count >= 0) {
                if (count == 2) {
                    InputFilter[] fArray = new InputFilter[1];
                    fArray[0] = new InputFilter.LengthFilter(s.length());
                    et.setFilters(fArray); // sets edittext's maxLength to number of digits now entered.

                }
                count++;
            }
        }

    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
// TODO Auto-generated method stub
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
// TODO Auto-generated method stub
    }

}