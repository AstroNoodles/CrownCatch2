package com.github.astronoodles.crowncatch2.cards;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.github.astronoodles.crowncatch2.R;
import com.github.astronoodles.crowncatch2.RSSWorker;

import java.util.List;

public class RecyclerCardAdapter extends RecyclerView.Adapter<RecyclerCardAdapter.ViewHolder>{

    private final List<String> imageLinksList;
    private final Context c;
    private final List<String> headers;
    private final List<String> links;
    private final List<String> sourceList;
    private final List<String> hoursAgoList;

    // note: the imageList was changed from List<Bitmap> to List<String>
    public RecyclerCardAdapter(Context c, List<String> imageList, List<String> headers,
                                List<String> links, List<String> sourceList, List<String> hoursAgoList){
        this.imageLinksList = imageList;
        this.headers = headers;
        this.c = c;
        this.links = links;
        this.sourceList = sourceList;
        this.hoursAgoList = hoursAgoList;

        if(sourceList.size() != hoursAgoList.size()){
            System.out.println("Make sure that the lists are the same size!");
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(c).inflate(R.layout.rss_recycler_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.i(RSSWorker.WORKER_LOG, "Dataset has been changed");
//        holder.imView.setImageBitmap(imageList.get(position));
        holder.header.setText(headers.get(position));
        holder.from.setText(sourceList.get(position));
        holder.hours_ago.setText(hoursAgoList.get(position));

        String imageLink = imageLinksList.get(position);
        Log.i(NewsRecyclerFragment.RECYCLER_NEWS_TAG, "Im to be loaded is: " + imageLink);
        Glide.
                with(c).load(imageLink).
                placeholder(R.drawable.social_distancing_poster).
                fitCenter().into(holder.imView);

        holder.toWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(links.get(position));
                Intent linkToWeb = new Intent(Intent.ACTION_VIEW, uri);
                if(linkToWeb.resolveActivity(c.getPackageManager()) != null){
                    Log.i(RSSWorker.WORKER_LOG, headers.get(position));
                    Log.i(RSSWorker.WORKER_LOG, sourceList.get(position));
                    c.startActivity(linkToWeb);
                }
            }
        });



    }

    @Override
    public int getItemCount() {
        return sourceList.size();
    }



    protected static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imView;
        private final TextView header;
        private final TextView from;
        private final ImageButton toWeb;
        private final TextView hours_ago;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imView = itemView.findViewById(R.id.image);
            header = itemView.findViewById(R.id.header);
            from = itemView.findViewById(R.id.source);
            toWeb = itemView.findViewById(R.id.image);
            hours_ago = itemView.findViewById(R.id.hours_ago);
        }
    }
}
