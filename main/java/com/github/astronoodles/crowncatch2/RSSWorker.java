package com.github.astronoodles.crowncatch2;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.core.math.MathUtils;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class RSSWorker extends Worker {
    public static final String WORKER_LOG = "RSSWorker";

    private final Context c;
    private static final int NEWS_ITEMS_DISPLAY = 11;

    public RSSWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        c = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            List<RSSSite> siteInfo = new ArrayList<>(6);
            String[] urlTitles = getInputData().getStringArray("urls");

            URL[] urls = new URL[urlTitles.length];

            for(int i = 0; i < urlTitles.length; i++){
                urls[i] = new URL(urlTitles[i]);
            }


            for(URL url : urls) {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser pullParser = factory.newPullParser();
                pullParser.setInput(url.openStream(), "UTF_8");
                siteInfo.add(new RSSSite(url, pullParser, c));
//                if(url.getHost().contains("cdc")){
//                    // TODO add in here specific code for CDC RSS
//                    Log.i(RSSWorker.WORKER_LOG, "done with cdc rss!");
//                } else {
//                    RSSSite otherSites = new RSSSite(url, pullParser);
//                    Log.i(RSSWorker.WORKER_LOG, "done with normal rss");
//                }
            }

            Random r = new Random(49);

            List<Integer> indexArr = new ArrayList<>(12);

            for(int i = 0; i < 12; i++){
                indexArr.add(r.nextInt(siteInfo.get(0).getHeaders().size()));
            }
            Log.d(WORKER_LOG, "Banana" + trimList(unravelData(siteInfo, SiteInfo.HEADERS, indexArr)));
            Data outputData = new Data.Builder()
                    .putStringArray("sources", trimList(unravelData(siteInfo, SiteInfo.SOURCES, indexArr)).toArray(new String[0]))
                    .putStringArray("headers", trimList(unravelData(siteInfo, SiteInfo.HEADERS, indexArr)).toArray(new String[0]))
                    .putStringArray("links", trimList(unravelData(siteInfo, SiteInfo.LINKS, indexArr)).toArray(new String[0]))
                    .putStringArray("date", trimList(unravelData(siteInfo, SiteInfo.HOURS_AGO, indexArr)).toArray(new String[0]))
                    .putStringArray("imageLinks", trimList(unravelData(siteInfo, SiteInfo.IMAGES, indexArr)).toArray(new String[0]))
                    .build();
//            Data outputData = new Data.Builder()
//                    .putStringArray("sources", trimList(pickUniqueRandom(siteInfo, 12, SiteInfo.SOURCES)).toArray(new String[0]))
//                    .putStringArray("headers", trimList(pickUniqueRandom(siteInfo, 12, SiteInfo.HEADERS)).toArray(new String[0]))
//                    .putStringArray("links", trimList(pickUniqueRandom(siteInfo, 12, SiteInfo.LINKS)).toArray(new String[0]))
//                    .putStringArray("date", trimList(pickUniqueRandom(siteInfo, 12, SiteInfo.HOURS_AGO)).toArray(new String[0]))
//                    .putStringArray("imageLinks", trimList(pickUniqueRandom(siteInfo, 12, SiteInfo.IMAGES)).toArray(new String[0]))
//                    .build();

            return Result.success(outputData);

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
            return Result.failure();
        }
    }

    private List<String> trimList(List<String> ls){
        List<String> ls2 = new ArrayList<>(20);
        for(String s : ls){
            if(!s.isEmpty()){
                ls2.add(s);
            }
        }
        return ls2;
    }

    /**
     * Quick method to get a unique random element from a list depending on a seed and how many unique elements you want.
     * @param ls - The original list to get elements from.
     * @param howMany how many items to pick
     * @return - A list picked with unique random elements
     */
    private List<String> pickUniqueRandom(List<RSSSite> ls, int howMany, SiteInfo indexInfo){
        Random r = new Random();
        List<Integer> indexArr = new ArrayList<>(howMany);
        List<String> randomPicked = new ArrayList<>(howMany);
        for(int i = 0; i < howMany; i++){
            indexArr.add(r.nextInt(ls.size()));
        }
        Collections.shuffle(indexArr, r);

        for(RSSSite site : ls) {
            for (int j = 0; j < howMany / ls.size(); j++) {
                for (int index : indexArr) {
                    switch (indexInfo) {
                        case IMAGES:
                            randomPicked.add(site.getImages().get(index));
                            break;
                        case SOURCES:
                            randomPicked.add(site.getSources().get(index));
                            break;
                        case HEADERS:
                            randomPicked.add(site.getHeaders().get(index));
                            break;
                        case LINKS:
                            randomPicked.add(site.getLinks().get(index));
                            break;
                        case HOURS_AGO:
                            randomPicked.add(site.getDate().get(index));
                            break;
                    }
                }
            }
        }
        return randomPicked;
    }

    private List<String> unravelData(List<RSSSite> bigData, SiteInfo indexInfo, List<Integer> indexArr){
        List<String> combined = new ArrayList<>(1000);

        for(RSSSite siteData : bigData){
            switch(indexInfo){
                case IMAGES:
                    for(int index : indexArr) {
                        List<String> ims = siteData.getImages();
                        Log.d(WORKER_LOG, "Images from Site: " + ims);
                        int clampIndex = MathUtils.clamp(index, 0, ims.size() - 1);
                        combined.add(ims.get(clampIndex));
                    }
                    break;
                case SOURCES:
                    for(int index : indexArr) {
                        List<String> srcs = siteData.getSources();
                        int clampIndex = MathUtils.clamp(index, 0, srcs.size() - 1);
                        combined.add(srcs.get(clampIndex));
                    }
                    break;
                case HEADERS:
                    for(int index : indexArr) {
                        List<String> headers = siteData.getHeaders();
                        int clampIndex = MathUtils.clamp(index, 0, headers.size() - 1);
                        Log.i(RSSWorker.WORKER_LOG, "Header Size: " + headers.size() + " - Site: " + siteData);
                        combined.add(headers.get(clampIndex));
                    }
                    break;
                case LINKS:
                    for(int index : indexArr) {
                        List<String> links = siteData.getLinks();
                        int clampIndex = MathUtils.clamp(index, 0, links.size() - 1);
                        combined.add(links.get(clampIndex));
                    }
                    break;
                case HOURS_AGO:
                    for(int index : indexArr) {
                        List<String> hours = siteData.getDate();
                        int clampIndex = MathUtils.clamp(index, 0, hours.size() - 1);
                        combined.add(hours.get(clampIndex));
                    }
                    break;
            }
        }
        return combined;
    }

    private enum SiteInfo {
        IMAGES, SOURCES, HEADERS, LINKS, HOURS_AGO
    }

}
