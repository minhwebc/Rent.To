package to.rent.rentto.Messages;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import java.util.List;

import to.rent.rentto.R;

public class MessagePreviewAdapter extends ArrayAdapter<PostInMessage> {
    private Context mContext;
    private List<PostInMessage> messageList = new ArrayList<>();

    public MessagePreviewAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public MessagePreviewAdapter(@NonNull Context context, ArrayList<PostInMessage> list) {
        super(context, 0 , list);
        mContext = context;
        messageList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;

        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.list_item,parent,false);

        Log.d("ArrayAdapter", messageList.toString());

        //PostInMessage currentMessage = messageList.get(position);
        int size = messageList.size();
        int newPosition = size - position - 1;
        PostInMessage currentMessage = messageList.get(newPosition);


        ImageView image = (ImageView)listItem.findViewById(R.id.imageView_poster);
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.ic_launcher_background);
        Glide.with(mContext)
                .load(currentMessage.imageURL)
                .apply(requestOptions)
                .into(image);

        TextView name = (TextView) listItem.findViewById(R.id.textView_name);
        String title = currentMessage.title;
        name.setText(limitStringLength(title, 36));

        TextView release = (TextView) listItem.findViewById(R.id.textView_release);
        String inLineMessagePreview = currentMessage.author + ": " + currentMessage.message;
        release.setText(limitStringLength(inLineMessagePreview, 36));

        return listItem;
    }
//
//    @Override
//    public void notifyDataSetChanged() {
//        super.notifyDataSetChanged();
//        ListView messagesListView = (ListView) ((Activity)mContext).findViewById(R.id.msgview);
//        messagesListView.setSelection(messageList.size());
//        Log.d("MessagePreviewAdapter", "Setting messagesListView to 0");
//    }

    /**
     * Limits the length of the given string to the limit, if it is above the limit
     * @param string
     * @param limit
     */
    private String limitStringLength(String string, int limit) {
        if(string == null || limit < 5 || string.length() <= limit) {
            return string;
        } else {
            return string.substring(0, limit) + "...";
        }
    }
}
