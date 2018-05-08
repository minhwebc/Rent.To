package to.rent.rentto.Listing;

import android.content.Intent;
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

import java.util.ArrayList;

import to.rent.rentto.Models.Item;
import to.rent.rentto.R;

/**
 * Created by Sora on 2/15/2018.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    private static final String TAG = "StaggeredRecyclerViewAd";

    private ArrayList<String> mIDs = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private String[] mData = new String[0];
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ItemsListActivity mContext;
    private String city;
    private ArrayList<String> zips = new ArrayList<>();
    private ArrayList<Item> mItems;

    public RecyclerViewAdapter(ItemsListActivity context, ArrayList<String> ids, ArrayList<String> imageUrls, String city, ArrayList<String> zipcodes, ArrayList<Item> mItems){
        Log.d(TAG, "constructor: called.");
        this.zips = zipcodes;
        this.city = city;
        this.mInflater = LayoutInflater.from(context);
        this.mContext = context;
        Log.d(TAG, mIDs.size()+"");
        mIDs = ids;
        mImageUrls = imageUrls;
        this.mItems = mItems;
    }

    //To-do here find the current city
    private String findCurrentCity(){
        return this.city;
    };

    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup par, int viewType){
        View view = mInflater.inflate(R.layout.recyclerview_item, par, false);
        return new ViewHolder(view);
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
        if(mItems.get(position).sold) {
            holder.soldInfo.setVisibility(View.VISIBLE);
            holder.soldInfo.setText("RENTED");
            holder.imageView.setColorFilter(ContextCompat.getColor(mContext, R.color.black_50));

        }else{
            holder.soldInfo.setVisibility(View.GONE);
        }
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked on: " + mIDs.get(position));
//                Toast.makeText(mContext, mIDs.get(position), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mContext, ListingActivity.class);
                intent.putExtra("ITEM_ID", mIDs.get(position));
                intent.putExtra("CITY", zips.get(position));
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mIDs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;
        TextView soldInfo;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image);
            soldInfo = (TextView) itemView.findViewById(R.id.ratedInformationImage);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) {
                mClickListener.onItemClick(view, getAdapterPosition());
            }
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
