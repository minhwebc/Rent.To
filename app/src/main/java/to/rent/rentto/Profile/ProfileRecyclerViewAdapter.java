package to.rent.rentto.Profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.util.ArrayList;
import java.util.Arrays;

import to.rent.rentto.R;
import to.rent.rentto.Utils.ShareMethods;

/**
 * Created by Sora on 2/15/2018.
 */

public class ProfileRecyclerViewAdapter extends RecyclerView.Adapter<ProfileRecyclerViewAdapter.ViewHolder>{

    private static final String TAG = "StaggeredRecyclerViewAd";

    private ArrayList<String> mIDs = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private String[] mData = new String[0];
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context mContext;
    private int width;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;


    public ProfileRecyclerViewAdapter(Context context, ArrayList<String> ids, ArrayList<String> imageUrls, int width){
        Log.d(TAG, "constructor: called.");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        this.width = width;
        this.mInflater = LayoutInflater.from(context);
        this.mContext = context;
        Log.d(TAG, mIDs.size()+"");
        mIDs = ids;
        mImageUrls = imageUrls;
    }

    //To-do here find the current city
    private String findCurrentCity(){
        return "seattle";
    };

    @Override
    public ProfileRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup par, int viewType){
        View view = mInflater.inflate(R.layout.recyclerview_item, par, false);
        return new ViewHolder(view, this.width);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        Log.d(TAG, "onBindViewHolder: called." + mIDs.size());
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.ic_launcher_background);

        Glide.with(mContext)
                .load(mImageUrls.get(position))
                .apply(requestOptions)
                .into(holder.imageView);

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked on: " + mIDs.get(position));
                Toast.makeText(mContext, mIDs.get(position), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mContext, ProfileListingActivity.class);
                intent.putExtra("ITEM_ID", mIDs.get(position));
                intent.putExtra("CITY", findCurrentCity());
                mContext.startActivity(intent);
            }
        });

        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            /*
            @Override
            public boolean onLongClick(View v) {

                return true;
            }*/
            @Override
            public boolean onLongClick(View view) {
                final CharSequence colors[] = new CharSequence[] {"Mark as sold", "Delete item"};
                final ArrayList<String> usersIDArray = new ArrayList<>();
                final ArrayList<String> usersNameArray = new ArrayList<>();

                String location = ShareMethods.getCurrentLocation();
                String itemID = mIDs.get(position);
                myRef.child("posts").child(location).child(itemID).child("user_offers").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            String userID = ds.getValue(String.class);
                            usersIDArray.add(userID);
                            myRef.child("users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                                        if(ds.getKey().equals("username")) {
                                            usersNameArray.add(ds.getValue(String.class));
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                builder.setTitle("Action");
                builder.setItems(colors, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0) {
                            Log.d("ViewHolder", "mark as rented item");
                            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setTitle("Who do you sold it to:");
                            String usersIDStringArray[] = usersIDArray.toArray(new String[usersIDArray.size()]);
                            String usersNameStringArray[] = usersNameArray.toArray(new String[usersNameArray.size()]);
                            builder.setItems(usersNameStringArray, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final String userStringSold = usersIDArray.get(which);
                                    myRef.child("users").child(userStringSold).child("users_to_be_rated").push().setValue(mAuth.getCurrentUser().getUid(),new DatabaseReference.CompletionListener(){
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            if(databaseError == null) {
                                                ((ProfileActivity) mContext).setUserID(userStringSold);
                                                showDialog();
                                            }
                                        }
                                    });
                                }
                            });
                            builder.show();
                        } else if(which == 1){
                            Log.d("ViewHolder", "delete item");
                        }
                    }
                });
                builder.show();
                return false;
            }

            private void showDialog() {
                new AppRatingDialog.Builder()
                        .setPositiveButtonText("Submit")
                        .setNegativeButtonText("Cancel")
                        .setNeutralButtonText("Later")
                        .setNoteDescriptions(Arrays.asList("Very Bad", "Not good", "Quite ok", "Very Good", "Excellent !!!"))
                        .setDefaultRating(2)
                        .setTitle("How would you rate this person")
                        .setDescription("Please select some stars and give your feedback")
                        .setStarColor(R.color.colorAccent)
                        .setNoteDescriptionTextColor(R.color.colorPrimary)
                        .setTitleTextColor(R.color.black)
                        .setDescriptionTextColor(R.color.colorPrimary)
                        .setHint("Please write your comment here ...")
                        .setHintTextColor(R.color.black_11)
                        .setCommentTextColor(R.color.black)
                        .setCommentBackgroundColor(R.color.white)
                        .create((ProfileActivity) mContext)
                        .show();
            }

        });

    }

    @Override
    public int getItemCount() {
        return mIDs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, RatingDialogListener {
        ImageView imageView;

        ViewHolder(View itemView, int width) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image);
            int newWidth = width / 3;
            if(imageView.getLayoutParams().width > newWidth) {
                int ratio = newWidth / imageView.getLayoutParams().width;
                imageView.getLayoutParams().width = newWidth;
                imageView.getLayoutParams().height = imageView.getLayoutParams().height * ratio;
            }
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) {
                mClickListener.onItemClick(view, getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View view) {
            final CharSequence colors[] = new CharSequence[] {"Mark as sold", "Delete item"};
            final ArrayList<String> usersIDArray = new ArrayList<>();
            final ArrayList<String> usersNameArray = new ArrayList<>();

            String location = ShareMethods.getCurrentLocation();
            String itemID = mIDs.get(getAdapterPosition());
            myRef.child("posts").child(location).child(itemID).child("user_offers").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        String userID = ds.getValue(String.class);
                        usersIDArray.add(userID);
                        myRef.child("users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for(DataSnapshot ds : dataSnapshot.getChildren()){
                                    if(ds.getKey().equals("username")) {
                                        usersNameArray.add(ds.getValue(String.class));
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

            builder.setTitle("Action");
            builder.setItems(colors, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(which == 0) {
                        Log.d("ViewHolder", "mark as rented item");
                        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("Who do you sold it to:");
                        String usersIDStringArray[] = usersIDArray.toArray(new String[usersIDArray.size()]);
                        String usersNameStringArray[] = usersNameArray.toArray(new String[usersNameArray.size()]);
                        builder.setItems(usersNameStringArray, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String userStringSold = usersIDArray.get(which);
                                myRef.child("users").child(userStringSold).child("users_to_be_rated").push().setValue(mAuth.getCurrentUser().getUid(),new DatabaseReference.CompletionListener(){
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if(databaseError == null) {
                                            ((ProfileActivity) mContext).setUserID(userStringSold);
                                            showDialog();
                                        }
                                    }
                                });
                            }
                        });
                        builder.show();
                    } else if(which == 1){
                        Log.d("ViewHolder", "delete item");
                    }
                }
            });
            builder.show();
            return false;
        }

        @Override
        public void onPositiveButtonClicked(int i, String s) {

        }

        @Override
        public void onNegativeButtonClicked() {

        }

        @Override
        public void onNeutralButtonClicked() {

        }

        private void showDialog() {
            new AppRatingDialog.Builder()
                    .setPositiveButtonText("Submit")
                    .setNegativeButtonText("Cancel")
                    .setNeutralButtonText("Later")
                    .setNoteDescriptions(Arrays.asList("Very Bad", "Not good", "Quite ok", "Very Good", "Excellent !!!"))
                    .setDefaultRating(2)
                    .setTitle("How would you rate this person")
                    .setDescription("Please select some stars and give your feedback")
                    .setStarColor(R.color.colorAccent)
                    .setNoteDescriptionTextColor(R.color.colorPrimary)
                    .setTitleTextColor(R.color.black)
                    .setDescriptionTextColor(R.color.colorPrimary)
                    .setHint("Please write your comment here ...")
                    .setHintTextColor(R.color.black_11)
                    .setCommentTextColor(R.color.black)
                    .setCommentBackgroundColor(R.color.white)
                    .create((ProfileActivity) mContext)
                    .show();
        }
    }


    // convenience method for getting data at click position
    String getItem(int id) {
        return mData[id];
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }


    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        //pass to go the a specific item listing activity
        void onItemClick(View view, int position);
    }


}
