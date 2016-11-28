package org.hisp.dhis.android.app.presenters;

import android.location.Location;

import org.hisp.dhis.android.app.LocationProvider;
import org.hisp.dhis.android.app.model.RxRulesEngine;
import org.hisp.dhis.android.app.views.FormSectionView;
import org.hisp.dhis.client.sdk.core.enrollment.EnrollmentInteractor;
import org.hisp.dhis.client.sdk.core.event.EventInteractor;
import org.hisp.dhis.client.sdk.core.program.ProgramInteractor;
import org.hisp.dhis.client.sdk.models.enrollment.Enrollment;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.event.EventStatus;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;
import org.hisp.dhis.client.sdk.models.program.ProgramType;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.FormSection;
import org.hisp.dhis.client.sdk.ui.models.Picker;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Single;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

// TODO cache metadata and data in memory
public class FormSectionPresenterImpl implements FormSectionPresenter {
    private static final String TAG = FormSectionPresenterImpl.class.getSimpleName();
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private final SimpleDateFormat simpleDateFormat;

    private final ProgramInteractor programInteractor;
    private final EventInteractor eventInteractor;
    private final EnrollmentInteractor enrollmentInteractor;
    private final RxRulesEngine rxRuleEngine;

    private final Logger logger;

    private FormSectionView formSectionView;
    private CompositeSubscription subscription;

    private LocationProvider locationProvider;
    private boolean gettingLocation = false;

