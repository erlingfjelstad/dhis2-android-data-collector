package org.hisp.dhis.android.app.views.dashboard;

import org.hisp.dhis.client.sdk.ui.bindings.presenters.Presenter;

import java.io.IOException;

public interface DashboardPresenter extends Presenter {
    void populateDashboard();
    void sync() throws IOException;
    void handleError(Throwable throwable);
}
