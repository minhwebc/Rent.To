package to.rent.rentto.Camera;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import to.rent.rentto.R;

public class ConfirmPhotoFragment extends android.support.v4.app.Fragment {
    private static final String TAG = "ConfirmPhotoFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confirm_photo, container, false);
        Log.d(TAG, "inside of ConfirmPhoto.java onCreateView");
        initiateImageView(view);
        return view;
    }

    /**
     * Sets imageView image to the chosen image
     * @param view
     */
    private void initiateImageView(View view) {
        ImageView imageView = view.findViewById(R.id.confirmImageView);
        boolean cameraRoll = getArguments().getBoolean("CameraRoll");
        if(cameraRoll) {
            //[] imageByteArray = getArguments().getByteArray("Bitmap");
            //Bitmap bmp = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
            Bitmap bmp = getArguments().getParcelable("Bitmap");
            imageView.setImageBitmap(bmp);
        } else {
            String imagePath = getArguments().getString("Image");
            Uri uri = Uri.parse(imagePath);
            imageView.setImageURI(uri);
        }
    }
}