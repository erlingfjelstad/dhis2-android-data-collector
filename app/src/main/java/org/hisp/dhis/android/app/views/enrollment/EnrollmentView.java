package org.hisp.dhis.android.app.views.enrollment;

import android.support.annotation.StringDef;

import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.ReportEntity;
import org.hisp.dhis.client.sdk.ui.models.ReportEntityFilter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public interface EnrollmentView extends View {

    static final String ID_PROGRAM = "program";

    void updateReportEntityFilters(List<ReportEntityFilter> reportEntityFilters);

    void drawEnrollments(List<ReportEntity> enrollmentReportEntities);

    void navigateToTeiDashboardWithEnrollment(String programUid, String enrollmentUid);

    void navigateToCreateNewEnrollment(String trackedEntityInstanceUid);

    String getString(@StringId String stringId);

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            ID_PROGRAM
    })
    @interface StringId {
    }
}
