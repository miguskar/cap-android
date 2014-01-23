package se.trollektivet.database;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import se.trollektivet.sync.PostsProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Table Posts
 * 
 * @author Mikael
 */
public class Post implements BaseColumns {

	public static final Uri POSTS_URI = Uri.parse("content://" + PostsProvider.AUTHORITY + "/posts");
	public static final Uri LATEST_POST_URI = Uri.parse("content://" + PostsProvider.AUTHORITY + "/posts/latest");
	public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static final String TABLE_NAME = "posts";

	public static final String COLUMN_POST = "post";
	public static final String COLUMN_CDATE = "cdate";
	public static final String COLUMN_USER = "user";
	public static final String COLUMN_POSTER_ID = "poster_id";

	public static final String CREATE_TABLE_SQL = "CREATE TABLE " + TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY, "
			+ COLUMN_POST + " TEXT NOT NULL, " + COLUMN_CDATE + " TEXT NOT NULL, " + COLUMN_USER + " TEXT NOT NULL, "
			+ COLUMN_POSTER_ID + " INTEGER NOT NULL" + ");";

	private int _id;
	private String post;
	private String date;
	private String user;
	private int posterId;

	public Post(String post, String user, int posterId) {
		this(post, DATE_FORMATTER.format(new Date(System.currentTimeMillis())), user, posterId);
	}


	public Post(String post, String date, String user, int posterId) {
		this.post = post;
		this.date = date;
		this.user = user;
		this.posterId = posterId;
	}

	public Post(Cursor c) {
		int postIndex = c.getColumnIndex(COLUMN_POST);
		int dateIndex = c.getColumnIndex(COLUMN_CDATE);
		int userIndex = c.getColumnIndex(COLUMN_USER);
		int posterIdIndex = c.getColumnIndex(COLUMN_POSTER_ID);

		_id = c.getInt(c.getColumnIndex(_ID));

		if (postIndex >= 0)
			post = c.getString(postIndex);
		if (dateIndex >= 0)
			date = c.getString(dateIndex);
		if (userIndex >= 0)
			user = c.getString(userIndex);
		if (posterIdIndex >= 0)
			posterId = c.getInt(posterIdIndex);
	}

	public Post(JSONObject jo) throws JSONException, ParseException {
		if (!jo.isNull(COLUMN_POST)) {
			try {
				this.post = URLDecoder.decode(jo.getString(COLUMN_POST), "ISO-8859-1");
			} catch (UnsupportedEncodingException e) {
				Log.e("POSTS", e.getMessage());
			}
		}
		if (!jo.isNull(COLUMN_POSTER_ID))
			this.posterId = jo.getInt(COLUMN_POSTER_ID);
		if (!jo.isNull("time")) {
			date = jo.getString("time");
		}
		if (!jo.isNull(COLUMN_USER))
			this.user = jo.getString(COLUMN_USER);
	}

	public int get_id() {
		return _id;
	}

	public String getPost() {
		return post;
	}

	public String getDate() {
		return date.toString();
	}

	public long getLongDate() {
		try {
			return DATE_FORMATTER.parse(date).getTime() / 1000L;
		} catch (ParseException e) {
			Log.e("POSTS GETLONGDATE", "COULD NOT PARSE DATE: " + date);
		}
		return 0;
	}

	public String getUser() {
		return user;
	}

	public int getPosterId() {
		return posterId;
	}

	public ContentValues getContentValues() {
		ContentValues values = new ContentValues();
		values.put(Post.COLUMN_POST, this.post);
		values.put(Post.COLUMN_CDATE, this.date);
		values.put(Post.COLUMN_USER, this.user);
		values.put(Post.COLUMN_POSTER_ID, this.posterId);
		return values;
	}
}