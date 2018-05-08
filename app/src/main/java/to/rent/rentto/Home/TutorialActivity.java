package to.rent.rentto.Home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import to.rent.rentto.R;

public class TutorialActivity extends AppCompatActivity {
    private final String TAG = "TutorialActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        Log.d(TAG, "Inside onCreate");
        setResult(RESULT_OK);
    }
}
