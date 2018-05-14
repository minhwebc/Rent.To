package to.rent.rentto.Help;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import to.rent.rentto.R;

public class TipsForMessagingFragment extends android.support.v4.app.Fragment {
    private static final String TAG = "HowToPostFragment";
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tips_for_messaging, container, false);

        ImageView backArrow = (ImageView) view.findViewById(R.id.message_and_meetup_backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                manager.popBackStack();
            }
        });
        return view;
    }

}
