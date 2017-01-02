package org.hisp.dhis.android.app.views.drawerform.trackedentityinstance.drawer;

import org.hisp.dhis.client.sdk.ui.bindings.presenters.Presenter;

public interface TeiNavigationPresenter extends Presenter {
    void configureAppBar(String itemUid, String programUid);

    void onProfileClick();

    void createNewEvent(String programUid, String programStageUid, String orgUnitUid, String enrollmentUid);
}
