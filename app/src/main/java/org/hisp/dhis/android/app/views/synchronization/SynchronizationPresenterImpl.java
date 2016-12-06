package org.hisp.dhis.android.app.views.synchronization;

import org.hisp.dhis.android.app.model.SyncWrapper;
import org.hisp.dhis.client.sdk.core.commons.ApiException;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.ui.bindings.commons.ApiExceptionHandler;
import org.hisp.dhis.client.sdk.ui.bindings.commons.AppError;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class SynchronizationPresenterImpl implements SynchronizationPresenter {

    private static final String TAG = SynchronizationPresenterImpl.class.getSimpleName();
    private SynchronizationView synchronizationView;
    private boolean isSyncing;
    private CompositeSubscription subscription;
    private final SyncWrapper syncWrapper;
    private final ApiExceptionHandler apiExceptionHandler;
    private final Logger logger;

    public SynchronizationPresenterImpl(SyncWrapper syncWrapper, ApiExceptionHandler apiExceptionHandler,
                                        Logger logger) {
        this.syncWrapper = syncWrapper;
        this.apiExceptionHandler = apiExceptionHandler;
        this.logger = logger;
        subscription = new CompositeSubscription();
    }

    @Override
    public void attachView(View view) {
        synchronizationView = (SynchronizationView) view;

        if (isSyncing) {
            synchronizationView.showProgress();
        } else {
            synchronizationView.hideProgress();
        }

        if (!isSyncing) {
            try {
                sync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void sync() throws IOException {
        synchronizationView.showProgress();
        isSyncing = true;

        subscription.add(syncWrapper.syncMetaData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Program>>() {
                    @Override
                    public void call(List<Program> programs) {
                        isSyncing = false;

                        if (synchronizationView != null) {
                            synchronizationView.hideProgress();
                            synchronizationView.onSyncFinish();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        isSyncing = false;

                        if (synchronizationView != null) {
                            synchronizationView.hideProgress();
                        }
                        handleError(throwable);
                    }
                }));
    }

    @Override
    public void detachView() {
        synchronizationView = null;
    }

    @Override
    public void handleError(final Throwable throwable) {
        AppError error = apiExceptionHandler.handleException(TAG, throwable);

        if (throwable instanceof ApiException) {
            ApiException exception = (ApiException) throwable;

            if (exception.getResponse() != null) {
                switch (exception.getResponse().code()) {
                    case HttpURLConnection.HTTP_UNAUTHORIZED: {
                        synchronizationView.showError(error.getDescription());
                        break;
                    }
                    case HttpURLConnection.HTTP_NOT_FOUND: {
                        synchronizationView.showError(error.getDescription());
                        break;
                    }
                    default: {
                        synchronizationView.showUnexpectedError(error.getDescription());
                        break;
                    }
                }
            }
        } else {
            logger.e(TAG, "handleError", throwable);
        }
    }
}
