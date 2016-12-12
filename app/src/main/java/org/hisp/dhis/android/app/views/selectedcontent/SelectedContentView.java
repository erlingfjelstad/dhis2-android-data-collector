package org.hisp.dhis.android.app.views.selectedcontent;

import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.ReportEntity;
import org.hisp.dhis.client.sdk.ui.models.ReportEntityFilter;

import java.util.List;

public interface SelectedContentView extends View {
    void showError(String message);

    void showUnexpectedError(String message);

    void showReportEntities(List<ReportEntity> reportEntityList);

    void hideProgressBar();

    void showProgressBar();

    void notifyFiltersChanged(List<ReportEntityFilter> reportEntityFilters);
}
