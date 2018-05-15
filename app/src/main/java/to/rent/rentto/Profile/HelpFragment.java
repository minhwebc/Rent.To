package to.rent.rentto.Profile;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import to.rent.rentto.Help.AboutRatingsFragment;
import to.rent.rentto.Help.FindMessagesFragment;
import to.rent.rentto.Help.HowToPostFragment;
import to.rent.rentto.Help.PostingRulesFragment;
import to.rent.rentto.Help.ProhibitedItemsFragment;
import to.rent.rentto.Help.TipsForMessagingFragment;
import to.rent.rentto.R;

public class HelpFragment extends Fragment {
    private static final String TAG = "HelpFragment";
    private TextView mHowToPost, mPostingRules, mFindMessages, mTipsForMessaging, mProtectingYourPrivacy, mHowContactInfoHelps, mProhibitedItems, mAboutRatings;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help, container, false);
        mHowToPost = (TextView) view.findViewById((R.id.how_to_post));
        mHowToPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new HelpFragment();
                FragmentManager manager = getFragmentManager();
                manager.beginTransaction().replace(R.id.helpMain, new HowToPostFragment()).addToBackStack(null).commit();
            }
        });
        mPostingRules = (TextView) view.findViewById((R.id.posting_rules));
        mPostingRules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new HelpFragment();
                FragmentManager manager = getFragmentManager();
                manager.beginTransaction().replace(R.id.helpMain, new PostingRulesFragment()).addToBackStack(null).commit();
            }
        });
        mFindMessages =(TextView) view.findViewById((R.id.find_messages));
        mFindMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new HelpFragment();
                FragmentManager manager = getFragmentManager();
                manager.beginTransaction().replace(R.id.helpMain, new FindMessagesFragment()).addToBackStack(null).commit();
            }
        });
        mTipsForMessaging = (TextView) view.findViewById((R.id.tips_for_messaging));
        mTipsForMessaging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new HelpFragment();
                FragmentManager manager = getFragmentManager();
                manager.beginTransaction().replace(R.id.helpMain, new TipsForMessagingFragment()).addToBackStack(null).commit();
            }
        });
        mProhibitedItems = (TextView) view.findViewById((R.id.prohibited_items));
        mProhibitedItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new HelpFragment();
                FragmentManager manager = getFragmentManager();
                manager.beginTransaction().replace(R.id.helpMain, new ProhibitedItemsFragment()).addToBackStack(null).commit();
            }
        });
        mAboutRatings = (TextView) view.findViewById((R.id.about_ratings));
        mAboutRatings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new HelpFragment();
                FragmentManager manager = getFragmentManager();
                manager.beginTransaction().replace(R.id.helpMain, new AboutRatingsFragment()).addToBackStack(null).commit();
            }
        });

        ImageView backArrow = (ImageView) view.findViewById(R.id.helpPage_backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back to ProfileActivity");
                getActivity().finish();
            }
        });




        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }





}
