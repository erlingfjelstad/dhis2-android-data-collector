package org.hisp.dhis.android.app.views.create.event;

import org.hisp.dhis.client.sdk.ui.bindings.presenters.Presenter;

import java.util.Date;

public interface CreateEventPresenter extends Presenter {
    void drawViews(String programUid);

    void storeScheduledEvent(Date scheduledDate, String orgUnitUid, String programStageUid, String programUid, String enrollmentUid);
}
