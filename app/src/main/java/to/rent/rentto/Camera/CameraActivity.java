package to.rent.rentto.Camera;

import android.app.FragmentTransaction;
import android.content.Context;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
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

import to.rent.rentto.Home.*;
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
    Button confirmButton;
    Button cameraRollButton;
    Button cameraButton;
    Button cancelButton;

    Uri imgaegUri;
    ImageView imageView;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        Log.d(TAG, "onCreate: Started.");

        cameraButton = (Button) findViewById(R.id.buttonCamera);
        cameraRollButton = (Button) findViewById(R.id.buttonCameraRoll);
        confirmButton = (Button) findViewById(R.id.buttonConfirmPicture);
        cancelButton = (Button) findViewById(R.id.buttonCancel);
        imageView = (ImageView) findViewById(R.id.cameraImageView);

        confirmButton.setVisibility(View.GONE); // hides on start, only after picture is selected
        cancelButton.setVisibility(View.GONE);
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

    // When the confirm button is pressed, uses picture for new posting screen
    public void launchConfirm(View view) {
        ConfirmPictureFragment confirmPictureFragment = new ConfirmPictureFragment();
        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        Bundle bundle = new Bundle();
        //bundle.putString("uri", imgaegUri.toString());
        transaction.replace(R.id.relLayout2, confirmPictureFragment).commit();
        imageView.setVisibility(View.GONE);

    }

    public void launchCancel(View view) {
        confirmButton.setVisibility(View.GONE); // only visible when a picture is selected
        cancelButton.setVisibility(View.GONE);
        cameraRollButton.setVisibility(View.VISIBLE); // since no pic is selected
        cameraButton.setVisibility(View.VISIBLE);
        imageView.setImageResource(0);
    }


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
        if(resultCode == RESULT_OK) {
            confirmButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
            cameraButton.setVisibility(View.GONE);
            cameraRollButton.setVisibility(View.GONE);
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


    // When user presses submit after entering in text to form
    public void submit(View view) {
        Log.d(TAG, "inside of submit");
    }





}
