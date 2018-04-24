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

        PostInMessage currentMessage = messageList.get(position);

        ImageView image = (ImageView)listItem.findViewById(R.id.imageView_poster);
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.ic_launcher_background);
        Glide.with(mContext)
                .load(currentMessage.imageURL)
                .apply(requestOptions)
                .into(image);

        TextView name = (TextView) listItem.findViewById(R.id.textView_name);
        name.setText(currentMessage.title);

        TextView release = (TextView) listItem.findViewById(R.id.textView_release);
        release.setText(currentMessage.author + ": "  + currentMessage.message);

        return listItem;
    }
}
