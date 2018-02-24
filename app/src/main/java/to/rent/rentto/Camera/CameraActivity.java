package to.rent.rentto.Camera;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.widget.Toast;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import to.rent.rentto.R;
import to.rent.rentto.Utils.BottomNavigationViewHelper;

public class CameraActivity extends AppCompatActivity {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
    private static final String TAG = "CameraActivity";
    private Context mContext = CameraActivity.this;
    private static final int ACTIVITY_NUM = 1;
    static final int REQUEST_IMAGE_CAPTURE = 1; // the request code number assigned to image capture
    static final int REQUEST_CAMERA_ROLL = 2;   // the request code number assigned to camera roll
    android.support.v4.app.FragmentManager fragmentManager;  // handles fragment switching
    Button confirmButton;
    Button cameraRollButton;
    Button cameraButton;
    Button cancelButton;
    Uri imgaegUri;  // the uri, for the image (for uploading)
    ImageView imageView;  // Where the selected image will be displayed
    String title; // the title, for post
    String description; // the description, for post
    String category; // the category, for post
    double price; // the price, for post
    String duration; // the duration, for post, Examples: ["31 days", "2 weeks", "1 months", "2 years"]
    String condition = "Condition - Change This later";
    String city; // the zipcode, for post

    /**
     * Hooks up buttons from camera fragment
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        cameraButton = (Button) findViewById(R.id.buttonCamera);
        cameraRollButton = (Button) findViewById(R.id.buttonCameraRoll);
        confirmButton = (Button) findViewById(R.id.buttonConfirmPicture);
        cancelButton = (Button) findViewById(R.id.buttonCancel);
        imageView = (ImageView) findViewById(R.id.cameraImageView);
        confirmButton.setVisibility(View.GONE); // hides on start, only after picture is selected
        cancelButton.setVisibility(View.GONE);
        setupBottomNavigationView();
    }

    /**
     * Launches the camera, and feeds result to imageView.
     * @param view
     */
    public void launchCamera(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Take a picture and pass results to onActivityResult
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    /**
     * Launches the camera roll, and feeds result to imageView.
     * @param view
     */
    public void launchCameraRoll(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CAMERA_ROLL);
    }

