package to.rent.rentto.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import to.rent.rentto.Camera.CameraActivity;
import to.rent.rentto.Listing.ItemsListActivity;
import to.rent.rentto.Messages.NotificationActivity;
import to.rent.rentto.Profile.ProfileActivity;
import to.rent.rentto.R;
import to.rent.rentto.Remind.RemindActivity;

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

    public static void disableNavigation(final Context context,BottomNavigationViewEx view) {
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                }
                return false;
            }
        });
    }

    public static void enableNavigation(final Context context,BottomNavigationViewEx view) {
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.ic_home:
                        Intent intent1 = new Intent(context, ItemsListActivity.class); //ACTIVITY_NUM = 0
                        intent1.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        context.startActivity(intent1);
                        Activity something1 = (Activity) context;
                        something1.overridePendingTransition(0,0); //0 for no animation
                        break;
                    case R.id.remind:
                        Intent intent3 = new Intent(context, RemindActivity.class); //ACTIVITY_NUM = 1
                        intent3.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        context.startActivity(intent3);
                        Activity something2 = (Activity) context;
                        something2.overridePendingTransition(0,0); //0 for no animation
                        break;
                    case R.id.ic_addImage:
                        Intent intent4 = new Intent(context, CameraActivity.class); //ACTIVITY_NUM = 1
                        intent4.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        context.startActivity(intent4);
                        Activity something3 = (Activity) context;
                        something3.overridePendingTransition(0,0); //0 for no animation
                        break;

                    case R.id.ic_profile:
                        Intent intent5 = new Intent(context, ProfileActivity.class); //ACTIVITY_NUM = 2
                        intent5.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        context.startActivity(intent5);
                        Activity something4 = (Activity) context;
                        something4.overridePendingTransition(0,0); //0 for no animation
                        break;

                    case R.id.ic_messages:
                        Intent intent6 = new Intent(context, NotificationActivity.class); // Activity_NUM = 3
                        intent6.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        context.startActivity(intent6);
                        Activity something5 = (Activity) context;
                        something5.overridePendingTransition(0,0); //0 for no animation
                        break;
                }
                return false;
            }
        });
    }
}
