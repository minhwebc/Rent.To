package to.rent.rentto.Utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import to.rent.rentto.Home.HomeActivity;
import to.rent.rentto.Profile.ProfileActivity;
import to.rent.rentto.R;
import to.rent.rentto.Share.ShareActivity;
import to.rent.rentto.Listing.ItemsListActivity;
import to.rent.rentto.Listing.ListingActivity;

import to.rent.rentto.Camera.CameraActivity;

/**
 * Created by iguest on 2/11/18.
 */

public class BottomNavigationViewHelper {
    private static final String TAG = "BottomNavHelper";

    public static void setupBottomNavigationView(BottomNavigationViewEx bottomNavigationViewEx) {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.setTextVisibility(false);
    }

    public static void enableNavigation(final Context context,BottomNavigationViewEx view) {
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
//                    case R.id.ic_home:
//                        Intent intent1 = new Intent(context, HomeActivity.class);
//                        context.startActivity(intent1);
//                        break;
//                    case R.id.ic_addImage:
//                        Intent intent2 = new Intent(context, ShareActivity.class);
//                        context.startActivity(intent2);
//                        break;
//                    case R.id.ic_profile:
//                        Intent intent3 = new Intent(context, ProfileActivity.class);
//                        context.startActivity(intent3);
//                        break;
//                }
                    case R.id.ic_home:
                        Intent intent1 = new Intent(context, HomeActivity.class); //ACTIVITY_NUM = 0
                        intent1.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        context.startActivity(intent1);
                        break;
                    case R.id.ic_listing:
                        Intent intent2 = new Intent(context, ListingActivity.class); //ACTIVITY_NUM = 1
                        intent2.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        context.startActivity(intent2);
                        break;

                    case R.id.ic_shopping:
                        Intent intent3 = new Intent(context, ItemsListActivity.class); //ACTIVITY_NUM = 2
                        intent3.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        context.startActivity(intent3);
                        break;
                    case R.id.ic_addImage:
                        Intent intent4 = new Intent(context, CameraActivity.class); //ACTIVITY_NUM = 3
                        intent4.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        context.startActivity(intent4);
                        break;

                    case R.id.ic_profile:
                        Intent intent5 = new Intent(context, ProfileActivity.class); //ACTIVITY_NUM = 4
                        intent5.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        context.startActivity(intent5);
                        break;
                }

                return false;
            }
        });
    }
}
