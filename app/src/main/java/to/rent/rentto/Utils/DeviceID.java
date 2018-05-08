package to.rent.rentto.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;

public class DeviceID extends AppCompatActivity {

    private TelephonyManager tm;
    private Context mContext;
    private String phoneNum;
    private String aid;

    @SuppressLint("MissingPermission")
    public DeviceID(Context mContext) {
        this.mContext = mContext;
        this.tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        this.phoneNum = tm.getLine1Number();
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
