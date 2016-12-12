package org.hisp.dhis.android.app.views.selectedcontent;

import org.hisp.dhis.android.app.model.SyncWrapper;
import org.hisp.dhis.client.sdk.core.commons.ApiException;
import org.hisp.dhis.client.sdk.core.program.ProgramInteractor;
import org.hisp.dhis.client.sdk.core.trackedentity.TrackedEntityAttributeValueInteractor;
import org.hisp.dhis.client.sdk.core.trackedentity.TrackedEntityInstanceInteractor;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.client.sdk.ui.bindings.commons.ApiExceptionHandler;
import org.hisp.dhis.client.sdk.ui.bindings.commons.AppError;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.ReportEntity;
import org.hisp.dhis.client.sdk.ui.models.ReportEntityFilter;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
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
    private final ProgramInteractor programInteractor;
    private final ApiExceptionHandler apiExceptionHandler;
    private final Logger logger;
    private boolean isSyncing;
    private CompositeSubscription subscription;

    public SelectedContentPresenterImpl(SyncWrapper syncWrapper,
                                        TrackedEntityInstanceInteractor trackedEntityInstanceInteractor,
                                        TrackedEntityAttributeValueInteractor trackedEntityAttributeValueInteractor,
                                        ProgramInteractor programInteractor,
                                        ApiExceptionHandler apiExceptionHandler, Logger logger) {
        this.syncWrapper = syncWrapper;
        this.trackedEntityInstanceInteractor = trackedEntityInstanceInteractor;
        this.trackedEntityAttributeValueInteractor = trackedEntityAttributeValueInteractor;
        this.programInteractor = programInteractor;
        this.apiExceptionHandler = apiExceptionHandler;
        this.logger = logger;
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
    public void updateContents(final String trackedEntityUid) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }

        subscription = new CompositeSubscription();

        subscription.add(getTrackedEntityInstancesByTrackedEntity(trackedEntityUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).map(new Func1<List<TrackedEntityInstance>, List<ReportEntity>>() {
                    @Override
                    public List<ReportEntity> call(List<TrackedEntityInstance> trackedEntityInstances) {
                        List<ReportEntity> reportEntities = new ArrayList<>();
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
    }

    @Override
    public void configureFilters() {

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


    }

    private Map<String, String> toMap(List<TrackedEntityAttributeValue> trackedEntityAttributeValues) {
        Map<String, String> map = new HashMap<>();
        for (TrackedEntityAttributeValue trackedEntityAttributeValue : trackedEntityAttributeValues) {
            map.put(trackedEntityAttributeValue.trackedEntityAttributeUid(), trackedEntityAttributeValue.value());
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
