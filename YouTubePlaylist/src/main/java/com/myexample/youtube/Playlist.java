package com.myexample.youtube;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
    public final int totalResults;
    public final int resultsPerPage;

    public List<Page> pages;

    public Playlist(JSONObject jsonPlaylist) throws JSONException {
        pages = new ArrayList<Page>();

        JSONObject pageInfo = jsonPlaylist.getJSONObject("pageInfo");
        totalResults = pageInfo.getInt("totalResults");
        resultsPerPage = pageInfo.getInt("resultsPerPage");

        addPage(jsonPlaylist);
    }

    public int getCount() {
        int count = 0;
        for (Page page : pages) {
            count += page.items.size();
        }

        return count;
    }

    public void addPage(JSONObject jsonPlaylist) throws JSONException {
        pages.add(new Page(jsonPlaylist.getJSONArray("items"), jsonPlaylist.getString("etag"), jsonPlaylist.optString("nextPageToken", null)));
    }

    public PlaylistItem getItem(int position) {
        int pageNumber = position / resultsPerPage;
        Page page = pages.get(pageNumber);

        return page.items.get(position % resultsPerPage);
    }

    public String getNextPageToken(int position) {
        int pageNumber = position / resultsPerPage;
        Page page = pages.get(pageNumber);

        return page.nextPageToken;
    }

    public String getEtag(int position) {
        int pageNumber = position / resultsPerPage;
        Page page = pages.get(pageNumber);

        return page.eTag;
    }

    public class Page {
        public final String nextPageToken;
        public final List<PlaylistItem> items;
        public final String eTag;

        Page(JSONArray jsonItems, String etag, String nextPageToken) throws JSONException {
            eTag = etag;
            items = new ArrayList<PlaylistItem>(jsonItems.length());
            this.nextPageToken = nextPageToken;

            for (int i = 0; i < jsonItems.length(); i++) {
                JSONObject item = jsonItems.getJSONObject(i);
                items.add(new PlaylistItem(item));
            }
        }
    }
}
