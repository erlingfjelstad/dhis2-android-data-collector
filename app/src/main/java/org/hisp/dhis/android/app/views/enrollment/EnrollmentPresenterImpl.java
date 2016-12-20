package org.hisp.dhis.android.app.views.enrollment;

import org.hisp.dhis.client.sdk.core.enrollment.EnrollmentInteractor;
import org.hisp.dhis.client.sdk.core.program.ProgramInteractor;
import org.hisp.dhis.client.sdk.core.trackedentity.TrackedEntityAttributeValueInteractor;
import org.hisp.dhis.client.sdk.core.trackedentity.TrackedEntityInstanceInteractor;
import org.hisp.dhis.client.sdk.models.enrollment.Enrollment;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.ReportEntity;
import org.hisp.dhis.client.sdk.ui.models.ReportEntityFilter;
import org.hisp.dhis.client.sdk.utils.LocaleUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class EnrollmentPresenterImpl implements EnrollmentPresenter {

    private final String TAG = this.getClass().getSimpleName();
    private static final String PROGRAM_KEY = "program";
    private static final String DATE_FORMAT = "yyyy-MM-dd h:mm";
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT, LocaleUtils.getLocale());

    private final EnrollmentInteractor enrollmentInteractor;
    private final ProgramInteractor programInteractor;
    private final TrackedEntityInstanceInteractor trackedEntityInstanceInteractor;
    private final TrackedEntityAttributeValueInteractor trackedEntityAttributeValueInteractor;
    CompositeSubscription subscription;

    EnrollmentView enrollmentView;

    public EnrollmentPresenterImpl(EnrollmentInteractor enrollmentInteractor, ProgramInteractor programInteractor, TrackedEntityInstanceInteractor trackedEntityInstanceInteractor, TrackedEntityAttributeValueInteractor trackedEntityAttributeValueInteractor) {
        this.enrollmentInteractor = enrollmentInteractor;
        this.programInteractor = programInteractor;
        this.trackedEntityInstanceInteractor = trackedEntityInstanceInteractor;
        this.trackedEntityAttributeValueInteractor = trackedEntityAttributeValueInteractor;
    }

    @Override
    public void attachView(View view) {
        enrollmentView = (EnrollmentView) view;
    }

    @Override
    public void detachView() {
        enrollmentView = null;
    }


    @Override
    public void drawEnrollments(final String trackedEntityInstanceUid) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }

        subscription = new CompositeSubscription();
        subscription.add(Observable.just(enrollmentInteractor.store()
                .queryByTrackedEntityInstance(trackedEntityInstanceUid))
                .subscribe(new Action1<List<Enrollment>>() {
                    @Override
                    public void call(List<Enrollment> enrollments) {

                        List<ReportEntityFilter> reportEntityFilters = new ArrayList<>();
                        List<ReportEntity> enrollmentReportEntities = new ArrayList<>();

                        List<TrackedEntityAttributeValue> trackedEntityAttributeValues = trackedEntityAttributeValueInteractor.store().query(trackedEntityInstanceUid);
                        Map<String, TrackedEntityAttributeValue> trackedEntityAttributeValueMap = new HashMap<>();
                        for (TrackedEntityAttributeValue trackedEntityAttributeValue : trackedEntityAttributeValues) {
                            trackedEntityAttributeValueMap.put(trackedEntityAttributeValue.trackedEntityAttributeUid(), trackedEntityAttributeValue);
                        }

                        //adding program name filter
                        ReportEntityFilter reportEntityFilterProgram = new ReportEntityFilter(PROGRAM_KEY, enrollmentView.getString(EnrollmentView.ID_PROGRAM), true);
                        reportEntityFilters.add(reportEntityFilterProgram);

                        for (Enrollment enrollment : enrollments) {
                            Program program = programInteractor.store().queryByUid(enrollment.program());

                            //adding enrollment date filter for each different program type
                            ReportEntityFilter reportEntityFilterDate = new ReportEntityFilter(program.enrollmentDateLabel(), program.enrollmentDateLabel(), true);
                            reportEntityFilters.add(reportEntityFilterDate);

                            List<ProgramTrackedEntityAttribute> programTrackedEntityAttributes = program.programTrackedEntityAttributes();
                            Map<String, String> trackedEntityAttributeValuesToShowInReportEntity = new HashMap<>();
                            for (ProgramTrackedEntityAttribute programTrackedEntityAttribute : programTrackedEntityAttributes) {
                                if (programTrackedEntityAttribute.displayInList()) {

                                    //adding filter for each attribute
                                    ReportEntityFilter reportEntityFilter = new ReportEntityFilter(programTrackedEntityAttribute.trackedEntityAttribute().uid(), programTrackedEntityAttribute.trackedEntityAttribute().displayName(), true);
                                    reportEntityFilters.add(reportEntityFilter);

                                    TrackedEntityAttributeValue trackedEntityAttributeValueToShowInReportEntity = trackedEntityAttributeValueMap.get(programTrackedEntityAttribute.trackedEntityAttribute().uid());
                                    if (trackedEntityAttributeValueToShowInReportEntity != null) {
                                        trackedEntityAttributeValuesToShowInReportEntity.put(programTrackedEntityAttribute.trackedEntityAttribute().uid(), trackedEntityAttributeValueToShowInReportEntity.value());
                                    }
                                }
                            }

                            trackedEntityAttributeValuesToShowInReportEntity.put(PROGRAM_KEY, program.displayName());
                            trackedEntityAttributeValuesToShowInReportEntity.put(program.enrollmentDateLabel(),
                                    SIMPLE_DATE_FORMAT.format(enrollment.dateOfEnrollment()));
                            trackedEntityAttributeValuesToShowInReportEntity.put(ReportEntityFilter.STATUS_KEY, enrollment.enrollmentStatus().toString());

                            ReportEntity reportEntity = new ReportEntity(enrollment.uid(), ReportEntity.Status.SENT, trackedEntityAttributeValuesToShowInReportEntity);
                            enrollmentReportEntities.add(reportEntity);
                        }

                        ReportEntityFilter reportEntityFilterStatus = new ReportEntityFilter(ReportEntityFilter.STATUS_KEY, ReportEntityFilter.STATUS_LABEL, true);
                        reportEntityFilters.add(reportEntityFilterStatus);

                        enrollmentView.updateReportEntityFilters(reportEntityFilters);
                        enrollmentView.drawEnrollments(enrollmentReportEntities);
                    }
                }));
    }

    @Override
    public void navigateToEnrollment(ReportEntity reportEntity, String trackedEntityInstanceUid) {
        String enrollmentUid = reportEntity.getId();
        Enrollment enrollment = enrollmentInteractor.store().query(enrollmentUid);
        enrollmentView.navigateToTeiDashboardWithEnrollment(enrollment.program(), enrollmentUid);
    }

    @Override
    public void createNewEnrollment(final String trackedEntityInstanceUid) {
        subscription.add(
                Observable.just(trackedEntityInstanceInteractor.store().
                        queryByUid(trackedEntityInstanceUid))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<TrackedEntityInstance>() {
                            @Override
                            public void call(TrackedEntityInstance trackedEntityInstance) {
                                if (enrollmentView != null) {
                                    enrollmentView.navigateToCreateNewEnrollment(
                                            trackedEntityInstance.trackedEntityUid(),
                                            trackedEntityInstance.uid());
                                }
                            }
                        }));

    }
}
