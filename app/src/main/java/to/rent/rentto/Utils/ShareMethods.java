package to.rent.rentto.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ShareMethods {
    public static String getCurrentLocation(){
        return "seattle";
    }
    public Context mContext;
    private static final int REQUEST_CATEGORY_CODE = 1000;
    private static final int LOCATION_PERMISSION_REQUEST = 1;

    public ShareMethods(Context mContext) {
        this.mContext = mContext;
    }

    //To-do here find the current city
    public String findCurrentCity(){
        if(ContextCompat.checkSelfPermission(mContext,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                ActivityCompat.requestPermissions((Activity)mContext, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            } else {
                ActivityCompat.requestPermissions((Activity)mContext, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            }
        } else {
            LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            try {
                Toast.makeText(mContext, "Location found", Toast.LENGTH_SHORT).show();
                return getZipcode(location.getLatitude(), location.getLongitude());
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(mContext, "Location not found, using default zip", Toast.LENGTH_SHORT).show();
                return "98105";
            }
        }
        return "";
    };

    private String getZipcode(double lat, double lon){
        String location = "";

        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(lat, lon, 1);
            if(addresses.size() > 0){
                location = addresses.get(0).getPostalCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, "Location not found, using default zip", Toast.LENGTH_SHORT).show();
            return "98105";
        }
        return location;
    }


    private double distanceBetweenZip(String zipOne, String zipTwo){
        String locationOne = zipOne + ", " + "United States";
        String locationTwo = zipTwo + ", " + "United States";
        Geocoder geoCoder = new Geocoder(mContext, Locale.getDefault());
        try {
            List<Address> addresses = geoCoder.getFromLocationName(locationOne, 1);
            double lat = addresses.get(0).getLatitude();
            double lon = addresses.get(0).getLongitude();
            List<Address> addressesTwo = geoCoder.getFromLocationName(locationTwo, 1);
            double latTwo = addressesTwo.get(0).getLatitude();
            double lonTwo = addressesTwo.get(0).getLongitude();
            float[] res = {3};
            Location.distanceBetween(lat, lon, latTwo, lonTwo, res);
            // Convert meters to miles
            return (res[0]/1609);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}
