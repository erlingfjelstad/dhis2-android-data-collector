package org.hisp.dhis.android.app.views.dashboard.navigation.event.create;

import org.hisp.dhis.client.sdk.core.ModelUtils;
import org.hisp.dhis.client.sdk.core.enrollment.EnrollmentInteractor;
import org.hisp.dhis.client.sdk.core.event.EventInteractor;
import org.hisp.dhis.client.sdk.core.organisationunit.OrganisationUnitInteractor;
import org.hisp.dhis.client.sdk.core.program.ProgramInteractor;
import org.hisp.dhis.client.sdk.core.trackedentity.TrackedEntityInstanceInteractor;
import org.hisp.dhis.client.sdk.models.common.State;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.event.EventStatus;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.ui.bindings.commons.SessionPreferences;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.FormEntityFilter;
import org.hisp.dhis.client.sdk.ui.models.Picker;
import org.hisp.dhis.client.sdk.utils.CodeGenerator;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CreateEventPresenterImpl implements CreateEventPresenter {

    private final OrganisationUnitInteractor organisationUnitInteractor;
    private final ProgramInteractor programInteractor;
    private final SessionPreferences sessionPreferences;
    private final TrackedEntityInstanceInteractor trackedEntityInstanceInteractor;
    private final EnrollmentInteractor enrollmentInteractor;
    private final EventInteractor eventInteractor;

    private CreateEventView createEventView;

    public CreateEventPresenterImpl(TrackedEntityInstanceInteractor trackedEntityInstanceInteractor,
                                    OrganisationUnitInteractor organisationUnitInteractor,
                                    ProgramInteractor programInteractor,
                                    EnrollmentInteractor enrollmentInteractor,
                                    EventInteractor eventInteractor,
                                    SessionPreferences sessionPreferences) {
        this.trackedEntityInstanceInteractor = trackedEntityInstanceInteractor;
        this.organisationUnitInteractor = organisationUnitInteractor;
        this.programInteractor = programInteractor;
        this.enrollmentInteractor = enrollmentInteractor;
        this.eventInteractor = eventInteractor;
        this.sessionPreferences = sessionPreferences;
    }

    @Override
    public void attachView(View view) {
        this.createEventView = (CreateEventView) view;
    }

    @Override
    public void detachView() {
        createEventView = null;
    }

    @Override
    public void drawViews(String programUid) {

        FormEntityFilter programStageFormEntityFilter = new FormEntityFilter("ASDA", "What");
        Picker programStagesPicker = new Picker.Builder()
                .hint("Create event for ")
                .build();
        for (ProgramStage programStage : programInteractor.store().queryByUid(programUid).programStages()) {
            Picker stagePicker = new Picker.Builder()
                    .id(programStage.uid())
                    .name(programStage.displayName())
                    .parent(programStagesPicker)
                    .build();
            programStagesPicker.addChild(stagePicker);
        }
        programStageFormEntityFilter.setPicker(programStagesPicker);


        FormEntityFilter orgUnitFormEntity = new FormEntityFilter("ASDA", "Where");
        Picker orgUnitPicker = createPickerTree(organisationUnitInteractor.store().queryAll());
        if (orgUnitPicker.getChildren().size() == 1) {
            orgUnitFormEntity.setLocked(true);
        }
        orgUnitFormEntity.setPicker(orgUnitPicker);

        if (createEventView != null) {
            createEventView.drawViews(programStageFormEntityFilter, orgUnitFormEntity);
        }
    }

    @Override
    public void storeScheduledEvent(Date scheduledDate, String orgUnitUid, String programStageUid, String programUid, String enrollmentUid) {
        String trackedEntityInstance = enrollmentInteractor.store().query(enrollmentUid).trackedEntityInstance();

        Event.Builder builder = Event.builder()
                .uid(CodeGenerator.generateCode())
                .created(Calendar.getInstance().getTime())
                .state(State.TO_POST)
                .organisationUnit(orgUnitUid)
                .trackedEntityInstance(trackedEntityInstance)
                .dueDate(scheduledDate)
                .enrollmentUid(enrollmentUid)
                .program(programUid)
                .programStage(programStageUid)
                .status(EventStatus.SCHEDULE);

        Date eventDate = Calendar.getInstance().getTime();
        builder.eventDate(eventDate);

        Event event = builder.build();

        eventInteractor.store().save(event);
    }

    /*
     * Goes through given organisation units and programs and builds Picker tree
     */
    //TODO: Check and download authorities for userCredentials to see if user is allowed to data entry for the different ProgramTypes
    private Picker createPickerTree(List<OrganisationUnit> units) {
        Map<String, OrganisationUnit> organisationUnitMap = ModelUtils.toMap(units);

        Picker rootPicker = new Picker.Builder()
                .hint("Where")
                .build();

        for (String unitKey : organisationUnitMap.keySet()) {
            // creating organisation unit picker items
            OrganisationUnit organisationUnit = organisationUnitMap.get(unitKey);
            Picker organisationUnitPicker = new Picker.Builder()
                    .id(organisationUnit.uid())
                    .name(organisationUnit.displayName())
                    .hint("Where")
                    .parent(rootPicker)
                    .build();

            rootPicker.addChild(organisationUnitPicker);
        }

        // set saved selections or default ones:
        if (sessionPreferences.getSelectedPickerUid(0) != null) {
            traverseAndSetSavedSelection(rootPicker);
        } else {
            // Traverse the tree. If there is a path with nodes
            // which have only one child, set default selection
            traverseAndSetDefaultSelection(rootPicker);
        }

        return rootPicker;
    }

    private void traverseAndSetSavedSelection(Picker node) {
        int treeLevel = 0;
        while (node != null) {
            String pickerId = sessionPreferences.getSelectedPickerUid(treeLevel);
            if (pickerId != null) {
                for (Picker child : node.getChildren()) {

                    if (child.getId().equals(pickerId)) {
                        node.setSelectedChild(child);
                        break;
                    }
                }
            }
            treeLevel++;
            node = node.getSelectedChild();
        }
    }

    private static void traverseAndSetDefaultSelection(Picker tree) {
        if (tree != null) {

            Picker node = tree;
            do {
                if (node.getChildren().size() == 1) {
                    // get the only child node and set it as selected
                    Picker singleChild = node.getChildren().get(0);
                    node.setSelectedChild(singleChild);
                }
            } while ((node = node.getSelectedChild()) != null);
        }
    }
}
