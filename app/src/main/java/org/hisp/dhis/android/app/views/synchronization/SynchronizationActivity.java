package org.hisp.dhis.android.app.views.synchronization;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import org.hisp.dhis.android.app.R;
import org.hisp.dhis.android.app.SkeletonApp;
import org.hisp.dhis.client.sdk.ui.activities.AbsSynchronizeActivity;
import org.hisp.dhis.client.sdk.ui.bindings.commons.NavigationHandler;

import javax.inject.Inject;

public class SynchronizationActivity extends AbsSynchronizeActivity implements SynchronizationView {

    @Inject
    SynchronizationPresenter synchronizationPresenter;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        ((SkeletonApp) getApplication()).getUserComponent().inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        synchronizationPresenter.attachView(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        synchronizationPresenter.detachView();
    }

    @Override
    public void onBackPressed() {
        // do nothing
    }

    @Override
    public void showProgress() {
        super.showProgress(getString(R.string.setting_up));
    }

    @Override
    public void hideProgress() {
        super.hideProgress();
    }

    @Override
    public void onSyncFinish() {
        navigateTo(NavigationHandler.homeActivity());
    }

    @Override
    public void showError(String message) {
        showErrorDialog(getString(R.string.title_error), message);
    }

    @Override
    public void showUnexpectedError(String message) {
        showErrorDialog(getString(R.string.title_error_unexpected), message);
    }

    private void showErrorDialog(String title, String message) {
        if (alertDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton(R.string.option_confirm, null);
            alertDialog = builder.create();
        }
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.show();
    }

}
