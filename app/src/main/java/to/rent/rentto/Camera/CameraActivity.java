package to.rent.rentto.Camera;

import android.content.Context;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;


import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.net.URI;

import to.rent.rentto.R;
import to.rent.rentto.Utils.BottomNavigationViewHelper;

/**
 * Created by Brandon on 2/12/2018.
 */

public class CameraActivity extends AppCompatActivity {
    private static final String TAG = "CameraActivity";

    private Context mContext = CameraActivity.this;
    private static final int ACTIVITY_NUM = 1;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_CAMERA_ROLL = 2;

    Uri imgaegUri;
    ImageView imageView;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        Log.d(TAG, "onCreate: Started.");

        Button cameraButton = (Button) findViewById(R.id.buttonCamera);
        Button cameraRollButton = (Button) findViewById(R.id.buttonCameraRoll);
        imageView = (ImageView) findViewById(R.id.cameraImageView);

        setupBottomNavigationView();


    }

    public void launchCamera(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Take a picture and pass results to onActivityResult

        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);

    }

    public void launchCameraRoll(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);

        startActivityForResult(intent, REQUEST_CAMERA_ROLL);
    }

    // if you want to return the image taken


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //Get the photo
            Bundle extras = data.getExtras();
            Bitmap photo = (Bitmap) extras.get("data");
            imageView.setImageBitmap(photo);

        } else if(requestCode == REQUEST_CAMERA_ROLL && resultCode == RESULT_OK) {
            imgaegUri = data.getData();
            imageView.setImageURI(imgaegUri);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up bottomnavigationview");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
