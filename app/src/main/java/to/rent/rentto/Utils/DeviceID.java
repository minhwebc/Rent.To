package to.rent.rentto.Utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class DeviceID extends AppCompatActivity {
    private String TAG = "DeviceID";
    private TelephonyManager tm;
    private Context mContext;
    private String phoneNum;
    private String aid;

    @SuppressLint("MissingPermission")
    public DeviceID(Context mContext) {
        this.mContext = mContext;
        this.tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if(mContext.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Could not get phone number, permission not granted. Set to '0'");
            this.phoneNum = "0";
        } else {
            if(tm != null) {
                this.phoneNum = tm.getLine1Number();
            }
        }
        if(this.phoneNum == null || this.phoneNum.length() == 0) {
            this.phoneNum = "0";
        }
        this.aid = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public String getPhoneNumber() {
        String num = this.phoneNum;
        return num;
    }

    public String getDeviceID() {
        String aid = this.aid;
        return aid;
    }

}
