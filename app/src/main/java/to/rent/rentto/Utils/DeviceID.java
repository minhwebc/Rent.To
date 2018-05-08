package to.rent.rentto.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;

public class DeviceID extends AppCompatActivity {

    private TelephonyManager tm;
    private Context mContext;
    private String phoneNum;

    @SuppressLint("MissingPermission")
    public DeviceID(Context mContext) {
        this.mContext = mContext;
        this.tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        this.phoneNum = tm.getLine1Number();
    }

    public String getPhoneNumber() {
        String num = this.phoneNum;
        return num;
    }

}
