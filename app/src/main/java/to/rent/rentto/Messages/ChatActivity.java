package to.rent.rentto.Messages;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

import to.rent.rentto.R;

public class ChatActivity extends AppCompatActivity {
    private static String TAG = "ChatActivity";
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        editText = (EditText) findViewById(R.id.editText);

    }
}
