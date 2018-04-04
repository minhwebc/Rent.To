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
import android.widget.Spinner;
import android.widget.TextView;
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
    private static final int ACTIVITY_NUM = 1; // the second case in bottomnav (0 index)
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
    private static final String TAG = "CameraActivity";
    private Context mContext = CameraActivity.this;
    static final int REQUEST_IMAGE_CAPTURE = 1; // the request code number assigned to image capture
    static final int MAX_PRICE = 10000;
    static final int REQUEST_CAMERA_ROLL = 2;   // the request code number assigned to camera roll
    android.support.v4.app.FragmentManager fragmentManager;  // handles fragment switching
    Button confirmButton;
    Button cameraRollButton;
    Button cameraButton;
    Button cancelButton;
    Uri imageUri;  // the uri, for the image (for uploading)
    ImageView imageView;  // Where the selected image will be displayed
    String title; // the title, for post
    String description; // the description, for post
    String category; // the category, for post
    String price; // the price, for post
    String timeType; // the type of time: hour, day, week, month, year
    String condition;
    String city; // the zipcode, for post
    Bitmap uploadable;

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
        transaction.replace(R.id.relLayout2, addTitleFragment, "title").addToBackStack(null).commit();
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
        imageView.setVisibility(View.VISIBLE);
        imageUri = null;
        uploadable = null;
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
            uploadable = photo;
        } else if(requestCode == REQUEST_CAMERA_ROLL && resultCode == RESULT_OK) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
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
        fragmentManager.beginTransaction().replace(R.id.relLayout2, fragment, newTag).addToBackStack(null).commit();
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
     * Gets category, condition, and description from user input, switches to price fragment
     * Description may be empty
     * @param view
     */
    public void submitCategory(View view) {
        Spinner categorySpinner = (Spinner) findViewById(R.id.spinnerCategory);
        category = categorySpinner.getSelectedItem().toString();
        EditText editTextDescription = (EditText) findViewById(R.id.editTextDescription);
        description = editTextDescription.getText().toString();
        TextView conditionTextView = (TextView) findViewById(R.id.conditionTextView);
        condition = conditionTextView.getText().toString();
        // Replace category fragment with price fragment
        PriceFragment priceFragment = new PriceFragment();
        changeFragment("category", priceFragment, "price");
    }

    /**
     * Gets price from user input, switches to location fragment
     * Price is nonnegative, and must not exceed 10000
     * @param view
     */
    public void submitPrice(View view) {
        EditText editTextPrice = (EditText) findViewById(R.id.editTextPrice);
        NumberPicker timePicker = (NumberPicker) findViewById(R.id.timePicker);
        if(checkEditTextNonEmpty(editTextPrice)) {
            try {
                timeType = timePicker.getDisplayedValues()[timePicker.getValue()];
                double rate = Math.round(Double.parseDouble(editTextPrice.getText().toString()) * 100.00)/ 100.00;
                price = String.format("%.2f", rate);

                if(rate > MAX_PRICE) {
                    Toast.makeText(mContext, "That price is too high (Max is " + MAX_PRICE + ")", Toast.LENGTH_SHORT).show();
                } else {
                    // Replace price fragment with location fragment
                    LocationFragment locationFragment = new LocationFragment();
                    changeFragment("price", locationFragment, "location");
                }
            } catch (Exception e) {
                Toast.makeText(mContext, "Please Check Your Inputs", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mContext, "Please Check Your Inputs. Something is Empty", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sets location to current location
     * @param view
     */
    public void getLocation(View view) {
        EditText editTextLocation = (EditText) findViewById(R.id.editTextLocation);
        String cityName = getCityName();
        editTextLocation.setText(cityName);
    }

    /**
     * Gets the zip code of the device's current location
     * @return The zip code
     */
    private String getCityName() {
        // For now, just returns Seattle
        return "Seattle";
    }

    /**
     * @return Whether the city is valid
     */
    private boolean validateCity(String cityString) {
        // For now, assume all city names are valid
        if(true) {
            city = cityString;
            Log.d(TAG, "Inside validateCity " + cityString);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets location from user input, submits posting and returns to blank camera activity
     * @param view
     */
    public void submitLocation(View view) {
        Log.d(TAG, "inside of submitLocation, cameraAcitivity");
        EditText editTextLocation = (EditText) findViewById(R.id.editTextLocation);
        if(checkEditTextNonEmpty(editTextLocation)) {
            // For now, just assume location is seattle
            String editTextCity = editTextLocation.getText().toString();
            Log.d(TAG, "The city name is " + editTextCity);
            if(validateCity(editTextCity)) {
                // All details gathered, may now post
                Button submitPostButton = (Button) findViewById(R.id.button_send);
                submitPostButton.setEnabled(false); // prevent spam clicking
                post();
            } else {
                Toast.makeText(mContext, "Not a valid city", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mContext, "You must choose a location", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Posts the post details and post picture to firebase
     * Pictures will be in items/[UUID]
     */
    private void post() {
        String result = String.format("title=%s;category=%s;description=%s;price=%s,location=%s", title, category, description, price, city);
        Log.d(TAG,"Attempting to post: " + result);
        StorageReference postRef = storageReference.child("items/" + UUID.randomUUID());
        UploadTask uploadTask;
        if(uploadable != null) { // photo was from camera
            Bitmap bitmap = uploadable;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            uploadTask = postRef.putBytes(data);
        } else { // photo was from camera roll
            uploadTask = postRef.putFile(imageUri);
        }
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUri = taskSnapshot.getDownloadUrl();
                addDatabasePost(downloadUri);
            }
        });
    }

    /**
     * Adds a new posting to database,
     * posts -> cityName -> postUID[category, condition, description, imageURL, rate, title, userUID]
     * user_items-> userUID-> postUID[city, imageURL]
     * @param downloadUri   The url of the image that accompanies this post.
     */
    private void addDatabasePost(Uri downloadUri) {
        String userUid = mAuth.getCurrentUser().getUid();
        Map<String, Object>  postValues = new HashMap<>();
        postValues.put("userUID", userUid);
        postValues.put("title", title);
        postValues.put("rate", "" + price + " per " + timeType);
        postValues.put("imageURL", downloadUri.toString());
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
        imageView.setVisibility(View.VISIBLE);
    }
}
