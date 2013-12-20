package se.trollektivet.sync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import se.trollektivet.cap_android.R;
import se.trollektivet.database.Post;
import se.trollektivet.sync.http_stuff.CAPGetPostsRequest;
import se.trollektivet.sync.http_stuff.CAPNewIdRequest;
import se.trollektivet.sync.http_stuff.CAPNewPostRequest;

import android.accounts.Account;
import android.app.Application;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.JsonReader;
import android.util.Log;

//Make server sync instead!
//http://stackoverflow.com/questions/6956626/how-to-get-device-network-information-android
public class PostsSyncAdapter extends AbstractThreadedSyncAdapter {

	public final static String USER_ID = "user_id";
	public final static String NEW_POST_FLAG = "NEW_POST_SYNC";
	public final static String USER_NAME = "username";
	public final static String POST_TEXT = "post_text";

	public final static String URL_CONNECTION_STRING = "http://majki.se/CAP.php";
	SharedPreferences prefs;
	long lastSync = 0;

	public PostsSyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);

		context.getContentResolver();
	}

	public PostsSyncAdapter(Context context, boolean autoInitialize, boolean allowParallellSyncs) {
		super(context, autoInitialize, allowParallellSyncs);

		context.getContentResolver();
		prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
	}

	@Override
	public void onPerformSync(Account account, Bundle bundle, String authority, ContentProviderClient provider,
			SyncResult syncResult) {
		prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

		if (bundle.getBoolean(NEW_POST_FLAG)) {
			// TODO lägg till post i lokala databasen
			sendNewPost(bundle);
		}

		List<Post> newPosts = getNewPosts(provider);
		Log.i("SYNC", "RECEIVED " + newPosts.size() + " new posts");
		try {
			for (Post p : newPosts) {
				provider.insert(Post.POSTS_URI, p.getContentValues());
			}
		} catch (RemoteException e) {
			Log.e("POSTSSYNCADAPTER", "COULD NOT CREATE POST ON INSERT");
		}
	}

	private List<Post> getNewPosts(ContentProviderClient provider) {
		String userId = Integer.toString(prefs.getInt(USER_ID, 0));
		Cursor c = null;
		JSONObject result = null;

		try {
			c = provider.query(Post.LATEST_POST_URI, null, null, new String[] { userId }, null);
			CAPGetPostsRequest request;

			if (c.moveToFirst()) {
				request = new CAPGetPostsRequest(userId, c.getString(0));
			} else {
				request = new CAPGetPostsRequest(userId, "0");
			}
			request.execute();
			result = new JSONObject(request.getResultString());
		} catch (RemoteException e1) {
			Log.e("POSTSSYNCADAPTER", "COULD NOT FETCH LATEST POST");
		} catch (ClientProtocolException e) {
			Log.e("POSTSSYNCADAPTER", "COULD NOT CONNECT: " + e.getMessage());
		} catch (IOException e) {
			Log.e("POSTSSYNCADAPTER", "????????");
		} catch (JSONException e) {
			Log.e("POSTSSYNCADAPTER", "COULD NOT PARSE JSON: " + e.getMessage());
		}

		ArrayList<Post> resultPosts = new ArrayList<Post>();
		JSONArray jsonPosts;
		try {
			jsonPosts = result.getJSONArray("posts");
			for (int i = 0; i < jsonPosts.length(); ++i) {
				try {
					resultPosts.add(new Post(jsonPosts.getJSONObject(i)));
				} catch (Exception e) {
					Log.e("POSTSSYNCADAPTER", "COULD NOT CREATE POST: " + e.getMessage());
				} 
			}
		} catch (JSONException e1) {
			Log.e("OJDÅ HEHE", "SYNCADAPTER ERROR!!!! DET FANNS INGEN POSTS ARRAY I JSONET!!");
		}

		return resultPosts;
	}

	private void sendNewPost(Bundle b) {

		int userId = prefs.getInt(USER_ID, 0);
		String name = b.getString(USER_NAME);
		String text = b.getString(POST_TEXT);
		CAPNewPostRequest request = new CAPNewPostRequest(userId, name, text);
		try {
			request.execute();
		} catch (Exception e) {
			Log.e("ERROR", e.getMessage());
		}
		request.checkResult();
	}

	
}
