package to.rent.rentto.Profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import to.rent.rentto.Models.Item;
import to.rent.rentto.Models.UserAccountSettings;
import to.rent.rentto.Models.UserSettings;
import to.rent.rentto.R;
import to.rent.rentto.Utils.BottomNavigationViewHelper;
import to.rent.rentto.Utils.FirebaseMethods;
import to.rent.rentto.Utils.UniversalImageLoader;

import static android.app.Activity.RESULT_OK;

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
    private Toolbar toolbar;
    private ImageView profileMenu;
    private BottomNavigationViewEx bottomNavigationView;
    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mDisplayName = (TextView) view.findViewById(R.id.display_name);
        mUsername = (TextView) view.findViewById(R.id.username);
        mWebsite = (TextView) view.findViewById(R.id.website);
        mDescription = (TextView) view.findViewById(R.id.description);
        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_photo);
        //mPosts = (TextView) view.findViewById(R.id.tvPosts);
        //mProgressBar = (ProgressBar) view.findViewById(R.id.profileProgressBar);
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
        int width = getScreenSizeX();
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
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.profileRecylerVieww);
        staggeredRecyclerViewAdapter =
                new ProfileRecyclerViewAdapter(this.mContext, iDs, mImageUrls, width, zips, rented);
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
        //UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");
        Glide.with(getActivity())
                .load(settings.getProfile_photo())
                .into(mProfilePhoto);
        mDisplayName.setText(settings.getUsername());
        mUsername.setText(settings.getUsername());
    }

    private void setupToolbar(){

        ((ProfileActivity)getActivity()).setSupportActionBar(toolbar);

        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to account settings.");
                Intent intent = new Intent(mContext, AccountSettingsActivity.class);
                startActivity(intent);
            }
        });
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
