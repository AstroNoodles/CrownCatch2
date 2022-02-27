package com.github.astronoodles.crowncatch2;

import android.content.Context;
import android.util.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RSSSite {
    private final URL siteName;
    private final XmlPullParser xpp;

    // TODO Convert images to bitmaps
    private final List<String> images = new ArrayList<>(15);
    private final List<String> sources = new ArrayList<>(15);
    private final List<String> headers = new ArrayList<>(15);
    private final List<String> links = new ArrayList<>(15);
    private final List<String> hours_ago = new ArrayList<>(15);

    public RSSSite(URL siteName, XmlPullParser xpp, Context c){
        this.siteName = siteName;
        this.xpp = xpp;

        try {
            if (siteName.getHost().contains("cdc")) {
                readCDCRSS(c);
            } else {
                readNormalRSS(c);
            }
        } catch(IOException | XmlPullParserException e){
            e.printStackTrace();
        }

    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<String> getLinks() {
        return links;
    }

    public List<String> getDate() {
        return hours_ago;
    }

    public List<String> getImages() {
        return images;
    }

    public List<String> getSources() {
        return sources;
    }

    private void readNormalRSS(Context c) throws XmlPullParserException, IOException {
        boolean insideItem = false;
        int eventType = xpp.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if(eventType == XmlPullParser.START_TAG) {
                boolean hasImage = false;
                switch (xpp.getName().toLowerCase()) {
                    case "item":
                        insideItem = true;
                        break;
                    case "title":
                        if (insideItem) {
                            String title = xpp.nextText();
                            Log.i(RSSWorker.WORKER_LOG, "Reached pulling out title normal " + title);
                            headers.add(title);
                        }
                        break;
                    case "pubdate":
                        // TODO Restyle date to fit all rss pages
                        String date = formatDateFromRSS(xpp.nextText());
                        hours_ago.add(date);
                        Log.i(RSSWorker.WORKER_LOG, "Reached pulling out date " + date);
                        break;
                    case "link":
                        if (insideItem) {
                            String otherLink = xpp.nextText();
                            Log.i(RSSWorker.WORKER_LOG, "Page Link: " + otherLink);
                            links.add(otherLink);
                        }
                        break;
                    case "content:encoded":
                        // This is questionable for NPR but I don't want the full HTML text, just the image
                        try {
                            String imgHTML = xpp.nextText();

                            int srcStart = imgHTML.indexOf("src='") + 5;
                            int srcEnd = imgHTML.indexOf("' alt") - 1;

                            if (srcEnd == -1 || srcStart == -1) {
                                srcEnd = imgHTML.indexOf("' />") - 1;
                            }
                            String imgLink = imgHTML.substring(srcStart, srcEnd);
                            Log.i(RSSWorker.WORKER_LOG, "The normal image link is: " + imgLink);
                            images.add(imgLink);

                            Log.i(RSSWorker.WORKER_LOG, sources.toString());
                            hasImage = true;
                        } catch(IndexOutOfBoundsException e){
                            hasImage = false;
                        }
                        break;
                    case "media:content":
                        // this will return the url image for the NYT rss feed
                        String urlNYT = xpp.getAttributeValue(null, "url");
                        Log.i(RSSWorker.WORKER_LOG, "Other types of img links include: " + urlNYT);
                        images.add(urlNYT);
                        hasImage = true;
                        break;
                }
                if (!hasImage) {
                    String imageURL = "";
                    if (siteName.getHost().contains("fda")) {
                        // TODO You must get credit in order to use this image! You could be sued!!!!
                        // TODO The same goes for all images here!
                        imageURL = "https://pbs.twimg.com/profile_images/773141404860162061/WK4RgHMx_400x400.jpg";
                    } else if(siteName.getHost().contains("npr")) {
                        imageURL = "https://media.npr.org/assets/img/2019/06/17/nprlogo_rgb_whiteborder_custom-7c06f2837fb5d2e65e44de702968d1fdce0ce748-s600-c85.png";
                    } else if(siteName.getHost().contains("nyt")){
                        imageURL = "https://upload.wikimedia.org/wikipedia/commons/7/77/The_New_York_Times_logo.png";
                    }
                    images.add(imageURL);


                    String host = siteName.getHost();
                    if(host.contains("npr.org")){
                        sources.add(c.getString(R.string.npr_title));
                    } else if(host.contains("nyt")){
                        sources.add(c.getString(R.string.nyt));
                    } else if(host.contains("fda.gov")){
                        sources.add(c.getString(R.string.fda_title));
                    } else {
                        sources.add(c.getString(R.string.unidentified_title));
                    }
                }

            } else if(eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")){
                insideItem = false;
            }
            eventType = xpp.next();
        }
    }

    private void readCDCRSS(Context c) throws XmlPullParserException, IOException{

        if(!siteName.getHost().contains("cdc")){
            throw new IllegalArgumentException("Only use this method for the cdc website!");
        }

        // The CDC newsroom has weird rss syntax that I should put it into a new method.
        byte events = 0;
        boolean insideEvent = false;
        int eventType = xpp.getEventType();

        while(eventType != XmlPullParser.END_DOCUMENT){
            if(eventType == XmlPullParser.START_TAG){
                switch(xpp.getName().toLowerCase()){
                    case "event":
                        insideEvent = true;
                        break;
                    case "title":
                        if(insideEvent){
                            String titleCDC = xpp.nextText();
                            Log.i(RSSWorker.WORKER_LOG, "Reached pulling out title " + titleCDC);
                            headers.add(titleCDC);
                        }
                        break;
                    case "id":
                        if(insideEvent){
                            String link = xpp.nextText();
                            Log.i(RSSWorker.WORKER_LOG, "Reached pulling out cdc link " + link);
                            links.add(link);
                        }
                        break;
                    case "updated":
                        if(insideEvent){
                            System.out.println("date obtained from cdc");
                            hours_ago.add(formatDateFromRSS(xpp.nextText()));
                        }
                        break;
                }
                sources.add(c.getString(R.string.cdc_newsroom));

                String cdcImg = "https://upload.wikimedia.org/wikipedia/commons/thumb/7/71/US_CDC_logo.svg/1280px-US_CDC_logo.svg.png";
                images.add(cdcImg);

            } else if(eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("event")){
                insideEvent = false;
            }
            eventType = xpp.next();

            if(events++ > 15){
                eventType = XmlPullParser.END_DOCUMENT;
            }
        }
    }

    private String formatDateFromRSS(String rssDate){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
            Date date = sdf.parse(rssDate);

            SimpleDateFormat usformat = new SimpleDateFormat("EEE, MMM dd yyyy hh:mm:ss a", Locale.US);
            return usformat.format(date);
        } catch(ParseException e){
            e.printStackTrace();
        }
        return "Unable to parse date " + rssDate;
    }

    @Override
    public String toString() {
        return "RSSSite reading the following website's info: " + siteName;
    }
}
