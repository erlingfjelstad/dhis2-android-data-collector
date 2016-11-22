package org.hisp.dhis.android.app.views.dashboard.navigation.event;

import org.hisp.dhis.android.app.views.dashboard.TeiDashboardPresenter;
import org.hisp.dhis.client.sdk.core.event.EventInteractor;
import org.hisp.dhis.client.sdk.core.program.ProgramInteractor;
import org.hisp.dhis.client.sdk.models.common.State;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.ExpansionPanel;
import org.hisp.dhis.client.sdk.ui.models.ReportEntity;
import org.hisp.dhis.client.sdk.ui.models.ReportEntityFilter;
import org.hisp.dhis.client.sdk.utils.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class TeiProgramStagePresenterImpl implements TeiProgramStagePresenter {
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private final TeiDashboardPresenter teiDashboardPresenter;
    private TeiProgramStageView teiProgramStageView;
    private CompositeSubscription subscription;
    private final ProgramInteractor programInteractor;
    private final EventInteractor eventInteractor;

    public TeiProgramStagePresenterImpl(TeiDashboardPresenter teiDashboardPresenter,
                                        ProgramInteractor programInteractor,
                                        EventInteractor eventInteractor) {
        this.teiDashboardPresenter = teiDashboardPresenter;
        this.programInteractor = programInteractor;
        this.eventInteractor = eventInteractor;
    }

    @Override
    public void drawProgramStages(String enrollmentUid, String programUid) {
        this.generateDummyProgramStages(enrollmentUid, programUid);
    }

    @Override
    public void onEventClicked(String eventUid) {
        teiDashboardPresenter.showDataEntryForEvent(eventUid);
        teiDashboardPresenter.hideMenu();
    }

    @Override
    public void attachView(View view) {
        teiProgramStageView = (TeiProgramStageView) view;
    }

    @Override
    public void detachView() {
        teiProgramStageView = null;
    }

    private void generateDummyProgramStages(final String enrollmentUid, final String programUid) {
//        List<ExpansionPanel> expansionPanels = new ArrayList<>();
        //TODO: find the view holder for ReportEntities and update the icon according to the status.
        //TODO: Fix the "123"'s and so on to be the actual uid's and display the event name from the hashMap instead.
        final List<ProgramStage> programStages = new ArrayList<>();
        List<Event> events = new ArrayList<>();

        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }

        subscription = new CompositeSubscription();
        subscription.add(Observable.zip(getProgram(programUid), getEventsForEnrollment(enrollmentUid), new Func2<Program, List<Event>, List<ExpansionPanel>>() {
            @Override
            public List<ExpansionPanel> call(Program program, List<Event> events) {
                List<ExpansionPanel> expansionPanels = new ArrayList<>();
                if (program != null && program.programStages() != null && !program.programStages().isEmpty()) {
                    for (ProgramStage programStage : program.programStages()) {
                        if (programStage != null) {
                            ExpansionPanel.Type type = ExpansionPanel.Type.ACTION_ADD;
                            if (programStage.repeatable() != null && programStage.repeatable()) {
                                type = ExpansionPanel.Type.ACTION_EDIT;
                            }
                            ExpansionPanel current = new ExpansionPanel(programStage.uid(), programStage.displayName(), type);
                            List<Event> eventsForStage = eventInteractor.store().queryEventsForEnrollmentAndProgramStage(enrollmentUid, programStage.uid());
                            current.setChildren(eventsToReportEntityList(programStage, eventsForStage));
                            expansionPanels.add(current);
                        }
                    }
                }
                return expansionPanels;
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<List<ExpansionPanel>>() {
                    @Override
                    public void call(List<ExpansionPanel> expansionPanelList) {
                        if (teiProgramStageView != null) {
                            teiProgramStageView.drawProgramStages(expansionPanelList);
                        }
                    }
                }));

//        subscription.add(Observable.zip(getProgram(programUid), getEventsForEnrollment(enrollmentUid), new Func2<Program, List<Event>, List<ProgramStage>>() {
//            @Override
//            public List<ProgramStage> call(Program program, List<Event> events) {
//                List<ExpansionPanel> expansionPanels = new ArrayList<>();
//                if(program != null && program.programStages() != null && !program.programStages().isEmpty()) {
//                    return program.programStages();
//                }
//                return null;
//            }
//        })
//                .forEach(new Action1<List<ProgramStage>>() {
//                    @Override
//                    public void call(List<ProgramStage> programStages) {
//
//                    }
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe(new Action1<List<ProgramStage>>() {
//                    @Override
//                    public void call(List<ProgramStage> expansionPanelList) {
//
//                    }
//                }));


    }

    /**
     * Returns a list of ReportEntities that corresponds to Event's in that program stage.
     *
     * @param programStage
     * @param events
     * @return
     */
    private List<ReportEntity> eventsToReportEntityList(ProgramStage programStage, List<Event> events) {
        List<ReportEntity> reportEntities = new ArrayList<>();
        if (events == null || events.isEmpty()) { // preventing additional work
            return reportEntities;
        }
        Collections.sort(events, Event.ASCENDING_DATE_COMPARATOR); // sort events by eventDate

        Collections.reverse(events);
        // retrieve state map for given events
        // it is done synchronously
        //TODO: use the new sdk when out, get states from the sdk :
        //Map<Long, State> stateMap = eventInteractor.map(events).toBlocking().first();

        for (Event event : events) {
            if (programStage.uid().equals(event.programStage())) { //if event in prog stage:
                // syncStatus of event
                ReportEntity.Status syncStatus;
                // get state of event from database
                //TODO: get the state's form the map:
                //State state = stateMap.get(event.getId());
                //A mock up that picks a random action for now:
                State state = State.values()[new Random().nextInt(State.values().length - 1)];

                //logger.d(TAG, "State action for event " + event + " is " + state.getAction());
                switch (state) {
                    case SYNCED: {
                        syncStatus = ReportEntity.Status.SENT;
                        break;
                    }
                    case TO_POST: {
                        syncStatus = ReportEntity.Status.TO_POST;
                        break;
                    }
                    case TO_UPDATE: {
                        syncStatus = ReportEntity.Status.TO_UPDATE;
                        break;
                    }
                    case ERROR: {
                        syncStatus = ReportEntity.Status.ERROR;
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException(
                                "Unsupported event state: " + state);
                    }
                }
                //Map<String, String> dataElementToValueMap = mapDataElementToValue(event.getDataValues());
                Map<String, String> dataElementToValueMap = new HashMap<>();
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

                dataElementToValueMap.put(ReportEntityFilter.DATE_KEY, sdf.format(event.eventDate()));
                dataElementToValueMap.put(ReportEntityFilter.STATUS_KEY, event.status().toString());
                //dataElementToValueMap.put("OrgUnit", event.getOrgUnit());
                reportEntities.add(new ReportEntity(event.uid(), syncStatus, dataElementToValueMap));
            }
        }
        return reportEntities;
    }

    private Map<String, String> mapDataElementToValue(List<TrackedEntityDataValue> dataValues) {
        Map<String, String> dataElementToValueMap = new HashMap<>();
        if (dataValues != null && !dataValues.isEmpty()) {
            for (TrackedEntityDataValue dataValue : dataValues) {
                String value = !StringUtils.isEmpty(dataValue.value()) ? dataValue.value() : "";
                dataElementToValueMap.put(dataValue.dataElement(), value);
            }
        }
        return dataElementToValueMap;
    }

    private Observable<List<Event>> getEventsForEnrollment(final String enrollmentUid) {
        return Observable.just(eventInteractor.store().queryEventsForEnrollment(enrollmentUid));
    }

    private Observable<Program> getProgram(final String programUid) {
        return Observable.just(programInteractor.store().queryByUid(programUid));
    }
}
