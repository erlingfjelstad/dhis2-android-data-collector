package org.hisp.dhis.android.app.views.selectedcontent;

import org.hisp.dhis.android.app.DataUtils;
import org.hisp.dhis.android.app.model.SyncWrapper;
import org.hisp.dhis.android.app.views.DashboardContextType;
import org.hisp.dhis.client.sdk.core.commons.ApiException;
import org.hisp.dhis.client.sdk.core.enrollment.EnrollmentInteractor;
import org.hisp.dhis.client.sdk.core.event.EventInteractor;
import org.hisp.dhis.client.sdk.core.organisationunit.OrganisationUnitInteractor;
import org.hisp.dhis.client.sdk.core.program.ProgramInteractor;
import org.hisp.dhis.client.sdk.core.trackedentity.TrackedEntityAttributeValueInteractor;
import org.hisp.dhis.client.sdk.core.trackedentity.TrackedEntityDataValueInteractor;
import org.hisp.dhis.client.sdk.core.trackedentity.TrackedEntityInstanceInteractor;
import org.hisp.dhis.client.sdk.models.enrollment.Enrollment;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.program.ProgramStageDataElement;
import org.hisp.dhis.client.sdk.models.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.client.sdk.models.program.ProgramType;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.client.sdk.ui.bindings.commons.ApiExceptionHandler;
import org.hisp.dhis.client.sdk.ui.bindings.commons.AppError;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.ContentEntity;
import org.hisp.dhis.client.sdk.ui.models.ReportEntity;
import org.hisp.dhis.client.sdk.ui.models.ReportEntityFilter;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

public class SelectedContentPresenterImpl implements SelectedContentPresenter {
    private static final String TAG = SelectedContentPresenterImpl.class.getSimpleName();
    private SelectedContentView selectedContentView;
    private final SyncWrapper syncWrapper;
    private final TrackedEntityInstanceInteractor trackedEntityInstanceInteractor;
    private final TrackedEntityAttributeValueInteractor trackedEntityAttributeValueInteractor;
    private final OrganisationUnitInteractor organisationUnitInteractor;
    private final ProgramInteractor programInteractor;
    private final ApiExceptionHandler apiExceptionHandler;
    private final Logger logger;
    private boolean isSyncing;
    private CompositeSubscription subscription;
    private final EventInteractor eventInteractor;
    private final TrackedEntityDataValueInteractor trackedEntityDataValueInteractor;
    private final EnrollmentInteractor enrollmentInteractor;

    public SelectedContentPresenterImpl(SyncWrapper syncWrapper,
                                        TrackedEntityInstanceInteractor trackedEntityInstanceInteractor,
                                        TrackedEntityAttributeValueInteractor trackedEntityAttributeValueInteractor,
                                        OrganisationUnitInteractor organisationUnitInteractor, ProgramInteractor programInteractor,
                                        ApiExceptionHandler apiExceptionHandler, Logger logger, EventInteractor eventInteractor, TrackedEntityDataValueInteractor trackedEntityDataValueInteractor, EnrollmentInteractor enrollmentInteractor) {
        this.syncWrapper = syncWrapper;
        this.trackedEntityInstanceInteractor = trackedEntityInstanceInteractor;
        this.trackedEntityAttributeValueInteractor = trackedEntityAttributeValueInteractor;
        this.organisationUnitInteractor = organisationUnitInteractor;
        this.programInteractor = programInteractor;
        this.apiExceptionHandler = apiExceptionHandler;
        this.logger = logger;
        this.eventInteractor = eventInteractor;
        this.trackedEntityDataValueInteractor = trackedEntityDataValueInteractor;
        this.enrollmentInteractor = enrollmentInteractor;
        this.subscription = new CompositeSubscription();
    }

    @Override
    public void attachView(View view) {
        isNull(view, "View must not be null");
        selectedContentView = (SelectedContentView) view;
    }

    @Override
    public void detachView() {
        selectedContentView = null;
    }

