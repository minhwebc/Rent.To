package to.rent.rentto.Home;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import to.rent.rentto.R;

public class WelcomeActivity extends AppCompatActivity {
    private final String TAG = "WelcomeActivity";
    private final int granted = PackageManager.PERMISSION_GRANTED;
    private final String camera = Manifest.permission.CAMERA;
    private final String location = Manifest.permission.ACCESS_FINE_LOCATION;
    private final String phone = Manifest.permission.READ_PHONE_NUMBERS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Log.d(TAG, "Inside of onCreate");
//        Intent intent = new Intent(this, HomeActivity.class);
//        startActivity(intent);
//        finish();
        // Gets Permissions
        if(checkSelfPermission(camera) != granted || checkSelfPermission(location) != granted || checkSelfPermission(phone) != granted) {
            Log.d(TAG,"Trying to get permission");
            ActivityCompat.requestPermissions(this, new String[]{camera, location, phone}, 1);
        } else {
            start();
        }
    }

    private void start() {
        SharedPreferences prefs = getSharedPreferences("Rent.toPrefs", MODE_PRIVATE);
        if(true || !prefs.getBoolean("hasSeenTutorial", false)) {
            showTutorial();
            SharedPreferences.Editor editor = getSharedPreferences("Rent.toPrefs", MODE_PRIVATE).edit();
            editor.putBoolean("hasSeenTutorial", true);
            editor.apply();
        } else {
            finishWelcome();
        }
    }

    private void finishWelcome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void showTutorial() {
        Intent intent = new Intent(this, TutorialActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0) {
            if(resultCode == RESULT_OK) {
                finishWelcome();
            } else {
                showTutorial();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        start();
    }
}
