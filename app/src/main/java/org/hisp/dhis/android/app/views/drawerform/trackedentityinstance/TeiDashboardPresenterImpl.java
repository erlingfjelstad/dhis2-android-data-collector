package org.hisp.dhis.android.app.views.drawerform.trackedentityinstance;

import org.hisp.dhis.android.app.views.drawerform.form.FormPresenter;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.Form;

public class TeiDashboardPresenterImpl implements TeiDashboardPresenter {

    private final String TAG = this.getClass().getSimpleName();

    TeiDashboardView teiDashboardView;

    FormPresenter formPresenter;

    public TeiDashboardPresenterImpl(FormPresenter formPresenter) {
        this.formPresenter = formPresenter;
    }

    @Override
    public void hideMenu() {
        if (teiDashboardView != null) {
            teiDashboardView.closeDrawer();
        }
    }

    @Override
    public void showMenu() {
        if (teiDashboardView != null) {
            teiDashboardView.openDrawer();
        }
    }

    @Override
    public void showForm(Form form) {
        hideMenu();
        formPresenter.buildForm(form);
    }

    @Override
    public void lockNavigation() {

    }

    @Override
    public void unlockNavigation() {
        teiDashboardView.setRegistrationComplete();
    }

    @Override
    public void attachView(View view) {
        teiDashboardView = (TeiDashboardView) view;
    }

    @Override
    public void detachView() {
        teiDashboardView = null;
    }
}