    @Override
    public void sync() throws IOException {
        selectedContentView.showProgressBar();
        isSyncing = true;
        subscription.add(syncWrapper.syncMetaData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Program>>() {
                    @Override
                    public void call(List<Program> programs) {
                        isSyncing = false;

                        if (selectedContentView != null) {
                            selectedContentView.hideProgressBar();
                        }

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        isSyncing = false;
                        if (selectedContentView != null) {
                            selectedContentView.hideProgressBar();
                        }
                        handleError(throwable);
                    }
                }));
    }

    @Override
    public void updateContents(final String contentId, String contentType) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }

        subscription = new CompositeSubscription();

        if (ContentEntity.TYPE_TRACKED_ENTITY.equals(contentType)) {
            subscription.add(getTrackedEntityInstancesByTrackedEntity(contentId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).map(new Func1<List<TrackedEntityInstance>, List<ReportEntity>>() {
                        @Override
                        public List<ReportEntity> call(List<TrackedEntityInstance> trackedEntityInstances) {
                            List<ReportEntity> reportEntities = new ArrayList<>();
                            if (trackedEntityInstances != null && !trackedEntityInstances.isEmpty())
                                for (TrackedEntityInstance trackedEntityInstance : trackedEntityInstances) {
                                    TrackedEntityInstance.Builder builder = trackedEntityInstance.toBuilder();
                                    builder.trackedEntityAttributeValues(trackedEntityAttributeValueInteractor.store().query(trackedEntityInstance.uid()));
                                    trackedEntityInstance = builder.build();

                                    reportEntities.add(new ReportEntity(trackedEntityInstance.uid(),
                                            ReportEntity.Status.valueOf(trackedEntityInstance.state().toString()),
                                            toMap(trackedEntityInstance.trackedEntityAttributeValues())));
                                }
                            return reportEntities;
                        }
                    }).subscribe(new Action1<List<ReportEntity>>() {
                        @Override
                        public void call(List<ReportEntity> reportEntities) {
                            if (selectedContentView != null) {
                                selectedContentView.showReportEntities(reportEntities);
                            }
                        }
                    }));
        } else if (ContentEntity.TYPE_PROGRAM.equals(contentType)) {
            subscription.add(Observable.just(eventInteractor.store().queryByProgram(contentId))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(new Func1<List<Event>, List<ReportEntity>>() {
                        @Override
                        public List<ReportEntity> call(List<Event> events) {
                            List<ReportEntity> reportEntities = new ArrayList<>();
                            if (events != null && !events.isEmpty()) {
                                for (Event event : events) {
                                    Event.Builder eventBuilder = event.toBuilder();
                                    eventBuilder.trackedEntityDataValues(trackedEntityDataValueInteractor.store().query(event.uid()));
                                    event = eventBuilder.build();
                                    reportEntities.add(new ReportEntity(event.uid(),
                                            ReportEntity.Status.valueOf(event.state().toString()),
                                            toEventDataValueMap(event.trackedEntityDataValues())));
                                }

                            }
                            return reportEntities;
                        }
                    }).subscribe(new Action1<List<ReportEntity>>() {
                        @Override
                        public void call(List<ReportEntity> reportEntities) {
                            if (selectedContentView != null) {
                                selectedContentView.showReportEntities(reportEntities);
                            }
                        }
                    }));
        }
    }

    @Override
    public void configureFilters(String contentId, String contentType) {

        switch (contentType) {
            case ContentEntity.TYPE_TRACKED_ENTITY: {

                subscription.add(Observable.just(programInteractor.store().queryAll())
                        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .map(new Func1<List<Program>, List<ReportEntityFilter>>() {
                            @Override
                            public List<ReportEntityFilter> call(List<Program> programs) {
                                List<ReportEntityFilter> reportEntityFilters = new ArrayList<>();
                                Map<String, ProgramTrackedEntityAttribute> programTrackedEntityAttributeMap = new HashMap<>();
                                for (Program program : programs) {
                                    if (program.programTrackedEntityAttributes() != null && !program.programTrackedEntityAttributes().isEmpty()) {
                                        List<ProgramTrackedEntityAttribute> programTrackedEntityAttributes = new ArrayList<>(program.programTrackedEntityAttributes());
                                        Collections.sort(programTrackedEntityAttributes, ProgramTrackedEntityAttribute.DESCENDING_SORT_ORDER_COMPARATOR);
                                        for (ProgramTrackedEntityAttribute programTrackedEntityAttribute : programTrackedEntityAttributes) {
                                            if (programTrackedEntityAttribute.displayInList() != null && programTrackedEntityAttribute.displayInList()) {
                                                programTrackedEntityAttributeMap.put(programTrackedEntityAttribute.trackedEntityAttribute().uid(), programTrackedEntityAttribute);
                                            }
                                        }
                                    }
                                }

                                for (ProgramTrackedEntityAttribute programTrackedEntityAttribute : programTrackedEntityAttributeMap.values()) {
                                    reportEntityFilters.add(new ReportEntityFilter(
                                            programTrackedEntityAttribute.trackedEntityAttribute().uid(),
                                            programTrackedEntityAttribute.trackedEntityAttribute().displayName(),
                                            true));

                                }

                                return reportEntityFilters;
                            }
                        }).subscribe(new Action1<List<ReportEntityFilter>>() {
                            @Override
                            public void call(List<ReportEntityFilter> reportEntityFilters) {
                                if (selectedContentView != null) {
                                    selectedContentView.notifyFiltersChanged(reportEntityFilters);
                                }
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                handleError(throwable);
                            }
                        }));
                break;
            }
            case ContentEntity.TYPE_PROGRAM: {
                subscription.add(Observable.just(programInteractor.store().queryByUid(contentId))
                        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .map(new Func1<Program, List<ReportEntityFilter>>() {
                            @Override
                            public List<ReportEntityFilter> call(Program program) {
                                List<ReportEntityFilter> reportEntityFilters = new ArrayList<>();
                                Map<String, ProgramStageDataElement> programStageDataElementMap = new HashMap<>();
                                ProgramStage singleStage = program.programStages().get(0);
                                if (singleStage != null && singleStage.programStageDataElements() != null && !singleStage.programStageDataElements().isEmpty()) {
                                    List<ProgramStageDataElement> programStageDataElements = new ArrayList<>(singleStage.programStageDataElements());
                                    Collections.sort(programStageDataElements, ProgramStageDataElement.DESCENDING_SORT_ORDER_COMPARATOR);
                                    for (ProgramStageDataElement programStageDataElement : programStageDataElements) {
                                        if (programStageDataElement.displayInReports() != null && programStageDataElement.displayInReports()) {
                                            programStageDataElementMap.put(programStageDataElement.dataElement().uid(), programStageDataElement);
                                        }
                                    }
                                }

                                for (ProgramStageDataElement programStageDataElement : programStageDataElementMap.values()) {
                                    reportEntityFilters.add(new ReportEntityFilter(
                                            programStageDataElement.dataElement().uid(),
                                            programStageDataElement.dataElement().displayName(),
                                            true));

                                }

                                return reportEntityFilters;
                            }
                        }).subscribe(new Action1<List<ReportEntityFilter>>() {
                            @Override
                            public void call(List<ReportEntityFilter> reportEntityFilters) {
                                if (selectedContentView != null) {
                                    selectedContentView.notifyFiltersChanged(reportEntityFilters);
                                }
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                handleError(throwable);
                            }
                        }));
                break;
            }
            default:
                break;
        }


    }

    @Override
    public void configureFloatingActionMenu(final String trackedEntityUid) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }

        subscription = new CompositeSubscription();

        subscription.add(Observable.just(programInteractor.store().query(ProgramType.WITH_REGISTRATION))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).map(new Func1<List<Program>, List<ContentEntity>>() {
                    @Override
                    public List<ContentEntity> call(List<Program> programs) {
                        List<ContentEntity> contentEntities = new ArrayList<>();

                        if (programs != null && !programs.isEmpty()) {
                            for (Program program : programs) {
                                if (program.trackedEntity().uid().equals(trackedEntityUid)) {
                                    contentEntities.add(new ContentEntity(program.uid(), program.displayName(), ContentEntity.TYPE_PROGRAM));

                                }
                            }
                        }
                        return contentEntities;
                    }
                }).subscribe(new Action1<List<ContentEntity>>() {
                    @Override
                    public void call(List<ContentEntity> contentEntities) {
                        if (selectedContentView != null) {
                            selectedContentView.setActionsToFab(contentEntities);
                        }
                    }
                }));

    }

    @Override
    public void navigate(final String contentId, final String contentTitle, final String contentType) {
        subscription.add(Observable.just(organisationUnitInteractor.store().queryAll()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<List<OrganisationUnit>>() {
                    @Override
                    public void call(List<OrganisationUnit> organisationUnits) {
                        if (organisationUnits != null && organisationUnits.size() > 1) {
                            if (selectedContentView != null) {
                                selectedContentView.navigateTo(contentId, contentTitle);
                            }
                        } else {
                            if (selectedContentView != null) {
                                // Only one org unit exist - create event and navigate to FormSectionActivity
                                if (organisationUnits != null && !organisationUnits.isEmpty()) {
                                    switch (contentType) {
                                        case ContentEntity.TYPE_PROGRAM: {
                                            Program program = programInteractor.store().queryByUid(contentId);
                                            if (program.programStages() != null && !program.programStages().isEmpty()) {
                                                ProgramStage programStage = program.programStages().get(0);
                                                Event event = DataUtils.createNonIdentifiableEvent(organisationUnits.get(0),
                                                        program, programStage, Calendar.getInstance().getTime());


                                                eventInteractor.store().save(event);

                                                selectedContentView.navigateToFormSectionActivity(
                                                        event.uid(), program.uid(),
                                                        programStage.uid(), DashboardContextType.EXISTING_ITEM);
                                                break;
                                            }
                                        }
                                        case ContentEntity.TYPE_TRACKED_ENTITY: {
                                            List<Program> programs = programInteractor.store().query(ProgramType.WITH_REGISTRATION);
                                            List<Program> programsForTrackedEntity = new ArrayList<>();
                                            for (Program program : programs) {
                                                if (program.trackedEntity() != null && program.trackedEntity().uid().equals(contentId)) {
                                                    programsForTrackedEntity.add(program);
                                                }
                                            }
                                            // More than one program exists, navigating to CreateItemActivity
                                            if (programsForTrackedEntity.size() > 1) {
                                                selectedContentView.navigateTo(contentId, contentTitle);
                                            }
                                            // Only one orgUnit and one program exists.
                                            // Create enrollment and move to FormSectionActivity
                                            else {
                                                OrganisationUnit organisationUnit = organisationUnits.get(0);


                                                Program program = programsForTrackedEntity.get(0);
                                                TrackedEntityInstance trackedEntityInstance =
                                                        DataUtils.createTrackedEntityInstance(organisationUnit.uid(), program);

                                                trackedEntityInstanceInteractor.store().save(trackedEntityInstance);

                                                Enrollment enrollment = DataUtils.createEnrollment(
                                                        organisationUnits.get(0),
                                                        program, trackedEntityInstance.uid(), Calendar.getInstance().getTime());

                                                enrollmentInteractor.store().save(enrollment);
                                                List<Event> eventsForEnrollment =
                                                        DataUtils.createIdentifiableEventsForEnrollment(program, enrollment);

                                                if (eventsForEnrollment.size() > 0) {
                                                    eventInteractor.store().save(eventsForEnrollment);
                                                }

                                            }

                                            break;
                                        }
                                        case ContentEntity.TYPE_DATA_SET: {
                                            break;
                                        }

                                        default:
                                            break;
                                    }

                                }
                            }
                        }
                    }
                }));

    }

    @Override
    public void deleteItem(ReportEntity reportEntity, String contentType) {
        switch (contentType) {
            case ContentEntity.TYPE_TRACKED_ENTITY: {

                break;
            }
            case ContentEntity.TYPE_PROGRAM: {

                break;
            }
        }
    }

    private Map<String, String> toMap(List<TrackedEntityAttributeValue> trackedEntityAttributeValues) {
        Map<String, String> map = new HashMap<>();
        for (TrackedEntityAttributeValue trackedEntityAttributeValue : trackedEntityAttributeValues) {
            map.put(trackedEntityAttributeValue.trackedEntityAttributeUid(), trackedEntityAttributeValue.value());
        }
        return map;
    }

    private Map<String, String> toEventDataValueMap(List<TrackedEntityDataValue> trackedEntityDataValues) {
        Map<String, String> map = new HashMap<>();
        for (TrackedEntityDataValue trackedEntityAttributeValue : trackedEntityDataValues) {
            map.put(trackedEntityAttributeValue.dataElement(), trackedEntityAttributeValue.value());
        }
        return map;
    }

    @Override
    public void handleError(final Throwable throwable) {
        AppError error = apiExceptionHandler.handleException(TAG, throwable);

        if (throwable instanceof ApiException) {
            ApiException exception = (ApiException) throwable;

            if (exception.getResponse() != null) {
                switch (exception.getResponse().code()) {
                    case HttpURLConnection.HTTP_UNAUTHORIZED: {
                        selectedContentView.showError(error.getDescription());
                        break;
                    }
                    case HttpURLConnection.HTTP_NOT_FOUND: {
                        selectedContentView.showError(error.getDescription());
                        break;
                    }
                    default: {
                        selectedContentView.showUnexpectedError(error.getDescription());
                        break;
                    }
                }
            }
        } else {
            logger.e(TAG, "handleError", throwable);
        }
    }

    private Observable<List<TrackedEntityInstance>> getTrackedEntityInstancesByTrackedEntity(String trackedEntityUid) {
        return Observable.just(trackedEntityInstanceInteractor.store().queryByTrackedEntityUid(trackedEntityUid));
    }
}
