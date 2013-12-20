package se.trollektivet.sync;

import android.accounts.Account;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

public class PostsContentObserver extends ContentObserver {

	public Account account;
	
	public PostsContentObserver(Handler handler) {
		super(handler);
	}
	
	public PostsContentObserver(Handler handler, Account account) {
		super(handler);
		this.account = account;
	}
	
	 /*
     * Define a method that's called when data in the
     * observed content provider changes.
     * This method signature is provided for compatibility with
     * older platforms.
     */
    @Override
    public void onChange(boolean selfChange) {
        /*
         * Invoke the method signature available as of
         * Android platform version 4.1, with a null URI.
         */
        onChange(selfChange, null);
    }
    /*
     * Define a method that's called when data in the
     * observed content provider changes.
     */
    @Override
    public void onChange(boolean selfChange, Uri changeUri) {
        /*
         * Ask the framework to run your sync adapter.
         * To maintain backward compatibility, assume that
         * changeUri is null.
         */
    	ContentResolver.requestSync(account, PostsProvider.AUTHORITY, null);
    }

}
