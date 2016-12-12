package org.hisp.dhis.android.app.views.dashboard;

import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.ContentEntity;

import java.util.List;

public interface DashboardView extends View {

    void swapData(List<ContentEntity> contentEntities);

    void showProgressBar();

    void hideProgressBar();

    void showUnexpectedError(String message);

    void showError(String message);
}
