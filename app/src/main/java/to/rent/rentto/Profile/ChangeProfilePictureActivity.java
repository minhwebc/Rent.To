package to.rent.rentto.Profile;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import to.rent.rentto.R;

public class ChangeProfilePictureActivity extends AppCompatActivity{

    private static final String TAG = "ChangeProfileActivity";
    private Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_profile_picture);
        mContext = ChangeProfilePictureActivity.this;
    }
}
