package to.rent.rentto.Dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import to.rent.rentto.R;

public class ConfirmPasswordDialog extends android.support.v4.app.DialogFragment {
    private static final String TAG = "ConfirmPasswordDialog";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_confirm_password, container, false);
        Log.d(TAG, "onCreateView: started");

        return view;
    }
}
