package org.hisp.dhis.android.app.views;

import android.location.Location;
import android.support.annotation.StringDef;

import org.hisp.dhis.client.sdk.models.enrollment.EnrollmentStatus;
import org.hisp.dhis.client.sdk.models.event.EventStatus;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.FormEntity;
import org.hisp.dhis.client.sdk.ui.models.FormSection;
import org.hisp.dhis.client.sdk.ui.models.Picker;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public interface FormSectionView extends View {
    String ID_CHOOSE_SECTION = "chooseSection";

    /**
     * Should be called in cases when ProgramStage
     * does not contain any explicit sections
     */
    void showFormDefaultSection(String formSectionId, String programUid, String programStageUid);

    /**
     * Tells view to render form sections
     *
     * @param formSections    List of FormSections
     * @param programUid
     * @param programStageUid
     */
    void showFormSections(List<FormSection> formSections, String programUid, String programStageUid);

    void setFormSectionsPicker(Picker picker);

    void showReportDatePicker(String hint, String value);

    void showCoordinatesPicker(String latitude, String longitude);

    void showFormTitle(String formTitle);

    void showEventStatus(EventStatus eventStatus);

    void showEnrollmentStatus(EnrollmentStatus enrollmentStatus);

    String getFormSectionLabel(@FormSectionLabelId String formSectionLabel);

    List<FormEntity> getInvalidFormEntities();

    void setEventUid(String eventUid);

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            ID_CHOOSE_SECTION
    })
    @interface FormSectionLabelId {
    }

    void setLocation(Location location);

    void setLocationButtonState(boolean enabled);
}
