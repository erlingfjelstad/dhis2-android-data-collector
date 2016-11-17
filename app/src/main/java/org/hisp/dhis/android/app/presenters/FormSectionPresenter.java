package org.hisp.dhis.android.app.presenters;

import org.hisp.dhis.client.sdk.models.event.EventStatus;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.Presenter;

import java.util.Date;

public interface FormSectionPresenter extends Presenter {
    void createDataEntryForm(String itemUid, String programUid);

    void saveEventDate(String eventUid, Date eventDate);

    void saveEnrollmentDate(String enrollmentUid, Date eventDate);

    void saveEventStatus(String eventUid, EventStatus eventStatus);

    void subscribeToLocations();

    void stopLocationUpdates();
}
