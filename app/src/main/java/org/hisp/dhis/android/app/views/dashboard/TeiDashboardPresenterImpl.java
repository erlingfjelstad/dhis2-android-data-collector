package org.hisp.dhis.android.app.views.dashboard;

import org.hisp.dhis.android.app.presenters.FormSectionPresenter;
import org.hisp.dhis.android.app.views.dashboard.navigation.TeiNavigationPresenter;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.Form;

public class TeiDashboardPresenterImpl implements TeiDashboardPresenter {

    private final String TAG = this.getClass().getSimpleName();

    TeiDashboardView dashBoardView;

    FormSectionPresenter formSectionPresenter;
    TeiNavigationPresenter teiNavigationPresenter;

    public TeiDashboardPresenterImpl(FormSectionPresenter formSectionPresenter) {
        this.formSectionPresenter = formSectionPresenter;
    }

    @Override
    public void hideMenu() {
        if (dashBoardView != null) {
            dashBoardView.closeDrawer();
        }
    }

    @Override
    public void showMenu() {
        if (dashBoardView != null) {
            dashBoardView.openDrawer();
        }
    }

    @Override
    public void showForm(Form form) {
        hideMenu();
        formSectionPresenter.buildForm(form);
    }

    @Override
    public void refreshMenuButtonVisibility(boolean showMenuButtons) {
        formSectionPresenter.refreshMenuButtonVisibility(showMenuButtons);
    }

    @Override
    public void hideMenuButtons() {
        formSectionPresenter.refreshMenuButtonVisibility(false);
    }

    @Override
    public void showMenuButtons() {
        formSectionPresenter.refreshMenuButtonVisibility(true);
    }

    @Override
    public void attachView(View view) {
        dashBoardView = (TeiDashboardView) view;
    }

    @Override
    public void detachView() {
        dashBoardView = null;
    }


}
