package se.trollektivet.sync;

import se.trollektivet.database.CapDbHelper;
import se.trollektivet.database.Post;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class PostsProvider extends ContentProvider {

	public static final String AUTHORITY = "se.trollektivet.sync.PostsProvider";

	private static final int POSTS = 1;
	private static final int POST = 2;
	private static final int LATEST_POST = 3;

	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sUriMatcher.addURI(AUTHORITY, "posts", POSTS);
		sUriMatcher.addURI(AUTHORITY, "posts/#", POST);
		sUriMatcher.addURI(AUTHORITY, "posts/latest", LATEST_POST);
	}

	private SQLiteOpenHelper databaseHelper;

	@Override
	public boolean onCreate() {
		databaseHelper = new CapDbHelper(getContext());
		return true;
	}

	/*
	 * Return an empty String for MIME type
	 */
	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case POSTS:
			return "vnd.android.cursor.dir/vnd.se.trollektivet.provider." + Post.TABLE_NAME;
		case POST:
			return "vnd.android.cursor.item/vnd.se.trollektivet.provider." + Post.TABLE_NAME;
		default:
			throw new IllegalArgumentException();
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		String table = Post.TABLE_NAME;

		if (selection == null)
			selection = "";

		switch (sUriMatcher.match(uri)) {
		case POSTS:
			break;
		case POST:
			selection += String.format("%s = %s", Post._ID, uri.getLastPathSegment());
			break;
		case LATEST_POST:
			projection = new String[] { Post.COLUMN_CDATE };
			selection = Post.COLUMN_POSTER_ID + " != ?";
			sortOrder = Post.COLUMN_CDATE + " DESC LIMIT 1";
			break;
		default:
			throw new IllegalArgumentException(AUTHORITY + " Could not match uri on query: " + uri.toString());
		}

		Cursor c = null;
		try {
			SQLiteDatabase db = databaseHelper.getReadableDatabase();
			c = db.query(table, projection, selection, selectionArgs, null, null, sortOrder);
		} catch (SQLException e) {
			Log.e(uri.toString(), e.getMessage());
		}
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		String table = Post.TABLE_NAME;

		long row = 0;
		try {
			SQLiteDatabase db = databaseHelper.getWritableDatabase();
			row = db.insert(table, null, values);
		} catch (SQLException e) {
			Log.e(uri.toString(), e.getMessage());
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.withAppendedPath(uri, String.valueOf(row));
	}
	

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		String table = Post.TABLE_NAME;
		switch (sUriMatcher.match(uri)) {
		case POST:
			selection += String.format("%s = %s", Post._ID, uri.getLastPathSegment());
		case POSTS:
			break;
		default:
			throw new IllegalArgumentException("Could not match uri on delete: " + uri.toString());
		}

		int rows = 0;
		try {
			SQLiteDatabase db = databaseHelper.getWritableDatabase();
			rows = db.delete(table, selection, selectionArgs);
		} catch (SQLException e) {
			Log.e(uri.toString(), e.getMessage());
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rows;
	}

	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

		String table = Post.TABLE_NAME;

		switch (sUriMatcher.match(uri)) {
		case POST:
			selection += String.format("%s = %s", Post._ID, uri.getLastPathSegment());
		case POSTS:
			break;
		default:
			throw new IllegalArgumentException("Could not match uri on update: " + uri.toString());
		}

		int rows = 0;
		try {
			SQLiteDatabase db = databaseHelper.getWritableDatabase();
			rows = db.update(table, values, selection, selectionArgs);
		} catch (SQLiteException e) {
			Log.e(uri.toString(), e.getMessage());
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rows;
	}
}