package to.rent.rentto.Profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.Rating;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import io.github.douglasjunior.androidSimpleTooltip.ArrowDrawable;
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip;
import to.rent.rentto.Models.Item;
import to.rent.rentto.Models.User;
import to.rent.rentto.Models.UserAccountSettings;
import to.rent.rentto.Models.UserSettings;
import to.rent.rentto.R;
import to.rent.rentto.Utils.BottomNavigationViewHelper;
import to.rent.rentto.Utils.FirebaseMethods;
import to.rent.rentto.Utils.UniversalImageLoader;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by allencho on 2/27/18.
 */

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";

    private static final int ACTIVITY_NUM = 3;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    //From ItemListActivity
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private ArrayList<String> iDs = new ArrayList<>();
    private ArrayList<String> zips = new ArrayList<>();
    private ArrayList<Boolean> rented = new ArrayList<>();
    private ProfileRecyclerViewAdapter staggeredRecyclerViewAdapter;
    private static final int NUM_COLUMNS = 3;
    private static final int CHANGE_PROFILE_PIC = 1;
    private TextView mPosts, mDisplayName, mUsername, mWebsite, mDescription;
    private ProgressBar mProgressBar;
    private CircleImageView mProfilePhoto;
    private GridView gridView;
    private RatingBar mRatingBar;
    private Toolbar toolbar;
    private ImageView profileMenu;
    private BottomNavigationViewEx bottomNavigationView;
    private Context mContext;
    private SwipeRefreshLayout swipeLayout;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mDisplayName = (TextView) view.findViewById(R.id.display_name);
        mUsername = (TextView) view.findViewById(R.id.username);
        mWebsite = (TextView) view.findViewById(R.id.website);
        mDescription = (TextView) view.findViewById(R.id.description);
        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_photo);
        //mPosts = (TextView) view.findViewById(R.id.tvPosts);
        //mProgressBar = (ProgressBar) view.findViewById(R.id.profileProgressBar);
        mRatingBar = (RatingBar) view.findViewById(R.id.rtbDvcMgmt);
        toolbar = (Toolbar) view.findViewById(R.id.profileToolBar);
        profileMenu = (ImageView) view.findViewById(R.id.profileMenu);
        bottomNavigationView = (BottomNavigationViewEx) view.findViewById(R.id.bottomNavViewBar);
        mFirebaseMethods = new FirebaseMethods(getActivity());

        mContext = getActivity();
        Log.d(TAG, "onCreateView: stared.");


        setupProfilePhotoClick();
        setupBottomNavigationView();
        setupToolbar();
        setupFirebaseAuth();

        //RecyclerView
        final int width = getScreenSizeX();


        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override public void run() {
                        initRecyclerView(width, view);
                        initImageBitMaps();
                        swipeLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });

        initImageLoader();
        initRecyclerView(width, view);
        initImageBitMaps();

        return view;
    }

    private void setupProfilePhotoClick() {
        mProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: switching to ChangeProfilePictureActivity.");
                Intent intent = new Intent(getActivity(), ChangeProfilePictureActivity.class);
                startActivityForResult(intent, CHANGE_PROFILE_PIC);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            getActivity().recreate();
        }
    }

    private void initImageBitMaps(){
        Log.d(TAG, "setupGridView: Setting up image grid.");

        final ArrayList<String> photos = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_items))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                iDs.clear();
                zips.clear();
                rented.clear();
                mImageUrls.clear();
                for ( DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){

                    String keyID = singleSnapshot.getKey(); //photoIDs
                    iDs.add(keyID);
                    Item mItem = singleSnapshot.getValue(Item.class);
                    zips.add(mItem.zip);

                    if(mItem.sold)
                        rented.add(true);
                    else
                        rented.add(false);
                    String photo_path = mItem.imageURL;
                    mImageUrls.add(photo_path);
                    Log.d(TAG, mItem.sold + " something");
                }
                //tooltip
                if(staggeredRecyclerViewAdapter.getItemCount() > 0) {
                    SharedPreferences prefs = mContext.getSharedPreferences("Rent.toPrefs", MODE_PRIVATE);
                    if(!prefs.getBoolean("hasSeenProfileItemTooltip", false)) {
                        SharedPreferences.Editor editor = mContext.getSharedPreferences("Rent.toPrefs", MODE_PRIVATE).edit();
                        editor.putBoolean("hasSeenProfileItemTooltip", true);
                        editor.apply();
                        new SimpleTooltip.Builder(mContext)
                                .anchorView(mProfilePhoto)
                                .gravity(Gravity.BOTTOM)
                                .arrowDirection(ArrowDrawable.BOTTOM)
                                .text("Long press your items to\n mark as rented or deleted")
                                .build()
                                .show();
                    }
                }
                staggeredRecyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });
    }

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }


    private void initRecyclerView(int width, View view) {
        Log.d(TAG, "initRecyclerView staggered view");
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.profilerecyclerView);
        staggeredRecyclerViewAdapter =
                new ProfileRecyclerViewAdapter(this.mContext, iDs, mImageUrls, zips, rented);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(NUM_COLUMNS, LinearLayoutManager.VERTICAL);
        if(recyclerView == null) {
            Log.d(TAG, "RecyclerView is null");
        }
        if(staggeredGridLayoutManager == null) {
            Log.d(TAG, "staggeredgridlayout manger is null");
        }
        if(recyclerView != null && staggeredGridLayoutManager != null){
                recyclerView.setLayoutManager(staggeredGridLayoutManager);
                recyclerView.setAdapter(staggeredRecyclerViewAdapter);
        }
    }

    private int getScreenSizeX () {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        return width;
    }

    private void setProfileWidgets(UserSettings userSettings) {
        Log.d(TAG, "setProfileWidgets: settings widgets with data retrieving from firebase database: " + userSettings.toString());
        Log.d(TAG, "setProfileWidgets: settings widgets with data retrieving from firebase database: " + userSettings.getSettings().getUsername());
        UserAccountSettings settings = userSettings.getSettings();
        User currentUser = userSettings.getUser();
        double rating = currentUser.getRating();

        //UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");
        if(settings.getProfile_photo() == null || settings.getProfile_photo().length() < 1) {
            mProfilePhoto.setImageResource(R.drawable.profile_default_pic);
        } else {
            Glide.with(getActivity())
                    .load(settings.getProfile_photo())
                    .into(mProfilePhoto);
        }
        mDisplayName.setText(settings.getUsername());
        mUsername.setText(settings.getUsername());
        mRatingBar.setRating((float) rating);
        String website = settings.getWebsite();
        if(website == null || website.length() < 1) {
            website = "No Website Listed";
        }
        mWebsite.setText(website);
        Log.d(TAG, "displayname is " + settings.getUsername() + " username is " + settings.getUsername() + " rating is " + (float) rating);
    }

    private void setupToolbar(){

        try {
            ((ProfileActivity) getActivity()).setSupportActionBar(toolbar);

            profileMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: navigating to account settings.");
                    Intent intent = new Intent(mContext, AccountSettingsActivity.class);
                    startActivity(intent);
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "Cannot get support action bar");
            e.printStackTrace();
        }
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationView);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    /*
    ------------------------------------ Firebase ---------------------------------------------
     */

    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //retrieve user information from the database
                try {
                    setProfileWidgets(mFirebaseMethods.getUserAccountSettings(dataSnapshot));
                } catch(Exception e) {

                }
                //retrieve images for the user in question

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
