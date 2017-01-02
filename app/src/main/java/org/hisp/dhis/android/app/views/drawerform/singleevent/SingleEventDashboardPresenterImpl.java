package org.hisp.dhis.android.app.views.drawerform.singleevent;

import org.hisp.dhis.android.app.views.drawerform.RightDrawerController;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;

public class SingleEventDashboardPresenterImpl implements SingleEventDashboardPresenter, RightDrawerController {


    private SingleEventDashboardView singleEventDashboardView;

    public SingleEventDashboardPresenterImpl() {
    }

    @Override
    public void showMenu() {
        singleEventDashboardView.openDrawer();
    }

    @Override
    public void hideMenu() {
        singleEventDashboardView.closeDrawer();
    }

    @Override
    public void attachView(View view) {
        singleEventDashboardView = (SingleEventDashboardView) view;
    }

    @Override
    public void detachView() {
        singleEventDashboardView = null;
    }
}
