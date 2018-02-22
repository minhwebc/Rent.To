package to.rent.rentto.Camera;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
//import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import to.rent.rentto.R;

/**
 * Created by Brandon on 2/12/2018.
 */

public class AddTitleFragment extends android.support.v4.app.Fragment {
    private static final String TAG = "AddTitleFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_title, container, false);
        final android.support.v4.app.Fragment self = this;
        Log.d(TAG, "inside of AddTitleFragment.java onCreateView");
        return view;
    }
}
