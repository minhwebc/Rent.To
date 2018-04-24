package to.rent.rentto.Utils;

import android.content.Context;

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


}
