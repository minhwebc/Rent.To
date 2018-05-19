package to.rent.rentto.Profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
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

import to.rent.rentto.Listing.ListingActivity;
import to.rent.rentto.R;

/**
 * Created by Sora on 2/15/2018.
 */

public class ProfileRecyclerViewAdapter extends RecyclerView.Adapter<ProfileRecyclerViewAdapter.ViewHolder>{

    private static final String TAG = "StaggeredRecyclerViewAd";
    private static final int REQUEST_CATEGORY_CODE = 1000;
    private static final int LOCATION_PERMISSION_REQUEST = 1;

    private ArrayList<String> mIDs = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private ArrayList<String> zips = new ArrayList<>();
    private ArrayList<Boolean> rented = new ArrayList<>();
    private String[] mData = new String[0];
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context mContext;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private boolean longClickable;


    public ProfileRecyclerViewAdapter(Context context, ArrayList<String> ids, ArrayList<String> imageUrls, ArrayList<String> zips, ArrayList<Boolean> rented) {
        this(context, ids, imageUrls, zips, rented, true);
    }

    public ProfileRecyclerViewAdapter(Context context, ArrayList<String> ids, ArrayList<String> imageUrls, ArrayList<String> zips, ArrayList<Boolean> rented, boolean longClickable){
        Log.d(TAG, "constructor: called.");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        this.mInflater = LayoutInflater.from(context);
        this.mContext = context;
        Log.d(TAG, mIDs.size()+"");
        mIDs = ids;
        this.zips = zips;
        this.rented = rented;
        mImageUrls = imageUrls;
        this.longClickable = longClickable;
    }

    @Override
    public ProfileRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup par, int viewType){
        View view = mInflater.inflate(R.layout.recyclerview_item, par, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        Log.d(TAG, "onBindViewHolder: called." + mIDs.size());
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.ic_launcher_background);
        if(rented.get(position)) {
            holder.soldInfo.setVisibility(View.VISIBLE);
            holder.soldInfo.setText("RENTED");
            holder.imageView.setColorFilter(ContextCompat.getColor(mContext, R.color.black_50));
        }else{
            holder.soldInfo.setVisibility(View.GONE);
        }
        Glide.with(mContext)
                .load(mImageUrls.get(position))
                .apply(requestOptions)
                .into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return mIDs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, RatingDialogListener {
        ImageView imageView;
        TextView soldInfo;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image);
            soldInfo = (TextView) itemView.findViewById(R.id.ratedInformationImage);
            itemView.setOnClickListener(this);
            if(longClickable) {
                itemView.setOnLongClickListener(this);
            }
        }

        @Override
        public void onClick(View view) {
//            Toast.makeText(mContext, mIDs.get(getAdapterPosition()), Toast.LENGTH_SHORT).show();
            Intent intent;
            new Intent(mContext, ProfileListingActivity.class);
            if(!longClickable) {
                intent = new Intent(mContext, ListingActivity.class);
            } else {
                intent = new Intent(mContext, ProfileListingActivity.class);
            }
            intent.putExtra("ITEM_ID", mIDs.get(getAdapterPosition()));
            intent.putExtra("CITY", zips.get(getAdapterPosition()));
            intent.putExtra("RENTED", rented.get(getAdapterPosition()));
            mContext.startActivity(intent);

        }

        @Override
        public boolean onLongClick(View view) {
            final CharSequence colors[] = new CharSequence[] {"Mark as Rented", "Delete item"};
            final ArrayList<String> usersIDArray = new ArrayList<>();
            final ArrayList<String> usersNameArray = new ArrayList<>();

            String location = zips.get(getAdapterPosition());
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
            usersIDArray.add("BLANK");
            usersNameArray.add("Someone outside of Rent To");
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

            builder.setTitle("Action");
            builder.setItems(colors, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if(which == 0) {
                        Log.d(TAG, "this is the message " + rented.get(getAdapterPosition()));
                        if(!rented.get(getAdapterPosition())) {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setTitle("Who do you sold it to:");
                            //String usersIDStringArray[] = usersIDArray.toArray(new String[usersIDArray.size()]);
                            String usersNameStringArray[] = usersNameArray.toArray(new String[usersNameArray.size()]);
                            builder.setItems(usersNameStringArray, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final String userStringSold = usersIDArray.get(which);
                                    if(userStringSold.equals("BLANK")){
                                        myRef.child("posts").child(zips.get(getAdapterPosition())).child(mIDs.get(getAdapterPosition())).child("sold").setValue(true, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                myRef.child("user_items").child(mAuth.getCurrentUser().getUid()).child(mIDs.get(getAdapterPosition())).child("sold").setValue(true, new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                        Toast.makeText(mContext, "Item has been marked rented", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        });
                                    } else {
                                        myRef.child("users").child(userStringSold).child("users_to_be_rated").push().setValue(mAuth.getCurrentUser().getUid(), new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                if (databaseError == null) {
                                                    ((ProfileActivity) mContext).setUserID(userStringSold);
                                                    ((ProfileActivity) mContext).setItem(mIDs.get(getAdapterPosition()), zips.get(getAdapterPosition()), "");
                                                    showDialog();
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                            builder.show();
                        }else{
                            Toast.makeText(mContext, "Item has already been marked rented", Toast.LENGTH_SHORT).show();
                        }
                    } else if(which == 1){
                        Log.d("ViewHolder", "delete item here");
                        Log.d("ViewHolder", "delete item");
                        Log.d("ViewHolder", zips.get(getAdapterPosition()));
                        Log.d("ViewHolder", mIDs.get(getAdapterPosition()));
                        AlertDialog.Builder builder;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            builder = new AlertDialog.Builder(mContext, android.R.style.Theme_Material_Dialog);
                        } else {
                            builder = new AlertDialog.Builder(mContext);
                        }
                        builder.setTitle("Delete post")
                                .setMessage("Are you sure you want to delete this entry?")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        myRef.child("posts").child(zips.get(getAdapterPosition())).child(mIDs.get(getAdapterPosition())).child("offer_messages").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                                                    Log.d(TAG, ds.getKey());
                                                    myRef.child("messages").child(ds.getValue(String.class)).setValue(null, new DatabaseReference.CompletionListener() {
                                                        @Override
                                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                            Toast.makeText(mContext, "Message deleted", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                                myRef.child("posts").child(zips.get(getAdapterPosition())).child(mIDs.get(getAdapterPosition())).setValue(null, new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                        myRef.child("user_items").child(mAuth.getCurrentUser().getUid()).child(mIDs.get(getAdapterPosition())).setValue(null, new DatabaseReference.CompletionListener() {
                                                            @Override
                                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                Toast.makeText(mContext, "Item deleted", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                });

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // do nothing
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
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
                    .setDescription("Please select some stars and give a reminder below")
                    .setStarColor(R.color.colorAccent)
                    .setNoteDescriptionTextColor(R.color.colorPrimary)
                    .setTitleTextColor(R.color.black)
                    .setDescriptionTextColor(R.color.colorPrimary)
                    .setHint("Please write something you want to be reminded here about the item. We will send you both a message to remind")
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
