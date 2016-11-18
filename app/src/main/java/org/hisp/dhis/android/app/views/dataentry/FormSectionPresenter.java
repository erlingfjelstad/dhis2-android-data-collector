package org.hisp.dhis.android.app.views.dataentry;

import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.event.EventStatus;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.Presenter;

import java.util.Date;

public interface FormSectionPresenter extends Presenter {
    void createDataEntryForm(String eventUid);

    void saveEventDate(String eventUid, Date eventDate);

    void saveEventStatus(String eventUid, EventStatus eventStatus);

    void subscribeToLocations();

    void stopLocationUpdates();
}