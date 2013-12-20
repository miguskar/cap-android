package se.trollektivet.sync;

import se.trollektivet.sync.http_stuff.CAPNewIdRequest;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public class IdFetcher extends AsyncTask<Void, Void, Integer> {

	public final static String USER_ID = "user_id";
	
	SharedPreferences preferences;
	
	public IdFetcher(SharedPreferences p) {
		preferences = p;
	}
	
	@Override
	protected void onPreExecute() {
		if (preferences.getInt(USER_ID, 0) != 0) {
			cancel(true);
			return;
		}
	}
	
	@Override
	protected Integer doInBackground(Void... params) {
		CAPNewIdRequest request = new CAPNewIdRequest();
		int idValue = 0;

		try {
			request.execute();
			idValue = request.getIdResult();
		} catch (Exception e) {
			Log.e("ERROR", e.getMessage());
		}
		
		return idValue;
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		preferences.edit().putInt(USER_ID, result).apply();
		Log.i("IDCHECK", "YOUR ID IS: " + result);
	}

}
