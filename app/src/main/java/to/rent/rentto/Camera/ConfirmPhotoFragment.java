package to.rent.rentto.Camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;

import to.rent.rentto.R;

public class ConfirmPhotoFragment extends android.support.v4.app.Fragment {
    private static final String TAG = "ConfirmPhotoFragment";
    String imagePath;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confirm_photo, container, false);
        Log.d(TAG, "inside of ConfirmPhoto.java onCreateView");
        imagePath = getArguments().getString("Image");
        Uri uri  = Uri.parse(imagePath);
        ImageView imageView = view.findViewById(R.id.confirmImageView);
        imageView.setImageURI(uri);
        return view;
    }
}
