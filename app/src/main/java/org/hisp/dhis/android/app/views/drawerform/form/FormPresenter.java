package org.hisp.dhis.android.app.views.drawerform.form;

import org.hisp.dhis.client.sdk.models.event.EventStatus;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.Presenter;
import org.hisp.dhis.client.sdk.ui.models.Form;
import org.hisp.dhis.client.sdk.ui.models.FormEntity;

import java.util.Date;
import java.util.List;

public interface FormPresenter extends Presenter {

    void buildForm(Form form);

    void saveEventDate(String eventUid, Date eventDate);

    void saveEnrollmentDate(String enrollmentUid, Date eventDate);

    void saveEventStatus(String eventUid, EventStatus eventStatus);

    void subscribeToLocations();

    void stopLocationUpdates();

    List<FormEntity> getInvalidFormEntities();
}