package to.rent.rentto.Camera;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import to.rent.rentto.R;

/**
 * Created by Brandon on 2/12/2018.
 */

public class ConfirmPictureFragment extends Fragment {
    private static final String TAG = "ConfirmPictureFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confirm_picture, container, false);
        Log.d(TAG, "inside of ConfirmPictureFragment.java onCreateView");
        return view;
    }
}
