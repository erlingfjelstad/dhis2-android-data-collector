package org.hisp.dhis.android.app.views.dashboard.navigation;

import org.hisp.dhis.client.sdk.ui.bindings.presenters.Presenter;

public interface TeiNavigationPresenter extends Presenter {
    void configureAppBar(String itemUid, String programUid);

    void onProfileClick();

    void showDataEntry(String eventUid, String programUid, String programStageUid);

    void createNewEvent(String programUid, String programStageUid, String orgUnitUid, String enrollmentUid);
}
