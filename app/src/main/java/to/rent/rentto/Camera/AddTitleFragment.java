package to.rent.rentto.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import to.rent.rentto.R;

public class AddTitleFragment extends android.support.v4.app.Fragment {
    private static final String TAG = "AddTitleFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_title, container, false);
        Log.d(TAG, "inside of AddTitleFragment.java onCreateView");
        return view;
    }
}
