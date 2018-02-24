package to.rent.rentto.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import to.rent.rentto.R;

public class LocationFragment extends android.support.v4.app.Fragment {
    private static final String TAG = "LocationFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_location, container, false);
        Log.d(TAG, "inside of LocationFragment.java onCreateView");
        return view;
    }
}