    /**
     * Switches to addTitleFragment and makes all elements from CameraActivity relLayout2 invisible
     * @param view
     */
    public void launchConfirm(View view) {
        AddTitleFragment addTitleFragment = new AddTitleFragment();
        fragmentManager= getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.relLayout2, addTitleFragment, "title").commit();
        imageView.setVisibility(View.GONE);
        confirmButton.setVisibility(View.GONE); // only visible when a picture is selected
        cancelButton.setVisibility(View.GONE);
    }

    /**
     * Resets CameraActivity to default viewing state
     * @param view
     */
    public void launchCancel(View view) {
        confirmButton.setVisibility(View.GONE); // only visible when a picture is selected
        cancelButton.setVisibility(View.GONE);
        cameraRollButton.setVisibility(View.VISIBLE); // since no pic is selected
        cameraButton.setVisibility(View.VISIBLE);
        imageView.setImageResource(0);
    }

    /**
     * Gets image, and displays to image found from URI
     * @param requestCode   Either from roll or from camera
     * @param resultCode    Should be RESULT_OK
     * @param data          Will be a URI for image.
     */
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

    /**
     * Sets up the bottom navigation view
     */
    private void setupBottomNavigationView(){
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    /**
     * Switches fragments, placed on relLayout2 in activity_camera.xml
     * @param oldTag    The tag for the old fragment
     * @param fragment  The new fragment
     * @param newTag    The tag for the new fragment
     */
    private void changeFragment(String oldTag, android.support.v4.app.Fragment fragment, String newTag) {
        android.support.v4.app.Fragment oldFragment = fragmentManager.findFragmentByTag(oldTag);
        fragmentManager.beginTransaction().remove(oldFragment).commit();
        fragmentManager.beginTransaction().add(R.id.relLayout2, fragment, newTag).commit();
    }

    /**
     * Returns if the editText has nonempty user input
     * @param editText  The editText that we are checking
     * @return  boolean for whether editText has user input
     */
    private boolean checkEditTextNonEmpty(EditText editText) {
        return !editText.getText().toString().matches("");
    }

    /**
     * Gets title from user input, switches to category selection fragment
     * Title is never empty
     * @param view
     */
    public void submitTitle(View view) {
        EditText editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        title = editTextTitle.getText().toString();
        if(checkEditTextNonEmpty(editTextTitle)) {
            // Replace title fragment with category fragment
            CategoryFragment categoryFragment = new CategoryFragment();
            changeFragment("title", categoryFragment, "category");
        } else {
            Toast.makeText(mContext, "Please enter a title",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Gets category and description from user input, switches to price and duration fragment
     * Description may be empty
     * Duration is never empty
     * @param view
     */
    public void submitCategory(View view) {
        NumberPicker categoryPicker = (NumberPicker) findViewById(R.id.categoryPicker);
        category = categoryPicker.getDisplayedValues()[categoryPicker.getValue()];
        EditText editTextDescription = (EditText) findViewById(R.id.editTextDescription);
        description = editTextDescription.getText().toString();
        // Replace category fragment with price fragment
        PriceFragment priceFragment = new PriceFragment();
        changeFragment("category", priceFragment, "price");
    }

    /**
     * Gets price and duration from user input, switches to location fragment
     * Price is a nonnegative double
     * Duration is a nonnegative int for days, weeks, months, or years.
     * @param view
     */
    public void submitPrice(View view) {
        EditText editTextPrice = (EditText) findViewById(R.id.editTextPrice);
        NumberPicker timePicker = (NumberPicker) findViewById(R.id.timePicker);
        EditText editTextDuration = (EditText) findViewById(R.id.editTextDuration);
        String timeType = "";
        int timeNumber = 0;
        //if(editTextDuration.getText().toString().matches("") || editTextPrice.getText().toString().matches("")) {
        if(checkEditTextNonEmpty(editTextDuration) && checkEditTextNonEmpty(editTextPrice)) {
            try {
                timeNumber = Integer.parseInt(editTextDuration.getText().toString());
                timeType = timePicker.getDisplayedValues()[timePicker.getValue()];
                price = Double.parseDouble(editTextPrice.getText().toString());
                duration = "" + timeNumber + " " + timeType;
                // Replace price fragment with location fragment
                LocationFragment locationFragment = new LocationFragment();
                changeFragment("price", locationFragment, "location");
            } catch (Exception e) {
                Toast.makeText(mContext, "Please Check Your Inputs", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mContext, "Please Check Your Inputs: One or More Fields are Empty", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Gets location from user input, submits posting and returns to blank camera activity
     * @param view
     */
    public void submitLocation(View view) {
        Log.d(TAG, "inside of submitLocation, cameraAcitivity");
        // For now, just assume location is seattle
        city = "seattle";
        // All details gathered, may now post
        post();
    }

    /**
     * Posts the post details and post picture to firebase
     * Pictures will be in items/UUID
     * Posts will be posted in database as
     * City -> UUID -> Post [userUID, pictureURL, title, description, category, price, duration]
     */
    private void post() {
        String result = String.format("title=%s;category=%s;description=%s;price=%s;duration=%s,location=%s", title, category, description, price, duration, city);
        Log.d(TAG,"Attempting to post: " + result);
        StorageReference postRef = storageReference.child("items/" + UUID.randomUUID());
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = imageView.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = postRef.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUri = taskSnapshot.getDownloadUrl();
                addDatabasePost(downloadUri);
            }
        });
    }

    /**
     * Adds a new posting to database, including:
     * city(all lowercase), Picture Url, Title, category, description, price, duration, condition
     * @param downloadUri   The url of the image that accompanies this post.
     */
    private void addDatabasePost(Uri downloadUri) {
        String userUid = mAuth.getCurrentUser().getUid();
        Map<String, Object>  postValues = new HashMap<>();
        postValues.put("userUID", userUid);
        postValues.put("title", title);
        postValues.put("rate", "" + price + " per Day");
        postValues.put("imageURL", downloadUri.toString());
        //postValues.put("Duration", duration);
        postValues.put("description", description);
        postValues.put("condition", condition);
        postValues.put("category", category);
        DatabaseReference myRef = mReference.child("posts/" + city.toLowerCase());
        String key = myRef.push().getKey();           //this returns the unique key generated by firebase
        myRef.child(key).setValue(postValues);

        DatabaseReference userItemsRef = mReference.child("user_items");
        Map<String, Object> userItemsPostValues = new HashMap<>();
        userItemsPostValues.put("city", "seattle");
        userItemsPostValues.put("imageURL", downloadUri.toString());
        userItemsRef.child(userUid).child(key).setValue(userItemsPostValues);
        Toast.makeText(mContext, "Post Submitted!", Toast.LENGTH_SHORT).show();
        // Removes fragment, back to default cameraActivity
        android.support.v4.app.Fragment oldFragment = fragmentManager.findFragmentByTag("location");
        fragmentManager.beginTransaction().remove(oldFragment).commit();
        launchCancel(null);
    }
}
