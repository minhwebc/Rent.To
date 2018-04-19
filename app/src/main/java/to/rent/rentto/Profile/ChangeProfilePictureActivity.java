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
import to.rent.rentto.R;

public class ChangeProfilePictureActivity extends AppCompatActivity{
    private static final String TAG = "ChangeProfileActivity";
    private Context mContext;
    static final int REQUEST_IMAGE_CAPTURE = 1; // the request code number assigned to image capture
    static final int REQUEST_CAMERA_ROLL = 2;   // the request code number assigned to camera roll
    Uri imageUri;
    Bitmap uploadable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_profile_picture);
        mContext = ChangeProfilePictureActivity.this;
    }

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

    public void profilePictureRoll(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CAMERA_ROLL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if(requestCode == REQUEST_CAMERA_ROLL) {
                imageUri = data.getData();
            }
            if(requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                Bitmap photo = (Bitmap) extras.get("data");
                uploadable = photo;
            }
        }
    }

    public void savePicture(View view) {
        UploadTask uploadTask;
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference postRef = storageReference.child("profilePictures/" + UUID.randomUUID());
        if(imageUri != null) {
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

    private void changeProfilePictureURL(Uri uri) {
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userUid = mAuth.getCurrentUser().getUid();
        mReference.child("user_account_settings").child(userUid).child("profile_photo").setValue(uri.toString());
    }
}
