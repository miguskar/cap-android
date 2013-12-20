package se.trollektivet.sync.http_stuff;

import android.util.Log;

public class CAPNewPostRequest extends CAPBaseHttpRequest {

	private static final String ORDER_ID = "newPost";
	
	public CAPNewPostRequest(int user_id, String name, String text) {
		super(ORDER_ID);
		addParameter("poster_id", String.valueOf(user_id));
		addParameter("name", name);
		addParameter("text", text);
	}
	
	public void checkResult() {
		Log.e("NEW_POST_RESULT", resultString);
	}

}
