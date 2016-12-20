package org.hisp.dhis.android.app.views.create.identifiable;

import org.hisp.dhis.android.app.DataUtils;
import org.hisp.dhis.client.sdk.core.ModelUtils;
import org.hisp.dhis.client.sdk.core.enrollment.EnrollmentInteractor;
import org.hisp.dhis.client.sdk.core.event.EventInteractor;
import org.hisp.dhis.client.sdk.core.organisationunit.OrganisationUnitInteractor;
import org.hisp.dhis.client.sdk.core.program.ProgramInteractor;
import org.hisp.dhis.client.sdk.core.trackedentity.TrackedEntityInstanceInteractor;
import org.hisp.dhis.client.sdk.models.common.State;
import org.hisp.dhis.client.sdk.models.enrollment.Enrollment;
import org.hisp.dhis.client.sdk.models.enrollment.EnrollmentStatus;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.event.EventStatus;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramType;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.client.sdk.ui.bindings.commons.SessionPreferences;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.FormEntityFilter;
import org.hisp.dhis.client.sdk.ui.models.Picker;
import org.hisp.dhis.client.sdk.utils.CodeGenerator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CreateIdentifiableItemPresenterImpl implements CreateIdentifiableItemPresenter {


    private final OrganisationUnitInteractor organisationUnitInteractor;
    private final ProgramInteractor programInteractor;
    private final SessionPreferences sessionPreferences;
    private final TrackedEntityInstanceInteractor trackedEntityInstanceInteractor;
    private final EnrollmentInteractor enrollmentInteractor;
    private final EventInteractor eventInteractor;

    private CreateIdentifiableItemView createEventView;

    public CreateIdentifiableItemPresenterImpl(TrackedEntityInstanceInteractor trackedEntityInstanceInteractor,
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
        this.createEventView = (CreateIdentifiableItemView) view;
    }

    @Override
    public void detachView() {
        createEventView = null;
    }

    @Override
    public void drawViews(String trackedEntityUid) {

        FormEntityFilter programStageFormEntityFilter = new FormEntityFilter("ASDA", "What");
        Picker programPicker = new Picker.Builder()
                .hint("Create new for ")
                .build();
        List<Program> programs = programInteractor.store().query(ProgramType.WITH_REGISTRATION);
        List<Program> programsForTrackedEntity = new ArrayList<>();
        for (Program program : programs) {
            if (program.trackedEntity() != null && program.trackedEntity().uid().equals(trackedEntityUid)) {
                programsForTrackedEntity.add(program);
            }
        }


        for (Program program : programsForTrackedEntity) {
            Picker stagePicker = new Picker.Builder()
                    .id(program.uid())
                    .name(program.displayName())
                    .parent(programPicker)
                    .build();
            programPicker.addChild(stagePicker);
        }
        programStageFormEntityFilter.setPicker(programPicker);


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
    public void createItem(String orgUnitUid, String programUid, String trackedEntityUid) {
        TrackedEntityInstance.Builder trackedEntityInstanceBuilder = TrackedEntityInstance.builder()
                .uid(CodeGenerator.generateCode())
                .created(Calendar.getInstance().getTime())
                .state(State.TO_POST)
                .organisationUnit(orgUnitUid)
                .trackedEntityUid(trackedEntityUid);
        TrackedEntityInstance trackedEntityInstance = trackedEntityInstanceBuilder.build();


        Enrollment.Builder enrollmentBuilder = Enrollment.builder()
                .uid(CodeGenerator.generateCode())
                .program(programUid)
                .organisationUnit(orgUnitUid)
                .created(Calendar.getInstance().getTime())
                .dateOfEnrollment(Calendar.getInstance().getTime())
                .trackedEntityInstance(trackedEntityInstance.uid())
                .state(State.TO_POST)
                .followUp(false)
                .enrollmentStatus(EnrollmentStatus.ACTIVE);
        Enrollment enrollment = enrollmentBuilder.build();

        trackedEntityInstanceInteractor.store().save(trackedEntityInstance);
        enrollmentInteractor.store().save(enrollment);
        Program program = programInteractor.store().queryByUid(programUid);

        List<Event> eventsForEnrollment = DataUtils.createIdentifiableEventsForEnrollment(program,enrollment);

        if(eventsForEnrollment.size() > 0) {
            eventInteractor.store().save(eventsForEnrollment);
        }

        createEventView.itemCreated(enrollment.uid(), enrollment.program());
    }

    @Override
    public void createNewItemForInstance(String orgUnitUid, String programUid, String identifiableId) {
        Enrollment.Builder enrollmentBuilder = Enrollment.builder()
                .uid(CodeGenerator.generateCode())
                .program(programUid)
                .organisationUnit(orgUnitUid)
                .created(Calendar.getInstance().getTime())
                .dateOfEnrollment(Calendar.getInstance().getTime())
                .trackedEntityInstance(identifiableId)
                .state(State.TO_POST)
                .followUp(false)
                .enrollmentStatus(EnrollmentStatus.ACTIVE);
        Enrollment enrollment = enrollmentBuilder.build();
        enrollmentInteractor.store().save(enrollment);

        createEventView.itemCreated(enrollment.uid(), enrollment.program());
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
