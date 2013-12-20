package se.trollektivet.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class PostsSyncService extends Service {

	private PostsSyncAdapter syncAdapter;
	
	private static final Object syncAdapterLock = new Object();
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("SERVICE", "Service created");
		// android.os.Debug.waitForDebugger();
		/*
         * Create the sync adapter as a singleton.
         * Set the sync adapter as syncable
         * Disallow parallel syncs
         */
		synchronized (syncAdapterLock) {
			if (syncAdapter == null)
				syncAdapter = new PostsSyncAdapter(getApplicationContext(), true);
		}
	}
	
	
	@Override
	public IBinder onBind(Intent intent) {
		/*
         * Get the object that allows external processes
         * to call onPerformSync(). The object is created
         * in the base class code when the SyncAdapter
         * constructors call super()
         */
        return syncAdapter.getSyncAdapterBinder();
	}

}
