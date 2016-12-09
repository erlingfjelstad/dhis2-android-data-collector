package org.hisp.dhis.android.app.views.dashboard;

import org.hisp.dhis.client.sdk.ui.bindings.presenters.Presenter;

public interface TeiDashboardPresenter extends Presenter {

    void hideMenu();

    void showMenu();

    void showDataEntryForEvent(String eventid);

    void navigateToExistingItem(String eventUid, String programUid, String programStageUid);

    void navigateToNewItem(String programUid, String programStageUid,
                           String orgUnitUid, String enrollmentUid);
}
