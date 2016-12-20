package org.hisp.dhis.android.app.views.dashboard;

import org.hisp.dhis.android.app.views.dashboard.trackedentityinstance.TeiDashboardPresenter;

public class NavigationLockControllerImpl implements NavigationLockController {

    private final TeiDashboardPresenter teiDashboardPresenter;

    public NavigationLockControllerImpl(TeiDashboardPresenter teiDashboardPresenter) {
        this.teiDashboardPresenter = teiDashboardPresenter;
    }

    @Override
    public void lockNavigation() {
        teiDashboardPresenter.lockNavigation();
    }

    @Override
    public void unlockNavigation() {
        teiDashboardPresenter.unlockNavigation();
        teiDashboardPresenter.showMenu();
    }
}