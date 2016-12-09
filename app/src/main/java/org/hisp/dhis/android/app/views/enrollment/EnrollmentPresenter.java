package org.hisp.dhis.android.app.views.enrollment;

import org.hisp.dhis.client.sdk.ui.bindings.presenters.Presenter;
import org.hisp.dhis.client.sdk.ui.models.ReportEntity;

interface EnrollmentPresenter extends Presenter {
    void drawEnrollments(String trackedEntityInstanceUid);
    void navigateToEnrollment(ReportEntity reportEntity, String trackedEntityInstanceUid);
    void createNewEnrollment(String trackedEntityInstanceUid);
}
