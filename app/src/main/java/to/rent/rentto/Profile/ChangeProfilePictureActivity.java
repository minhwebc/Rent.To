package to.rent.rentto.Profile;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import to.rent.rentto.Camera.ConfirmPhotoFragment;
import to.rent.rentto.R;

public class ChangeProfilePictureActivity extends AppCompatActivity{
    private static final String TAG = "ChangeProfileActivity";
    private Context mContext;
    android.support.v4.app.FragmentManager fragmentManager;  // handles fragment switching
    static final int REQUEST_IMAGE_CAPTURE = 1; // the request code number assigned to image capture
    static final int REQUEST_CAMERA_ROLL = 2;   // the request code number assigned to camera roll
    Uri imageUri;
    Bitmap uploadable; // the bitmap may be uploaded to firebase immediately
    boolean gotPicture = false; // whether there is a picture ready for upload
    byte[] imageByteArray;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_profile_picture);
        mContext = ChangeProfilePictureActivity.this;
        AddProfilePhotoFragment addProfilePhotoFragment = new AddProfilePhotoFragment();
        fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.relLayout2, addProfilePhotoFragment, "addProfilePhoto").commit();
        // Gets Permissions
        if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

    /**
     * Launches camera and puts image to imageview for confirmation
     * Alerts the user if the app needs camera permission
     * @param view
     */
    public void profilePictureCamera(View view) {
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
     * Launches camera roll and puts image to imageview for confirmation
     * @param view
     */
    public void profilePictureRoll(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CAMERA_ROLL);
    }

    /**
     * Sets the imageview on the confirmation screen to the chosen image
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageUri = null;
        imageByteArray = null;
        uploadable = null;
        gotPicture = false;
        if(resultCode == RESULT_OK) {
            gotPicture = true;
            if(requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                Bitmap photo = (Bitmap) extras.get("data");
                uploadable = photo;
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                imageByteArray = stream.toByteArray();
            } else if(requestCode == REQUEST_CAMERA_ROLL) {
                imageUri = data.getData();}
        }
    }

    /**
     * If a picture was taken, changes fragment to confirm the profile photo for change
     */
    @Override
    protected void onResume() {
        super.onResume();
        if(gotPicture) {
            ConfirmPhotoFragment confirmPhotoFragment = new ConfirmPhotoFragment();
            fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
            Bundle args = new Bundle();
            args.putBoolean("CameraRoll", imageUri == null);
            if(imageUri != null) {
                args.putString("Image", imageUri.toString());
            } else {
                args.putByteArray("ImageByteArray", imageByteArray);
            }
            confirmPhotoFragment.setArguments(args);
            fragmentManager.beginTransaction().replace(R.id.relLayout2, confirmPhotoFragment, "confirm").addToBackStack(null).commit();
        }
    }

    /**
     * Uploads image shown in imageview to firebase and updates firebase->user_account_settings->UUID->profile_photo
     * @param view
     */
    public void launchConfirm(View view) {
        if(gotPicture) {
            UploadTask uploadTask;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            StorageReference postRef = storageReference.child("profilePictures/" + UUID.randomUUID());
            if (imageUri != null) {
                uploadTask = postRef.putFile(imageUri);
            } else {
                Bitmap bitmap = uploadable;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();
                uploadTask = postRef.putBytes(data);
            }
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUri = taskSnapshot.getDownloadUrl();
                    changeProfilePictureURL(downloadUri);
                }
            });
            this.finish();
        }
    }

    /**
     * Changes the profile picture url on firebase to the given URI
     * @param uri
     */
    private void changeProfilePictureURL(Uri uri) {
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userUid = mAuth.getCurrentUser().getUid();
        mReference.child("user_account_settings").child(userUid).child("profile_photo").setValue(uri.toString());
    }
}