    public FormSectionPresenterImpl(ProgramInteractor programInteractor,
                                    EventInteractor eventInteractor,
                                    EnrollmentInteractor enrollmentInteractor,
                                    RxRulesEngine rxRuleEngine,
                                    LocationProvider locationProvider, Logger logger) {
        this.programInteractor = programInteractor;
        this.eventInteractor = eventInteractor;
        this.enrollmentInteractor = enrollmentInteractor;
        this.rxRuleEngine = rxRuleEngine;
        this.locationProvider = locationProvider;
        this.logger = logger;
        this.simpleDateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.US);
    }

    @Override
    public void attachView(View view) {
        isNull(view, "View must not be null");
        formSectionView = (FormSectionView) view;
        if (gettingLocation) {
            formSectionView.setLocationButtonState(false);
        }
    }

    @Override
    public void detachView() {
        formSectionView = null;

        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    @Override
    public void createDataEntryForm(final String itemUid, String programUid, String programStageUid) {
        Program program = getProgram(programUid).toBlocking().first();
        if (program != null && program.programType() != null) {
            switch (program.programType()) {
                case WITH_REGISTRATION: {
                    if (programStageUid == null) {
                        createEnrollmentDataEntryForm(itemUid);
                    }
                    else {
                        createEventDataEntryForm(itemUid);
                    }
                }
                break;
                case WITHOUT_REGISTRATION: {
                    createEventDataEntryForm(itemUid);
                    break;
                }
                default:
                    throw new IllegalArgumentException("ProgramType is not supported");
            }
        }
    }

    private void createEventDataEntryForm(final String eventUid) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }

        subscription = new CompositeSubscription();

        subscription.add(getEvent(eventUid)
                .map(new Func1<Event, Event>() {
                    @Override
                    public Event call(Event event) {
                        // TODO consider refactoring rules-engine logic out of map function)
                        // synchronously initializing rule engine
                        rxRuleEngine.init(eventUid).toBlocking().first();

                        // compute initial RuleEffects
                        rxRuleEngine.notifyDataSetChanged();
                        return event;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Event>() {
                    @Override
                    public void call(Event event) {
                        isNull(event, String.format("Event with uid %s does not exist", eventUid));

                        if (formSectionView != null) {
                            formSectionView.showEventStatus(event.status());
                        }

                        // fire next operations
                        subscription.add(showFormPickers(event));
                        subscription.add(showFormSections(event.program()));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, null, throwable);
                    }
                }));
    }

    private void createEnrollmentDataEntryForm(final String enrollmentUid) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }

        subscription = new CompositeSubscription();

        subscription.add(getEnrollment(enrollmentUid)
                .map(new Func1<Enrollment, Enrollment>() {
                    @Override
                    public Enrollment call(Enrollment enrollment) {
                        // TODO consider refactoring rules-engine logic out of map function)
                        // synchronously initializing rule engine
//                        rxRuleEngine.initForEnrollment(enrollmentUid).toBlocking().first();

                        // compute initial RuleEffects
//                        rxRuleEngine.notifyDataSetChanged();
                        return enrollment;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Enrollment>() {
                    @Override
                    public void call(Enrollment enrollment) {
                        isNull(enrollment, String.format("Enrollment with uid %s does not exist", enrollmentUid));

                        if (formSectionView != null) {
                            formSectionView.showEnrollmentStatus(enrollment.enrollmentStatus());
                        }

                        // fire next operations
                        subscription.add(showFormPickers(enrollment));
                        subscription.add(showFormSections(enrollment.program()));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, null, throwable);
                    }
                }));
    }

    @Override
    public void saveEventDate(final String eventUid, final Date eventDate) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }

        subscription = new CompositeSubscription();
        subscription.add(getEvent(eventUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Event>() {
                    @Override
                    public void call(Event event) {
                        isNull(event, String.format("Event with uid %s does not exist", eventUid));

                        event.toBuilder().eventDate(eventDate).build();

                        subscription.add(saveEvent(event));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, null, throwable);
                    }
                }));
    }

    @Override
    public void saveEnrollmentDate(final String enrollmentUid, final Date eventDate) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }

        subscription = new CompositeSubscription();
        subscription.add(getEnrollment(enrollmentUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Enrollment>() {
                    @Override
                    public void call(Enrollment enrollment) {
                        isNull(enrollment, String.format("Enrollment with uid %s does not exist", enrollmentUid));

                        enrollment = enrollment.toBuilder().dateOfEnrollment(eventDate).build();

                        subscription.add(saveEnrollment(enrollment));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, null, throwable);
                    }
                }));
    }

    @Override
    public void saveEventStatus(final String eventUid, final EventStatus eventStatus) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }

        subscription = new CompositeSubscription();
        subscription.add(getEvent(eventUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Event>() {
                    @Override
                    public void call(Event event) {
                        isNull(event, String.format("Event with uid %s does not exist", eventUid));

                        event = event.toBuilder().status(eventStatus).build();

                        subscription.add(saveEvent(event));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, null, throwable);
                    }
                }));
    }

    private void viewSetLocation(Location location) {
        if (formSectionView != null) {
            formSectionView.setLocation(location);
        }
    }

    @Override
    public void subscribeToLocations() {
        gettingLocation = true;
        locationProvider.locations()
                .timeout(31L, TimeUnit.SECONDS)
                .buffer(2L, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<List<Location>>() {
                            @Override
                            public void call(List<Location> locations) {
                                if (locations.isEmpty() || locations.get(0) == null) {
                                    return;
                                }
                                Location currentLocation = locations.get(0);
                                Location bestLocation = currentLocation;
                                float accuracyAverage = currentLocation.getAccuracy();
                                //go over the locations and find the best + keep average
                                for (int i = 1; i < locations.size(); i++) {
                                    currentLocation = locations.get(i);
                                    accuracyAverage += currentLocation.getAccuracy();
                                    if (locationProvider.isBetterLocation(currentLocation, bestLocation)) {
                                        bestLocation = currentLocation;
                                    }
                                }
                                accuracyAverage = accuracyAverage / locations.size();
                                // if accuracy doesn't improve and we have more than one, we have the best estimate.
                                if (Math.round(accuracyAverage)
                                        == Math.round(bestLocation.getAccuracy())
                                        && locations.size() > 1) {
                                    gettingLocation = false;
                                    viewSetLocation(bestLocation);
                                    locationProvider.stopUpdates();
                                }
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                if (throwable instanceof TimeoutException) {
                                    logger.d(TAG, "Rx subscribeToLocaitons() timed out.");
                                } else {
                                    logger.e(TAG, "subscribeToLocations() rx call :" + throwable);
                                }
                                gettingLocation = false;
                                viewSetLocation(null);
                                locationProvider.stopUpdates();
                            }
                        },
                        new Action0() {
                            @Override
                            public void call() {
                                logger.d(TAG, "onComplete");
                                gettingLocation = false;
                                viewSetLocation(null);
                                locationProvider.stopUpdates();
                            }
                        }
                );
        locationProvider.requestLocation();
    }

    @Override
    public void stopLocationUpdates() {
        locationProvider.stopUpdates();
    }

    private Subscription saveEvent(final Event event) {
        return storeEvent(event)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean isSaved) {
                        if (isSaved) {
                            logger.d(TAG, "Successfully saved event " + event);
                        } else {
                            logger.d(TAG, "Failed to save event " + event);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, "Failed to save event " + event, throwable);
                    }
                });
    }

    private Subscription saveEnrollment(final Enrollment enrollment) {
        return storeEnrollment(enrollment)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean isSaved) {
                        if (isSaved) {
                            logger.d(TAG, "Successfully saved enrollment " + enrollment);
                        } else {
                            logger.d(TAG, "Failed to save enrollment " + enrollment);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, "Failed to save enrollment " + enrollment, throwable);
                    }
                });
    }

    private Subscription showFormPickers(final Enrollment enrollment) {
        String programId = enrollment.program();

        return getProgram(programId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Program>() {
                    @Override
                    public void call(Program program) {
                        if (formSectionView != null) {
                            String dateOfEnrollment;

                            if (enrollment.dateOfEnrollment() != null) {
                                dateOfEnrollment = simpleDateFormat.format(enrollment.dateOfEnrollment());
                            } else {
                                dateOfEnrollment = "";
                            }

                            formSectionView.showReportDatePicker(
                                    program.enrollmentDateLabel(), dateOfEnrollment);


                            if (program.captureCoordinates()) {
                                String latitude = null;
                                String longitude = null;

                                if (enrollment.coordinates() != null &&
                                        enrollment.coordinates().latitude() != null) {
                                    latitude = String.valueOf(enrollment.coordinates().latitude());
                                }

                                if (enrollment.coordinates() != null &&
                                        enrollment.coordinates().longitude() != null) {
                                    longitude = String.valueOf(enrollment.coordinates().longitude());
                                }

                                formSectionView.showCoordinatesPicker(latitude, longitude);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, "Failed to fetch program stage", throwable);
                    }
                });
    }

    private Subscription showFormPickers(final Event event) {
        String programId = event.program();

        return getProgram(programId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Program>() {
                    @Override
                    public void call(Program program) {
                        if (formSectionView != null) {
                            String eventDate;

                            if (event.eventDate() != null) {
                                eventDate = simpleDateFormat.format(event.eventDate());
                            } else {
                                eventDate = "";
                            }
                            ProgramStage currentProgramStage = null;
                            if (ProgramType.WITHOUT_REGISTRATION.equals(program.programType())) {
                                currentProgramStage = program.programStages().get(0);
                            }
                            else { // program is with registration, we need to loop through all stages and find matching uid
                                for (ProgramStage programStage : program.programStages()) {
                                    if(event.programStage().equals(programStage.uid())) {
                                        currentProgramStage = programStage;
                                        break;
                                    }
                                }
                            }

                            if (currentProgramStage == null) {
                                throw new IllegalArgumentException("No stages found for program");
                            }


                            formSectionView.showReportDatePicker(
                                    currentProgramStage.executionDateLabel(), eventDate);


                            if (program.captureCoordinates()) {
                                String latitude = null;
                                String longitude = null;

                                if (event.coordinates() != null &&
                                        event.coordinates().latitude() != null) {
                                    latitude = String.valueOf(event.coordinates().latitude());
                                }

                                if (event.coordinates() != null &&
                                        event.coordinates().longitude() != null) {
                                    longitude = String.valueOf(event.coordinates().longitude());
                                }

                                formSectionView.showCoordinatesPicker(latitude, longitude);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, "Failed to fetch program stage", throwable);
                    }
                });
    }

    private Subscription showFormSections(final String programUid) {

        return getProgram(programUid)
                .map(new Func1<Program, AbstractMap.SimpleEntry<Picker, List<FormSection>>>() {
                    @Override
                    public AbstractMap.SimpleEntry<Picker, List<FormSection>> call(Program program) {
                        ProgramStage currentProgramStage = null;

                        if (ProgramType.WITHOUT_REGISTRATION.equals(program.programType())) {
                            currentProgramStage = program.programStages().get(0);
                        }

                        List<ProgramStageSection> stageSections = null;

                        if (currentProgramStage != null && currentProgramStage.programStageSections() != null) {
                            stageSections = new ArrayList<>(currentProgramStage.programStageSections());
                        }

                        String chooseSectionPrompt = null;
                        if (formSectionView != null) {
                            chooseSectionPrompt = formSectionView.getFormSectionLabel(
                                    FormSectionView.ID_CHOOSE_SECTION);
                        }

                        // fetching prompt from resources
                        Picker picker = new Picker.Builder()
                                .id(program.uid())
                                .name(chooseSectionPrompt)
                                .build();

                        // transform sections
                        List<FormSection> formSections = new ArrayList<>();
                        if (stageSections != null && !stageSections.isEmpty()) {

                            // sort sections
                            Collections.sort(stageSections,
                                    ProgramStageSection.DESCENDING_SORT_ORDER_COMPARATOR);

                            for (ProgramStageSection section : stageSections) {
                                formSections.add(new FormSection(
                                        section.uid(), section.displayName()));
                                picker.addChild(
                                        new Picker.Builder()
                                                .id(section.uid())
                                                .name(section.displayName())
                                                .parent(picker)
                                                .build());
                            }
                        }

                        return new AbstractMap.SimpleEntry<>(picker, formSections);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<AbstractMap.SimpleEntry<Picker, List<FormSection>>>() {
                    @Override
                    public void call(AbstractMap.SimpleEntry<Picker, List<FormSection>> results) {
                        if (results != null && formSectionView != null) {
                            if (results.getValue() == null || results.getValue().isEmpty()) {
                                formSectionView.showFormDefaultSection(results.getKey().getId());
                            } else {
                                formSectionView.showFormSections(results.getValue());
                                formSectionView.setFormSectionsPicker(results.getKey());
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, "Form construction failed", throwable);
                    }
                });
    }

    private Observable<Program> getProgram(final String programUid) {
        return Observable.just(programInteractor.store().queryByUid(programUid));
    }

    private Single<Boolean> storeEvent(final Event event) {
        return Single.just(eventInteractor.store().save(event));
    }

    private Observable<Event> getEvent(final String eventUid) {
        return Observable.just(eventInteractor.store().query(eventUid));
    }

    private Observable<Enrollment> getEnrollment(final String enrollmentUid) {
        return Observable.just(enrollmentInteractor.store().query(enrollmentUid));
    }

    private Single<Boolean> storeEnrollment(final Enrollment enrollment) {
        return Single.just(enrollmentInteractor.store().save(enrollment));
    }
}
