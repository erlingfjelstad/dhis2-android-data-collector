package org.hisp.dhis.android.app.views.dashboard;

import org.hisp.dhis.android.app.presenters.FormSectionPresenter;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;

public class TeiDashboardPresenterImpl implements TeiDashboardPresenter {

    private final String TAG = this.getClass().getSimpleName();

    TeiDashboardView dashBoardView;

    FormSectionPresenter formSectionPresenter;

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
    public void showDataEntryForEvent(String eventUid) {
        hideMenu();
        formSectionPresenter.showDataEntryForm(eventUid, "", "");
    }

    @Override
    public void navigateToExistingItem(String eventUid, String programUid, String programStageUid) {
        hideMenu();
        formSectionPresenter.showDataEntryForm(eventUid, programUid, programStageUid);
    }

    @Override
    public void navigateToNewItem(String programUid, String programStageUid, String orgUnitUid, String enrollmentUid) {
        hideMenu();
        formSectionPresenter.createNewEvent(programUid, programStageUid, orgUnitUid, enrollmentUid);
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
