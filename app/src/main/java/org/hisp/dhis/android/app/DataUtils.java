package org.hisp.dhis.android.app;

import org.hisp.dhis.client.sdk.models.common.State;
import org.hisp.dhis.client.sdk.models.enrollment.Enrollment;
import org.hisp.dhis.client.sdk.models.enrollment.EnrollmentStatus;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.event.EventStatus;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.client.sdk.utils.CodeGenerator;
import org.hisp.dhis.client.sdk.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DataUtils {
    private DataUtils() {
        // no instances
    }

    public static Enrollment createEnrollment(OrganisationUnit organisationUnit, Program program, String trackedEntityInstanceUid, Date dateOfEnrollment) {
        return Enrollment.builder()
                .uid(CodeGenerator.generateCode())
                .created(Calendar.getInstance().getTime())
                .state(State.TO_POST)
                .dateOfEnrollment(dateOfEnrollment)
                .organisationUnit(organisationUnit.uid())
                .program(program.uid())
                .trackedEntityInstance(trackedEntityInstanceUid)
                .followUp(false)
                .enrollmentStatus(EnrollmentStatus.ACTIVE)
                .build();
    }

    public static Event createNonIdentifiableEvent(OrganisationUnit organisationUnit, Program program,
                                                   ProgramStage programStage, Date eventDate) {
//        isNull(organisationUnit)
        return Event.builder().uid(CodeGenerator.generateCode())
                .created(Calendar.getInstance().getTime())
                .state(State.TO_POST)
                .eventDate(eventDate)
                .organisationUnit(organisationUnit.uid())
                .program(program.uid())
                .programStage(programStage.uid())
                .status(EventStatus.ACTIVE).build();
    }

    public static List<Event> createIdentifiableEventsForEnrollment(Program program, Enrollment enrollment) {
        List<Event> events = new ArrayList<>();

        if (program.programStages() != null && !program.programStages().isEmpty()) {
            for (ProgramStage programStage : program.programStages()) {
                if (programStage.autoGenerateEvent() != null && programStage.autoGenerateEvent()) {
                    Event.Builder eventBuilder = Event.builder();

                    eventBuilder.uid(CodeGenerator.generateCode());
                    eventBuilder.created(Calendar.getInstance().getTime());
                    eventBuilder.state(State.TO_POST);
                    eventBuilder.program(enrollment.program());
                    eventBuilder.programStage(programStage.uid());
                    eventBuilder.enrollmentUid(enrollment.uid());
                    eventBuilder.organisationUnit(enrollment.organisationUnit());
                    eventBuilder.trackedEntityInstance(enrollment.trackedEntityInstance());
                    eventBuilder.dueDate(DateUtils.plusDays(enrollment.dateOfEnrollment(), programStage.minDaysFromStart() != null ? programStage.minDaysFromStart() : 0));
                    eventBuilder.status(EventStatus.SCHEDULE);

                    events.add(eventBuilder.build());
                }
            }
        }

        return events;
    }

    public static TrackedEntityInstance createTrackedEntityInstance(String organisationUnitId, Program program) {

        return TrackedEntityInstance.builder().uid(CodeGenerator.generateCode())
                .created(Calendar.getInstance().getTime())
                .organisationUnit(organisationUnitId)
                .trackedEntityUid(program.trackedEntity().uid())
                .state(State.TO_POST).build();
    }
}
