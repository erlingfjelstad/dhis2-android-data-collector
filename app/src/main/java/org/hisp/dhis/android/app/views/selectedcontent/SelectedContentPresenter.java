package org.hisp.dhis.android.app.views.selectedcontent;

import android.app.Activity;

import org.hisp.dhis.client.sdk.ui.bindings.presenters.Presenter;
import org.hisp.dhis.client.sdk.ui.models.ReportEntity;

import java.io.IOException;

public interface SelectedContentPresenter extends Presenter {
    void sync() throws IOException;

    void handleError(Throwable throwable);

    void updateContents(String trackedEntityUid, String contentType);

    void configureFilters(String contentId, String contentType);

    void configureFloatingActionMenu(String trackedEntityUid);

    void navigate(String contentId, String contentTitle, String contentType);

    void deleteItem(ReportEntity reportEntity, String contentType);
}
