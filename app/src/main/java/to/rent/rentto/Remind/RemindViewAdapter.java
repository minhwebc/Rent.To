package to.rent.rentto.Remind;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import to.rent.rentto.Models.RemindMessageItem;
import to.rent.rentto.R;

public class RemindViewAdapter extends ArrayAdapter<RemindMessageItem> {
    private static final String TAG = "RRecyclerViewAdapter";
    private ArrayList<RemindMessageItem> list;
    private Context mContext;
    private ArrayList<String> messageList;




    public RemindViewAdapter(RemindActivity context, ArrayList<RemindMessageItem> list, ArrayList<String> messageIDs){
        super(context, 0 , list);
        Log.d(TAG, "constructor: called.");
        this.list = list;
        this.mContext = context;
        this.messageList = messageIDs;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.remind_list_layout,parent,false);

        RemindMessageItem currentMessage = list.get(position);


        ImageView image = (ImageView) listItem.findViewById(R.id.imageView_poster);
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.ic_launcher_background);

        Glide.with(mContext)
                .load(currentMessage.itemURL)
                .apply(requestOptions)
                .into(image);

        TextView name = (TextView) listItem.findViewById(R.id.borrower);
        name.setText("Borrower: " + currentMessage.borrower);

        TextView release = (TextView) listItem.findViewById(R.id.lender);
        release.setText("Lender: " + currentMessage.lender);

        TextView title = (TextView) listItem.findViewById(R.id.title);
        title.setText("Item: " + currentMessage.itemTitle);

        TextView reminder = (TextView) listItem.findViewById(R.id.reminder);
        reminder.setText("Reminder: " + currentMessage.reminder);

        return listItem;
    }

}
