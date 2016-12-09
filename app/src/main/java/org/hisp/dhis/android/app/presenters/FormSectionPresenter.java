package org.hisp.dhis.android.app.presenters;

import org.hisp.dhis.client.sdk.models.event.EventStatus;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.Presenter;
import org.hisp.dhis.client.sdk.ui.models.FormEntity;

import java.util.Date;
import java.util.List;

public interface FormSectionPresenter extends Presenter {

    void createNewEvent(String programUid, String programStageUid, String orgUnitUid, String enrollmentUid);

    void saveEventDate(String eventUid, Date eventDate);

    void saveEnrollmentDate(String enrollmentUid, Date eventDate);

    void saveEventStatus(String eventUid, EventStatus eventStatus);

    void subscribeToLocations();

    void stopLocationUpdates();

    List<FormEntity> getInvalidFormEntities();

    void showMenu();

    void showDataEntryForm(String eventUid, String programUid, String programStageUid);
}
