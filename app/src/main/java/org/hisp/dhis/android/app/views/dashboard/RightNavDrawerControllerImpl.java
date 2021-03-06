package org.hisp.dhis.android.app.views.dashboard;

public class RightNavDrawerControllerImpl implements RightNavDrawerController {

    private final TeiDashboardPresenter teiDashboardPresenter;

    public RightNavDrawerControllerImpl(TeiDashboardPresenter teiDashboardPresenter) {
        this.teiDashboardPresenter = teiDashboardPresenter;
    }

    @Override
    public void showMenu() {
        teiDashboardPresenter.showMenu();
    }

    @Override
    public void hideMenu() {
        teiDashboardPresenter.hideMenu();
    }
}