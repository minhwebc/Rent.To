package to.rent.rentto.Home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import to.rent.rentto.R;

/**
 * Created by Brandon on 2/12/2018.
 */

public class WelcomeFragment extends Fragment {
    private static final String TAG = "WelcomeFragment";
    public static final java.lang.String ARG_PAGE = "arg_page";

    public WelcomeFragment() {

    }

    public static WelcomeFragment newInstance(int position) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, position);
        WelcomeFragment fragment = new WelcomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int position = getArguments().getInt(ARG_PAGE);
        int resource = R.layout.fragment_confirm_photo;
        switch(position) {
            case 0:
                resource = R.layout.fragment_welcome_tutorial;
                break;
            case 1:
                resource = R.layout.fragment_lender_tutorial;
                break;
            case 2:
                resource = R.layout.fragment_borrower_tutorial;
                break;
            case 3:
                resource = R.layout.fragment_home_tutorial;
                break;
            case 4:
                resource = R.layout.fragment_post_tutorial;
                break;
            case 5:
                resource = R.layout.fragment_reminders_tutorial;
                break;
            case 6:
                resource = R.layout.fragment_profile_tutorial;
                break;
            case 7:
                resource = R.layout.fragment_messages_tutorial;
                break;
            case 8:
                resource = R.layout.fragment_getstarted_tutorial;
                break;
            default:
                resource = R.layout.fragment_getstarted_tutorial;
                break;
        }
        View view = inflater.inflate(resource, container, false);
//        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        Log.d(TAG, "inside of onCreateView");
        return view;
    }
}
