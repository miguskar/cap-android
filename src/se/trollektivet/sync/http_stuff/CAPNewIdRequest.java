package se.trollektivet.sync.http_stuff;

public class CAPNewIdRequest extends CAPBaseHttpRequest {

	private static final String ORDER_ID = "getNewId";
	
	public CAPNewIdRequest() {
		super(ORDER_ID);
	}
	
	public int getIdResult() {
		return Integer.parseInt(resultString.replace("\n", ""));
	}
}
