package org.hisp.dhis.android.app.views.drawerform.trackedentityinstance.drawer.event;

import org.hisp.dhis.client.sdk.ui.bindings.presenters.Presenter;

public interface TeiProgramStagePresenter extends Presenter {

    void drawProgramStages(String enrollmentUid, String programUid);

    void showEventForm(String eventUid);
}
