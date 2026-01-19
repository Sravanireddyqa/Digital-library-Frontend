package com.simats.digitallibrary;

import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONException;
import org.json.JSONObject;

public class Notification implements Parcelable {
    private String id;
    private int userId;
    private NotificationType type;
    private String title;
    private String message;
    private JSONObject data;
    private boolean isRead;
    private long timestamp;

    public Notification() {
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
    }

    public Notification(String id, int userId, NotificationType type, String title, String message) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
        this.data = new JSONObject();
    }

    // Parcelable implementation
    protected Notification(Parcel in) {
        id = in.readString();
        userId = in.readInt();
        String typeKey = in.readString();
        type = NotificationType.fromKey(typeKey);
        title = in.readString();
        message = in.readString();
        isRead = in.readByte() != 0;
        timestamp = in.readLong();

        String dataString = in.readString();
        try {
            data = dataString != null ? new JSONObject(dataString) : new JSONObject();
        } catch (JSONException e) {
            data = new JSONObject();
        }
    }

    public static final Creator<Notification> CREATOR = new Creator<Notification>() {
        @Override
        public Notification createFromParcel(Parcel in) {
            return new Notification(in);
        }

        @Override
        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeInt(userId);
        dest.writeString(type.getKey());
        dest.writeString(title);
        dest.writeString(message);
        dest.writeByte((byte) (isRead ? 1 : 0));
        dest.writeLong(timestamp);
        dest.writeString(data != null ? data.toString() : null);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    public void setData(String jsonString) {
        try {
            this.data = new JSONObject(jsonString);
        } catch (JSONException e) {
            this.data = new JSONObject();
        }
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimeAgo() {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else {
            return "Just now";
        }
    }
}
