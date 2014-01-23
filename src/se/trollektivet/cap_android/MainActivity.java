package se.trollektivet.cap_android;

import se.trollektivet.database.Post;
import se.trollektivet.sync.IdFetcher;
import se.trollektivet.sync.PostsContentObserver;
import se.trollektivet.sync.PostsProvider;
import se.trollektivet.sync.PostsSyncAdapter;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class MainActivity extends Activity implements LoaderCallbacks<Cursor>, OnSharedPreferenceChangeListener {

	// An account type, in the form of a domain name
	public static final String ACCOUNT_TYPE = "se.trollektivet";
	// The account name
	public static final String ACCOUNT = "troll";

	ContentResolver resolver;
	CursorAdapter postsAdapter;
	SharedPreferences prefs;
	Account account;
	private String username;
	private int userId;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.settings, false);
		prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		prefs.registerOnSharedPreferenceChangeListener(this);
		new IdFetcher(prefs).execute();
		this.username = prefs.getString("setting_username", null);
		this.userId = prefs.getInt("user_id", 0);
 
		// setup resolver and adapter
		ListView postsListView = (ListView) findViewById(R.id.postsListView);
		resolver = getContentResolver();
		postsAdapter = new PostsListAdapter(this, null, 0, userId);
		postsListView.setAdapter(postsAdapter);

		getLoaderManager().initLoader(0, null, this);

		
		account = CreateSyncAccount(this);
		
		
	}

	public static Account CreateSyncAccount(Context context) {
		// Create the account type and default account
		Account newAccount = new Account(ACCOUNT, ACCOUNT_TYPE);
		// Get an instance of the Android account manager
		AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
		/*
		 * Add the account and account type, no password or user data If
		 * successful, return the Account object, otherwise report an error.
		 */
		if (accountManager.addAccountExplicitly(newAccount, null, null)) {
			ContentResolver.setIsSyncable(newAccount, PostsProvider.AUTHORITY, 1);
			ContentResolver.setSyncAutomatically(newAccount, PostsProvider.AUTHORITY, true);
		} else {
			Log.e("Account", "Could not create");
		}
		return newAccount;
	}

	@Override
	protected void onResume() {
		super.onResume();
		// set sync!
		long frequency = Long.parseLong(prefs.getString(getString(R.string.settings_update_interval_key), "15"));
		ContentResolver.addPeriodicSync(account, PostsProvider.AUTHORITY, new Bundle(), frequency);
		requestSync(false, null);

		MultiAutoCompleteTextView inputField = (MultiAutoCompleteTextView) findViewById(R.id.postInput);
		inputField.setOnEditorActionListener(listener);
		inputField.setImeActionLabel("Post", KeyEvent.KEYCODE_ENTER);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// init sync!
		long frequency = 60L * Long.parseLong(prefs.getString(
				getString(R.string.settings_update_interval_background_key), "30"));
		ContentResolver.addPeriodicSync(account, PostsProvider.AUTHORITY, new Bundle(), frequency);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		prefs.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
			startActivity(i);
			break;
		default:
			throw new IllegalArgumentException("NO SUCH OPTION IMPLEMENTED");
		}
		return super.onOptionsItemSelected(item);
	}

	private void createNewPost(String text) {
		if (this.userId != 0 && this.username != null) {
			Post p = new Post(text, this.username, this.userId);
			resolver.insert(Post.POSTS_URI, p.getContentValues());
			requestSync(true, text);
			getLoaderManager().restartLoader(0, null, this);
		} else {
			Toast.makeText(this, "You have not yet received an id. Please try again", Toast.LENGTH_LONG).show();
		}
	}

	private void requestSync(boolean newPost, String text) {
		Bundle syncSettings = new Bundle();
		if (newPost) {
			syncSettings.putBoolean(PostsSyncAdapter.NEW_POST_FLAG, true);
			syncSettings.putString(PostsSyncAdapter.USER_NAME, username);
			syncSettings.putString(PostsSyncAdapter.POST_TEXT, text);
		}
		syncSettings.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		syncSettings.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
		ContentResolver.requestSync(account, PostsProvider.AUTHORITY, syncSettings);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getApplicationContext(), Post.POSTS_URI, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		postsAdapter.swapCursor(cursor);

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		postsAdapter.swapCursor(null);

	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals("setting_username"))
			this.username = sharedPreferences.getString(key, null);

		if (key.equals("user_id")) {
			this.userId = sharedPreferences.getInt(key, 0);
		}

	}

	
	//FULSKIT BLÄ
	private OnEditorActionListener listener = new OnEditorActionListener() {

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			String text = v.getText().toString();
			if (text == null || text.replace(" ", "").equals("")) {
				v.setText("");
				return true;
			}
			
			createNewPost(text);
			v.setText("");

			return true;
		}
	};
}
