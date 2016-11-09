package org.hisp.dhis.android.app.model;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.hisp.dhis.android.app.model.SyncAdapter;

public class SyncService extends Service {

    private static final Object syncAdapterLock = new Object();
    private static SyncAdapter syncAdapter;

    @Override
    public void onCreate() {
        super.onCreate();
        /* Create syncAdapter as a singleton. */
        synchronized (syncAdapterLock) {
            if (syncAdapter == null) {
                syncAdapter = new SyncAdapter(getApplicationContext(), true, true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }
}