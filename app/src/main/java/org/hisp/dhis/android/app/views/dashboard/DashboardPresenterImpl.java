package org.hisp.dhis.android.app.views.dashboard;

import org.hisp.dhis.android.app.model.SyncWrapper;
import org.hisp.dhis.client.sdk.core.commons.ApiException;
import org.hisp.dhis.client.sdk.core.program.ProgramInteractor;
import org.hisp.dhis.client.sdk.core.trackedentity.TrackedEntityInteractor;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramType;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntity;
import org.hisp.dhis.client.sdk.ui.bindings.commons.ApiExceptionHandler;
import org.hisp.dhis.client.sdk.ui.bindings.commons.AppError;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.ContentEntity;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

public class DashboardPresenterImpl implements DashboardPresenter {

    private static final String TAG = DashboardPresenterImpl.class.getSimpleName();
    private DashboardView dashboardView;
    private final TrackedEntityInteractor trackedEntityInteractor;
    private final ProgramInteractor programInteractor;
    private final SyncWrapper syncWrapper;
    private CompositeSubscription subscription;
    private boolean isSyncing;
    private boolean hasSyncedBefore;
    private final ApiExceptionHandler apiExceptionHandler;
    private final Logger logger;

    public DashboardPresenterImpl(TrackedEntityInteractor trackedEntityInteractor,
                                  ProgramInteractor programInteractor, SyncWrapper syncWrapper,
                                  ApiExceptionHandler apiExceptionHandler, Logger logger) {
        this.trackedEntityInteractor = trackedEntityInteractor;
        this.programInteractor = programInteractor;
        this.syncWrapper = syncWrapper;
        this.apiExceptionHandler = apiExceptionHandler;
        this.logger = logger;

        this.subscription = new CompositeSubscription();
    }

    @Override
    public void attachView(View view) {
        isNull(view, "DashboardView must not be null");
        dashboardView = (DashboardView) view;


        if (isSyncing) {
            dashboardView.showProgressBar();
        } else {
            dashboardView.hideProgressBar();
        }

        // check if metadata was synced,
        // if not, sync it
        if (!isSyncing && !hasSyncedBefore) {
            try {
                sync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void sync() throws IOException {
        dashboardView.showProgressBar();
        isSyncing = true;
        subscription.add(syncWrapper.syncMetaData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Program>>() {
                    @Override
                    public void call(List<Program> programs) {
                        isSyncing = false;
                        hasSyncedBefore = true;

                        if (dashboardView != null) {
                            dashboardView.hideProgressBar();
                        }
                        populateDashboard();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        isSyncing = false;
                        hasSyncedBefore = true;
                        if (dashboardView != null) {
                            dashboardView.hideProgressBar();
                        }
                        handleError(throwable);
                    }
                }));
    }

    @Override
    public void handleError(final Throwable throwable) {
        AppError error = apiExceptionHandler.handleException(TAG, throwable);

        if (throwable instanceof ApiException) {
            ApiException exception = (ApiException) throwable;

            if (exception.getResponse() != null) {
                switch (exception.getResponse().code()) {
                    case HttpURLConnection.HTTP_UNAUTHORIZED: {
                        dashboardView.showError(error.getDescription());
                        break;
                    }
                    case HttpURLConnection.HTTP_NOT_FOUND: {
                        dashboardView.showError(error.getDescription());
                        break;
                    }
                    default: {
                        dashboardView.showUnexpectedError(error.getDescription());
                        break;
                    }
                }
            }
        } else {
            logger.e(TAG, "handleError", throwable);
        }
    }

    @Override
    public void detachView() {
        dashboardView = null;
    }

    @Override
    public void populateDashboard() {
//        subscription.add(Observable.just(trackedEntityInteractor.store().queryAll())
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .zipWith(programInteractor.store().queryAll(), new Func2<List<TrackedEntity>, List<Program>, List<ReportEntity>>() {
//                    @Override
//                    public List<ReportEntity> call(List<TrackedEntity> trackedEntities, List<Program> programs) {
//                        return null;
//                    }
//                }).subscribe(new Action1<List<ReportEntity>>() {
//                    @Override
//                    public void call(List<ReportEntity> reportEntities) {
//
//                    }
//                })

        subscription.add(Observable.just(programInteractor.store().queryAll()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Program>>() {
                    @Override
                    public void call(List<Program> programs) {
                        Map<String, TrackedEntity> trackedEntityUids = new HashMap<>();
                        Map<String, Program> programUids = new HashMap<>();

                        for (Program program : programs) {
                            if (program.programType() != null && ProgramType.WITH_REGISTRATION.equals(program.programType())) {
                                if (program.trackedEntity() != null) {
                                    trackedEntityUids.put(program.trackedEntity().uid(), program.trackedEntity());
                                }
                            } else if (program.programType() != null && ProgramType.WITHOUT_REGISTRATION.equals(program.programType())) {
                                programUids.put(program.uid(), program);
                            }
                        }
                        List<ContentEntity> contentEntities = new ArrayList<>();
                        for (TrackedEntity trackedEntity : trackedEntityUids.values()) {
                            TrackedEntity trackedEntityPojo = trackedEntityInteractor.store().queryByUid(trackedEntity.uid());
                            contentEntities.add(new ContentEntity(trackedEntityPojo.uid(), trackedEntityPojo.displayName(), ContentEntity.TYPE_TRACKED_ENTITY));
                        }
                        for (Program program : programUids.values()) {
                            contentEntities.add(new ContentEntity(program.uid(), program.displayName(), ContentEntity.TYPE_PROGRAM));
                        }

                        if (dashboardView != null) {
                            dashboardView.swapData(contentEntities);
                        }

                    }
                }));
    }
}
