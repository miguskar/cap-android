package se.trollektivet.sync.http_stuff;

public class CAPGetPostsRequest extends CAPBaseHttpRequest {

	private static final String ORDER_ID = "getNewPostsMobile";
	
	public CAPGetPostsRequest(String username, String lastPost) {
		super(ORDER_ID);
		addParameter("username", username);
		addParameter("last_post", lastPost);
	}

	public String getResultString() {
		return resultString;
	}
}
