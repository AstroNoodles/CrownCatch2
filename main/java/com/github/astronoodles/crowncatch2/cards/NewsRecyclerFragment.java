package com.github.astronoodles.crowncatch2.cards;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.*;
import com.github.astronoodles.crowncatch2.MapActivity2;
import com.github.astronoodles.crowncatch2.R;
import com.github.astronoodles.crowncatch2.RSSWorker;
import com.github.astronoodles.crowncatch2.cards.RecyclerCardAdapter;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewsRecyclerFragment extends Fragment {
    private ProgressBar pBar;
    private RecyclerView rv;
    private Button startTask;
    private RecyclerCardAdapter adapter;
    
    public static final String RECYCLER_NEWS_TAG = "NewsRecyclerFragment";

    private List<Bitmap> images = new ArrayList<Bitmap>(30);
    private List<String> links = new ArrayList<>(30);
    private List<String> headers = new ArrayList<>(30);
    private List<String> sources = new ArrayList<>(30);
    private List<String> dates = new ArrayList<>(30);
    private List<String> imageLinks = new ArrayList<>(30);


    public NewsRecyclerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.rss, container, false);
        startTask = v.findViewById(R.id.startNews);
        rv = v.findViewById(R.id.recycler);
        pBar = v.findViewById(R.id.progressBar);

        adapter = new RecyclerCardAdapter(getActivity(), imageLinks, headers, links, sources, dates);

        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv.setAdapter(adapter);
        // TODO This will be done later. Not important now!
        final String[] urls = new String[]{
                //"https://tools.cdc.gov/podcasts/feed.asp?feedid=183",
                // "https://www.fda.gov/about-fda/contact-fda/stay-informed/rss-feeds/medwatch/rss.xml",
                "https://rss.nytimes.com/services/xml/rss/nyt/HomePage.xml",
                //new URL("feeds.bbci.co.uk/news/world/us_and_canada/rss.xml"),
                "https://feeds.npr.org/1001/rss.xml"
                // TODO Replace all empty images with placeholder logos in the RSSSite java class!
        };
        final WorkRequest request = createWork(urls);

        startTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // new RSSAsyncTask(NewsRecyclerFragment.this).execute(urls);

                WorkManager.getInstance(v.getContext()).enqueue(request);

                startTask.setClickable(false);
                pBar.setProgress(0);
                pBar.setVisibility(View.VISIBLE);
            }
        });

        WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(request.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if(workInfo != null && workInfo.getState() == WorkInfo.State.SUCCEEDED){
                            pBar.setProgress(50, true);

                            Data d = workInfo.getOutputData();
                            String[] linksArr = d.getStringArray("links");
                            String[] headersArr = d.getStringArray("headers");
                            String[] sourcesArr = d.getStringArray("sources");
                            String[] datesArr = d.getStringArray("date");
                            String[] imageLinksArr = d.getStringArray("imageLinks");

                            Log.i(RECYCLER_NEWS_TAG, String.format("The image links list has size %d", imageLinksArr.length));

                            Log.i(RECYCLER_NEWS_TAG, "We start with a size of " + images.size());
                            // TODO Create the images from the links and register them in the adapter
                            if(createImageRequest(imageLinksArr)){
                                pBar.setProgress(75, true);
                                Log.i(RECYCLER_NEWS_TAG, "Now to the change the adapter");
                                // images will already be edited by the thread
                                links.addAll(Arrays.asList(linksArr));
                                headers.addAll(Arrays.asList(headersArr));
                                sources.addAll(Arrays.asList(sourcesArr));
                                dates.addAll(Arrays.asList(datesArr));
                                imageLinks.addAll(Arrays.asList(imageLinksArr));
                                Log.i(RECYCLER_NEWS_TAG, imageLinks.toString());
                                adapter.notifyDataSetChanged();
                                pBar.setProgress(100, true);
                                startTask.setClickable(true);
                                pBar.setProgress(100);
                                // pBar.setVisibility(View.INVISIBLE);
                            }


                        } else if(workInfo != null && workInfo.getState() == WorkInfo.State.BLOCKED){
                            pBar.setProgress(20, true);
                        } else if(workInfo != null && workInfo.getState() == WorkInfo.State.FAILED){
                            Snackbar.make(v, R.string.internet_problem, Snackbar.LENGTH_SHORT);
                        }
                    }
                });

        MapActivity2.createNotificationChannel(getContext());
        NotificationManagerCompat nmc = NotificationManagerCompat.from(getContext());

        nmc.notify(3, MapActivity2.buildPointsNotification(getContext(), getString(R.string.news_points)).build());
        MapActivity2.addPointsToCounter(getContext(), 40);

        return v;
    }

    public WorkRequest createWork(String[] data){
        Data workData = new Data.Builder()
                .putStringArray("urls", data)
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        return new OneTimeWorkRequest.Builder(RSSWorker.class)
                .setInputData(workData)
                .setConstraints(constraints)
                .build();
    }

    private boolean createImageRequest(String[] imageLinks){
//        try {
//            Log.i(RECYCLER_NEWS_TAG, "here to start the thread");
//            Thread networkThread = new Thread(new ImageRequest(imageLinks));
//            networkThread.start();
//            networkThread.join();
//        } catch(InterruptedException e){
//            e.printStackTrace();
//        }
//        return images.size() != 0;
        return imageLinks.length != 0;
    }

    public class ImageRequest implements Runnable {
        private final String[] imageLinks;

        public ImageRequest(String[] imageLinks){
            this.imageLinks = imageLinks;
        }

        @Override
        public void run() {
            try {
                
                for (int i = 0; i < imageLinks.length; i++) {
                    Log.i(RSSWorker.WORKER_LOG, "Image URL: " + imageLinks[i]);
                    URL urlLink = new URL(imageLinks[i]);

                    // Setting options to downsize image till the image is 300 x 300. Hopefully loads better?
                    final BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(urlLink.openStream(), null, opts);

                    opts.inSampleSize = dilateImage(opts, 200, 200);

                    opts.inJustDecodeBounds = false;
                    Bitmap img = BitmapFactory.decodeStream(urlLink.openStream(), null, opts);

                    Log.i(RECYCLER_NEWS_TAG, "decoded an image " + i);
                    images.add(img);
                }
            } catch(IOException e){
                e.printStackTrace();
        }
        }

        // This will downsize the image to the wanted width - reqWidth and wanted height - reqHeight
        public int dilateImage(BitmapFactory.Options opt, int reqWidth, int reqHeight){
            int width = opt.outWidth;
            int height = opt.outHeight;
            int downsizeFactor = 1;

            if(width > reqWidth || height > reqHeight){
                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                while((halfWidth / downsizeFactor) >= reqWidth || (halfHeight / downsizeFactor) >= reqHeight) {
                    downsizeFactor *= 2;
                    Log.i(RECYCLER_NEWS_TAG, "Downsizing the image now by " + downsizeFactor);
                }
            }
            return downsizeFactor;
        }

    }





}
