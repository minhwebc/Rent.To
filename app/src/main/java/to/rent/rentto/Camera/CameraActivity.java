package to.rent.rentto.Camera;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.KeyguardManager;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.widget.NumberPicker;


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
    android.support.v4.app.FragmentManager fragmentManager;
    Button confirmButton;
    Button cameraRollButton;
    Button cameraButton;
    Button cancelButton;


    Uri imgaegUri;  // the uri, for the image (for uploading)
    ImageView imageView;

    String title; // the title, for post
    String description; // the description, for post
    String category; // the category, for post
    int price; // the price, for post
    String duration; // the duration, for post
    int zipcode; // the zipcode, for post



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
        AddTitleFragment addTitleFragment = new AddTitleFragment();
        fragmentManager= getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();

        Bundle bundle = new Bundle();
        //bundle.putString("uri", imgaegUri.toString());
        transaction.add(R.id.relLayout2, addTitleFragment, "title").commit();
        imageView.setVisibility(View.GONE);
        confirmButton.setVisibility(View.GONE); // only visible when a picture is selected
        cancelButton.setVisibility(View.GONE);
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
    public void submitTitle(View view) {
        Log.d(TAG, "inside of submitTitle, cameraAcitivity");
        EditText editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        title = editTextTitle.getText().toString();
        Log.d(TAG, "The title is " + title + " inside cameraActivity");
        android.support.v4.app.Fragment titleFragment = fragmentManager.findFragmentByTag("title");
        CategoryFragment categoryFragment = new CategoryFragment();
        fragmentManager.beginTransaction().remove(titleFragment).commit();
        fragmentManager.beginTransaction().add(R.id.relLayout2, categoryFragment, "category").commit();
    }

    public void submitCategory(View view) {
        Log.d(TAG, "inside of submitCategory, cameraAcitivity");

        NumberPicker categoryPicker = (NumberPicker) findViewById(R.id.categoryPicker);
        category = categoryPicker.getDisplayedValues()[categoryPicker.getValue()];
        EditText editTextDescription = (EditText) findViewById(R.id.editTextDescription);
        description = editTextDescription.getText().toString();

        Log.d(TAG, "The category is " + category + " inside cameraActivity");
        Log.d(TAG, "The description is " + description + " inside cameraActivity");
        //android.support.v4.app.Fragment titleFragment = fragmentManager.findFragmentByTag("title");
        //CategoryFragment categoryFragment = new CategoryFragment();
        //fragmentManager.beginTransaction().remove(titleFragment).commit();
        //fragmentManager.beginTransaction().add(R.id.relLayout2, categoryFragment, "category").commit();
    }






}
