package org.hisp.dhis.android.app.views.drawerform;

import org.hisp.dhis.android.app.views.drawerform.trackedentityinstance.TeiDashboardPresenter;

public class NavigationLockControllerImpl implements NavigationLockController {

    private final TeiDashboardPresenter teiDashboardPresenter;

    public NavigationLockControllerImpl(TeiDashboardPresenter teiDashboardPresenter) {
        this.teiDashboardPresenter = teiDashboardPresenter;
    }

    @Override
    public void lockNavigation() {
        if (teiDashboardPresenter != null) {
            teiDashboardPresenter.lockNavigation();
        }
    }

    @Override
    public void unlockNavigation() {
        if (teiDashboardPresenter != null) {
            teiDashboardPresenter.unlockNavigation();
            teiDashboardPresenter.showMenu();
        }
    }
}