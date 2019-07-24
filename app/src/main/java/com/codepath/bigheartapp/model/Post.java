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

@ParseClassName("Post")
public class Post extends ParseObject implements Serializable {
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_LOCATION = "location";
    public static final String KEY_USER = "userId";
    public static final String KEY_DATE = "createdAt";
    public static final String KEY_IS_EVENT = "isEvent";
    private static final String KEY_MONTH = "month";
    private static final String KEY_DAY = "day";
    private static final String KEY_YEAR = "year";
    private static final String KEY_TIME = "time";
    private static final String KEY_LIKED_BY = "hearts";
    private static final String KEY_EVENT_TITLE = "hearts";

    public void setEventTitle(String title) {
        put(KEY_EVENT_TITLE, title);
    }

    public String getEventTitle() {
        return getString(KEY_EVENT_TITLE);
    }

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public boolean getIsEvent() {
        return getBoolean(KEY_IS_EVENT);
    }

    public void setIsEvent(boolean isEvent) {
        put(KEY_IS_EVENT, isEvent);
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint(KEY_LOCATION);
    }

    public void setLocation(ParseGeoPoint location) {
        put(KEY_LOCATION, location);
    }

    public ParseFile getImage() {
        return getParseFile(KEY_IMAGE);
    }

    public void setImage(ParseFile image){
        put(KEY_IMAGE, image);
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user){
        put(KEY_USER, user);
    }

    //Date columns
    public String getMonth() {
        return getString(KEY_MONTH);
    }

    public void setMonth(String month) {
        put(KEY_MONTH, month);
    }

    public String getDay() {
        return getString(KEY_DAY);
    }

    public void setDay(String day) {
        put(KEY_DAY, day);
    }

    public String getYear() {
        return getString(KEY_YEAR);
    }

    public void setYear(String year) {
        put(KEY_YEAR, year);
    }

    public String getTime() {
        return getString(KEY_TIME);
    }

    public void setTime(String time) {
        put(KEY_TIME, time);
    }

    //Likes
    public JSONArray getLikes() {
        return getJSONArray(KEY_LIKED_BY);
    }

    public int getNumLikes() { return getLikes().length(); }

    public void likePost(ParseUser user) {
        add(KEY_LIKED_BY, user);
    }

    public void unlikePost(ParseUser user) {
        ArrayList<ParseUser> a = new ArrayList<>();
        a.add(user);
        removeAll(KEY_LIKED_BY, a);
    }

    public boolean isLiked() {
        JSONArray a = getLikes();
        if(a != null) {
            for (int i = 0; i < a.length(); i++) {
                try {
                    if (a.getJSONObject(i).getString("objectId").equals(ParseUser.getCurrentUser().getObjectId())) {
                        return true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

            //Querying
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

