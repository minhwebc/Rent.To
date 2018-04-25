package to.rent.rentto.Camera;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import to.rent.rentto.R;
import to.rent.rentto.Utils.BottomNavigationViewHelper;
import android.Manifest;

public class CameraActivity extends AppCompatActivity {
    private static final int ACTIVITY_NUM = 2; // the second case in bottomnav (0 index)
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
    private FusedLocationProviderClient mFusedLocationClient;
    private static final String TAG = "CameraActivity";
    private Context mContext = CameraActivity.this;
    static final int REQUEST_IMAGE_CAPTURE = 1; // the request code number assigned to image capture
    static final int MAX_PRICE = 10000;
    static final int REQUEST_CAMERA_ROLL = 2;   // the request code number assigned to camera roll
    android.support.v4.app.FragmentManager fragmentManager;  // handles fragment switching
    Uri imageUri;  // the uri, for the image (for uploading)
    byte[] imageByteArray;
    String title; // the title, for post
    String description; // the description, for post
    String category; // the category, for post
    String price; // the price, for post
    String timeType; // the type of time: hour, day, week, month, year
    String condition;
    String city; // the zipcode, for post
    String zip; // The zip code for the post
    Bitmap uploadable;
    boolean gotPicture;

    /**
     * Hooks up buttons from camera fragment, Asks for permissions for camera and location
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        AddPhotoFragment addPhotoFragment = new AddPhotoFragment();
        fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.relLayout2, addPhotoFragment, "photo").commit();
        setupBottomNavigationView();

        // Gets Permissions
        if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    /**
     * Launches the camera, and feeds result to imageView.
     * Requires Camera Permission
     * @param view
     */
    public void launchCamera(View view) {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 1. Instantiate an AlertDialog.Builder with its constructor
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

            // 2. Chain together various setter methods to set the dialog characteristics
            builder.setMessage("Please Allow Camera Access")
                    .setTitle("Rent.to Needs Permission");

            // 3. Get the AlertDialog from create()
            AlertDialog dialog = builder.create();

            dialog.show();
        } else { // App has camera permission
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
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
        changeFragment("photo", addTitleFragment, "title");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(gotPicture) {
            ConfirmPhotoFragment confirmPhotoFragment = new ConfirmPhotoFragment();
            Bundle args = new Bundle();
            args.putBoolean("CameraRoll", imageUri == null);
            if(imageUri != null) {
                args.putString("Image", imageUri.toString());
            } else {
                args.putByteArray("ImageByteArray", imageByteArray);
            }
            confirmPhotoFragment.setArguments(args);
            changeFragment("photo", confirmPhotoFragment, "confirm");
        }
    }

    /**
     * Gets image, and displays to image found from URI
     * @param requestCode   Either from roll or from camera
     * @param resultCode    Should be RESULT_OK
     * @param data          Will be a URI for image.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        gotPicture = false;
        imageUri = null;
        imageByteArray = null;
        uploadable = null;
        if(resultCode == RESULT_OK) {
            Log.d(TAG, "result code was ok");
            gotPicture = true;
        }
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //Get the photo
            Bundle extras = data.getExtras();
            Bitmap photo = (Bitmap) extras.get("data");
            uploadable = photo;
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
            imageByteArray = stream.toByteArray();
        } else if(requestCode == REQUEST_CAMERA_ROLL && resultCode == RESULT_OK) {
            imageUri = data.getData();
        }
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
                double rate = Math.round(Double.parseDouble(editTextPrice.getText().toString().substring(1)) * 100.00)/ 100.00;
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
     * When user presses "Get Current Location Button", Sets location to current location
     * Requires Location Permission
     * @param view
     */
    public void getLocation(View view) {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 1. Instantiate an AlertDialog.Builder with its constructor
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

            // 2. Chain together various setter methods to set the dialog characteristics
            builder.setMessage("Please Allow Location Access")
                    .setTitle("Rent.to Needs Permission");

            // 3. Get the AlertDialog from create()
            AlertDialog dialog = builder.create();

            dialog.show();
        } else { // App has Location permission
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location == null) {
                                Toast.makeText(mContext, "Rent.to cannot get your location", Toast.LENGTH_SHORT).show();
                                // Logic to handle location object
                            } else { // location is not null
                                double lat = location.getLatitude();
                                double lng = location.getLongitude();
                                Log.d(TAG, "Lat: " + lat + ", Lng: " + lng);
                                Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
                                List<Address> addresses;
                                try {
                                    addresses = geocoder.getFromLocation(lat, lng, 1);
                                    Address address = addresses.get(0);
                                    String zip = address.getPostalCode(); // gets zip code
                                    String locality = address.getLocality(); // Gets locality (the city name)
                                    Log.d(TAG, "zipcode is " + zip + " locality is: " + locality);
                                    setZip(zip);
                                    setLocality(locality);
                                    EditText editTextLocation = (EditText) findViewById(R.id.editTextLocation);
                                    editTextLocation.setText(zip);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(mContext, "Rent.to cannot get your location", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
            mFusedLocationClient.getLastLocation();
        }
    }

    /**
     * Sets the zip code for the post
     * @param postalCode
     */
    private void setZip(String postalCode) {
        zip = postalCode;
    }

    /**
     * Sets the locality (city) for the post
     * @param locality
     */
    private void setLocality(String locality) {
        city = locality;
    }

    /**
     * Validates the zipcode
     * @return Whether the zipcode is valid
     */
    private boolean validateZip(String zipCode) {
        return zipCode.matches("^[0-9]{5}(-[0-9]{4})?$");
    }

    /**
     * Gets location from user input, submits posting and returns to blank camera activity
     * @param view
     */
    public void submitLocation(View view) {
        Log.d(TAG, "inside of submitLocation, cameraAcitivity");
        EditText editTextLocation = (EditText) findViewById(R.id.editTextLocation);
        if(checkEditTextNonEmpty(editTextLocation)) {
            zip = editTextLocation.getText().toString();
            Log.d(TAG, "The postal code is " + zip);
            if(validateZip(zip)) {
                // All details gathered, may now post
                Button submitPostButton = (Button) findViewById(R.id.button_send);
                submitPostButton.setEnabled(false); // prevent spam clicking
                post();
            } else {
                Toast.makeText(mContext, "Not a valid U.S. postal code", Toast.LENGTH_SHORT).show();
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
        String result = String.format("title=%s;category=%s;description=%s;price=%s,location=%s", title, category, description, price, zip);
        Log.d(TAG,"Attempting to post: " + result);
        StorageReference postRef = storageReference.child("items/" + UUID.randomUUID());
        UploadTask uploadTask;
        if(imageUri == null) { // photo was from camera
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
        DatabaseReference myRef = mReference.child("posts/" + zip);
        String key = myRef.push().getKey();           //this returns the unique key generated by firebase
        myRef.child(key).setValue(postValues);
        DatabaseReference userItemsRef = mReference.child("user_items");
        Map<String, Object> userItemsPostValues = new HashMap<>();
        userItemsPostValues.put("zip", zip);
        userItemsPostValues.put("city", city);
        userItemsPostValues.put("title", title);
        userItemsPostValues.put("rate", "" + price + " per " + timeType);
        userItemsPostValues.put("imageURL", downloadUri.toString());
        userItemsPostValues.put("description", description);
        userItemsPostValues.put("condition", condition);
        userItemsPostValues.put("category", category);
        userItemsRef.child(userUid).child(key).setValue(userItemsPostValues);
        Toast.makeText(mContext, "Post Submitted!", Toast.LENGTH_SHORT).show();
        // Removes fragment, back to default cameraActivity
        finish();
    }
}
