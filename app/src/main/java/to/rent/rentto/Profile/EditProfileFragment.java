package to.rent.rentto.Profile;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import to.rent.rentto.R;

/**
 * Created by allencho on 2/15/18.
 */

public class EditProfileFragment extends Fragment {
    private static final String TAG = "EditProfileFragment";


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v(TAG, "here");
        View view = inflater.inflate(R.layout.fragment_editprofile, container, false);

        return view;
    }
}
