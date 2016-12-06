package org.hisp.dhis.android.app.views.synchronization;

import org.hisp.dhis.client.sdk.ui.bindings.views.View;

public interface SynchronizationView extends View {
    void showProgress();
    void hideProgress();
    void onSyncFinish();

    void showError(String description);

    void showUnexpectedError(String description);
}
