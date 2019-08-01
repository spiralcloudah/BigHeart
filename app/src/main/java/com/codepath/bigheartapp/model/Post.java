package com.codepath.bigheartapp.model;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ParseClassName("Post")
public class Post extends ParseObject implements Serializable {

    // Set unchangeable variables to specified columns in the parse database
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_USER = "userId";
    public static final String KEY_DATE = "createdAt";
    public static final String KEY_IS_EVENT = "isEvent";
    public static final String KEY_DAY = "day";
    public static final String KEY_TIME = "time";
    public static final String KEY_LIKED_BY = "hearts";
    public static final String KEY_EVENT_TITLE = "eventTitle";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_BOOKMARKED = "bookmarked";


    public String getEventId() {
        return getObjectId();
    }

    public void setEventTitle(String title) {
        put(KEY_EVENT_TITLE, title);
    }

    public String getEventTitle() {
        return getString(KEY_EVENT_TITLE);
    }


    // Configure description
    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    // Configure image
    public ParseFile getImage() {
        return getParseFile(KEY_IMAGE);
    }

    public void setImage(ParseFile image) {
        put(KEY_IMAGE, image);
    }

    // Configure location
    public ParseGeoPoint getLocation() {
        return getParseGeoPoint(KEY_LOCATION);
    }

    public void setLocation(ParseGeoPoint location) {
        put(KEY_LOCATION, location);
    }

    // Configure user
    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    // Configure post/event status
    public boolean getIsEvent() {
        return getBoolean(KEY_IS_EVENT);
    }

    public void setIsEvent(boolean isEvent) {
        put(KEY_IS_EVENT, isEvent);
    }

    // Configure day
    public String getDay() {
        return getString(KEY_DAY);
    }

    public void setDay(String day) {
        put(KEY_DAY, day);
    }

    // Configure time
    public String getTime() {
        return getString(KEY_TIME);
    }

    public void setTime(String time) {
        put(KEY_TIME, time);
    }

    //Configure likes/hearts
    public List getLikes() {
        return getList(KEY_LIKED_BY);
    }

    public void likePost(ParseUser user) {
        add(KEY_LIKED_BY, user);
    }

    public void unlikePost(ParseUser user) {
        ArrayList<ParseUser> a = new ArrayList<>();
        a.add(user);
        removeAll(KEY_LIKED_BY, a);
    }


    // Check to see whether a post is liked
    public boolean isLiked() {
        List<ParseUser> a = getLikes();
        if (a != null) {
            for (int i = 0; i < a.size(); i++) {
                try {
                    if (a.get(i).getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    // Configure address
    public String getAddress() {
        return getString(KEY_ADDRESS);
    }

    public void setAddress(String address) {
        put(KEY_ADDRESS, address);
    }

    public void bookmarkPost(String postId) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        currentUser.addUnique(KEY_BOOKMARKED, postId);
        currentUser.saveInBackground();
    }

    public boolean isBookmarked(Post post) {
        JSONArray bookmarks = getBookmarked();
        if (bookmarks != null) {
            for (int i = 0; i < bookmarks.length(); i++) {
                try {
                    if (bookmarks.get(i).toString().equals(post.getObjectId())) {
                        return true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public JSONArray getBookmarked() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        return currentUser.getJSONArray(KEY_BOOKMARKED);
    }

    public void removeBookmark(Post post) {
        ParseUser user = ParseUser.getCurrentUser();
        String rmId = post.getEventId();

        JSONArray bookmarks = user.getJSONArray(KEY_BOOKMARKED);
        //JSONArray newbookmarks = new JSONArray();

        for (int i = 0; i < bookmarks.length(); i++) {

            try {
                if (!rmId.equals(bookmarks.get(i).toString())) {
                    bookmarks.remove(i);
                    //newbookmarks.put(bookmarks.get(i).toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        user.put(KEY_BOOKMARKED, bookmarks);
        user.saveInBackground();
    }

    // Get the number of bookmarked events
    public int getNumBookmarks() {
        return getBookmarked().length();
    }


    // Querying for posts
    public static class Query extends ParseQuery<Post> {
        public Query() {
            super(Post.class);
        }

        public Query getTop() {
            setLimit(20);
            return this;
        }

        public Query withUser() {
            include("user");
            return this;
        }
    }
}

