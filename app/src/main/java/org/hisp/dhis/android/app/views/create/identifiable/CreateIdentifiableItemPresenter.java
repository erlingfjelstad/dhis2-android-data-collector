package org.hisp.dhis.android.app.views.create.identifiable;

import org.hisp.dhis.client.sdk.ui.bindings.presenters.Presenter;

import java.util.Date;

public interface CreateIdentifiableItemPresenter extends Presenter {
    void storeScheduledEvent(Date scheduledDate, String orgUnitUid, String programStageUid, String programUid, String enrollmentUid);

    void drawViews(String programUid);

    void createItem(String orgUnitUid, String programUid, String contentId);

    void createNewItemForInstance(String orgUnitUid, String programUid, String identifiableId);
}
