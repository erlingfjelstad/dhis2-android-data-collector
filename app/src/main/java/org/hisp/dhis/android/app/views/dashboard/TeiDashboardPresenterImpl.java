package org.hisp.dhis.android.app.views.dashboard;

import org.hisp.dhis.android.app.presenters.FormSectionPresenter;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;

public class TeiDashboardPresenterImpl implements TeiDashboardPresenter {

    private final String TAG = this.getClass().getSimpleName();

    TeiDashboardView dashBoardView;

    FormSectionPresenter formSectionPresenter;


    public TeiDashboardPresenterImpl() {
    }

    @Override
    public void hideMenu() {
        dashBoardView.closeDrawer();
    }

    @Override
    public void showDataEntryForEvent(String eventUid) {
        formSectionPresenter.createDataEntryForm(eventUid, "");
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
