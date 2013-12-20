package se.trollektivet.sync.http_stuff;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.util.Log;

public abstract class CAPBaseHttpRequest extends HttpPost {
	
	public final static String CONNECTION_STRING = "http://majki.se/CAP.php";
	
	private List<NameValuePair> parameters = new ArrayList<NameValuePair>(); ;
	protected String resultString; 
	
	public CAPBaseHttpRequest(String order) {
		super(CONNECTION_STRING);
		parameters.add(new BasicNameValuePair("order", order));
	}
	
	protected void addParameter(String name, String value) {
		parameters.add(new BasicNameValuePair(name, value));
	}
	
	public void execute() throws ClientProtocolException, IOException {
		setEntity(new UrlEncodedFormEntity(parameters));
		
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
		HttpConnectionParams.setSoTimeout(httpParams, 10000);
		HttpProtocolParams.setContentCharset(httpParams, "iso-8859-1");
		HttpProtocolParams.setHttpElementCharset(httpParams, "iso-8859-1");
		HttpClient httpclient = new DefaultHttpClient(httpParams);
		
		HttpResponse response = httpclient.execute(this);
		
		HttpEntity entity = response.getEntity();
		// If the response does not enclose an entity, there is no need
		// to worry about connection release

		String result = "";
		
		if (entity != null) {

			// A Simple JSON Response Read
			InputStream instream = entity.getContent();
			result = convertStreamToString(instream);
			// now you have the string representation of the HTML request
			instream.close();
		}
		resultString = result;
	}
	
	private static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader;
		String text = null;
		try {
			reader = new BufferedReader(
					new InputStreamReader(is, "iso-8859-1"), 8);
		
		StringBuilder sb = new StringBuilder();

		String line = null;
		
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			text = sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return text;
	}
}
