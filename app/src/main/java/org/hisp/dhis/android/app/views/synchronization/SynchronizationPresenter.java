package org.hisp.dhis.android.app.views.synchronization;

import org.hisp.dhis.client.sdk.ui.bindings.presenters.Presenter;

public interface SynchronizationPresenter extends Presenter {
    void handleError(final Throwable throwable);
}
