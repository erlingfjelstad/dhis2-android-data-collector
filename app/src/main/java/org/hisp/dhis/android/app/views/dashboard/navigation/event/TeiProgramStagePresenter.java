package org.hisp.dhis.android.app.views.dashboard.navigation.event;

import org.hisp.dhis.client.sdk.ui.bindings.presenters.Presenter;

public interface TeiProgramStagePresenter extends Presenter {

    void drawProgramStages(String enrollmentUid);

    void onEventClicked(String eventUid);
}
