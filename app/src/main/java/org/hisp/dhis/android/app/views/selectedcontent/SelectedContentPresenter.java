package org.hisp.dhis.android.app.views.selectedcontent;

import org.hisp.dhis.client.sdk.ui.bindings.presenters.Presenter;

import java.io.IOException;

public interface SelectedContentPresenter extends Presenter {
    void sync() throws IOException;

    void handleError(Throwable throwable);

    void updateContents(String trackedEntityUid);

    void configureFilters();
}
