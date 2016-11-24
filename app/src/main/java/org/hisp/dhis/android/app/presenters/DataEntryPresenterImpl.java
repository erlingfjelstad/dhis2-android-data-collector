package org.hisp.dhis.android.app.presenters;


import org.hisp.dhis.android.app.model.RxRulesEngine;
import org.hisp.dhis.android.app.views.DataEntryView;
import org.hisp.dhis.client.sdk.core.enrollment.EnrollmentInteractor;
import org.hisp.dhis.client.sdk.core.event.EventInteractor;
import org.hisp.dhis.client.sdk.core.option.OptionSetInteractor;
import org.hisp.dhis.client.sdk.core.program.ProgramInteractor;
import org.hisp.dhis.client.sdk.core.trackedentity.TrackedEntityAttributeValueInteractor;
import org.hisp.dhis.client.sdk.core.trackedentity.TrackedEntityDataValueInteractor;
import org.hisp.dhis.client.sdk.core.trackedentity.TrackedEntityInstanceInteractor;
import org.hisp.dhis.client.sdk.core.user.UserInteractor;
import org.hisp.dhis.client.sdk.models.common.State;
import org.hisp.dhis.client.sdk.models.dataelement.DataElement;
import org.hisp.dhis.client.sdk.models.enrollment.Enrollment;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.option.Option;
import org.hisp.dhis.client.sdk.models.option.OptionSet;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.program.ProgramStageDataElement;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;
import org.hisp.dhis.client.sdk.models.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.client.sdk.rules.RuleEffect;
import org.hisp.dhis.client.sdk.ui.bindings.commons.RxOnValueChangedListener;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.FormEntity;
import org.hisp.dhis.client.sdk.ui.models.FormEntityAction;
import org.hisp.dhis.client.sdk.ui.models.FormEntityCharSequence;
import org.hisp.dhis.client.sdk.ui.models.FormEntityCheckBox;
import org.hisp.dhis.client.sdk.ui.models.FormEntityDate;
import org.hisp.dhis.client.sdk.ui.models.FormEntityEditText;
import org.hisp.dhis.client.sdk.ui.models.FormEntityEditText.InputType;
import org.hisp.dhis.client.sdk.ui.models.FormEntityFilter;
import org.hisp.dhis.client.sdk.ui.models.FormEntityRadioButtons;
import org.hisp.dhis.client.sdk.ui.models.FormEntityText;
import org.hisp.dhis.client.sdk.ui.models.Picker;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;
import static org.hisp.dhis.client.sdk.utils.StringUtils.isEmpty;

public class DataEntryPresenterImpl implements DataEntryPresenter {
    private static final String TAG = DataEntryPresenterImpl.class.getSimpleName();

    private final UserInteractor currentUserInteractor;

    private final OptionSetInteractor optionSetInteractor;
    private final ProgramInteractor programInteractor;
    private final EventInteractor eventInteractor;
    private final EnrollmentInteractor enrollmentInteractor;
    private final TrackedEntityInstanceInteractor trackedEntityInstanceInteractor;
    private final TrackedEntityDataValueInteractor trackedEntityDataValueInteractor;
    private final TrackedEntityAttributeValueInteractor trackedEntityAttributeValueInteractor;

    private final RxRulesEngine rxRulesEngine;

    private final Logger logger;
    private final RxOnValueChangedListener onValueChangedListener;

    private DataEntryView dataEntryView;
    private CompositeSubscription subscription;


    public DataEntryPresenterImpl(UserInteractor currentUserInteractor,
                                  ProgramInteractor programInteractor,
                                  OptionSetInteractor optionSetInteractor,
                                  EventInteractor eventInteractor,
                                  EnrollmentInteractor enrollmentInteractor,
                                  TrackedEntityInstanceInteractor trackedEntityInstanceInteractor,
                                  TrackedEntityDataValueInteractor trackedEntityDataValueInteractor,
                                  TrackedEntityAttributeValueInteractor trackedEntityAttributeValueInteractor,
                                  RxRulesEngine rxRulesEngine, Logger logger) {
        this.currentUserInteractor = currentUserInteractor;
        this.optionSetInteractor = optionSetInteractor;
        this.programInteractor = programInteractor;

        this.eventInteractor = eventInteractor;
        this.enrollmentInteractor = enrollmentInteractor;
        this.trackedEntityInstanceInteractor = trackedEntityInstanceInteractor;
        this.trackedEntityDataValueInteractor = trackedEntityDataValueInteractor;
        this.trackedEntityAttributeValueInteractor = trackedEntityAttributeValueInteractor;
        this.rxRulesEngine = rxRulesEngine;

        this.logger = logger;
        this.onValueChangedListener = new RxOnValueChangedListener();
    }

    @Override
    public void attachView(View view) {
        isNull(view, "view must not be null");
        dataEntryView = (DataEntryView) view;
    }


    @Override
    public void detachView() {
        dataEntryView = null;

        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    @Override
    public void createDataEntryForm(String itemId, String programId, String programStageUid, String programStageSectionUid) {
        Program program = getProgram(programId).toBlocking().first();
        if (program != null && program.programType() != null) {
            switch (program.programType()) {
                case WITH_REGISTRATION: {
                    if (programStageUid == null) {
                        createEnrollmentDataEntryForm(itemId, programId);
                    } else {
                        createEventDataEntryForm(itemId, programId, programStageUid, programStageSectionUid);
                    }
                    break;
                }
                case WITHOUT_REGISTRATION: {
                    createEventDataEntryForm(itemId, programId, programStageUid, programStageSectionUid);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Program type not supported");
            }
        }
    }

    //TODO: Re-introduce ruleEngine
    private void createEnrollmentDataEntryForm(final String enrollmentId, final String programId) {
        logger.d(TAG, "EnrollmentId: " + enrollmentId);

        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }

        subscription = new CompositeSubscription();

        subscription.add(
//                engine().take(1).switchMap(
//                new Func1<List<FormEntityAction>, Observable<AbstractMap.SimpleEntry<List<FormEntity>, List<FormEntityAction>>>>() {
//                    @Override
//                    public Observable<AbstractMap.SimpleEntry<List<FormEntity>, List<FormEntityAction>>> call(
//                            final List<FormEntityAction> formEntityActions) {
//                        return Observable.zip(getEnrollment(enrollmentId), getProgram(programId),
//                                new Func2<Enrollment, Program, AbstractMap.SimpleEntry<List<FormEntity>, List<FormEntityAction>>>() {
//
//                                    @Override
//                                    public AbstractMap.SimpleEntry<List<FormEntity>, List<FormEntityAction>> call(
//                                            Enrollment enrollment, Program program) {
//
//
//                                        List<ProgramTrackedEntityAttribute> programTrackedEntityAttributes =
//                                                program.programTrackedEntityAttributes();
//                                        List<FormEntity> formEntities = transformProgramTrackedEntityAttributes(
//                                                enrollment, programTrackedEntityAttributes);
//
//                                        return new AbstractMap.SimpleEntry<>(formEntities, formEntityActions);
//                                    }
//                                });
//                    }
//                })
                Observable.zip(getEnrollment(enrollmentId), getProgram(programId),
                        new Func2<Enrollment, Program, AbstractMap.SimpleEntry<List<FormEntity>, List<FormEntityAction>>>() {

                            @Override
                            public AbstractMap.SimpleEntry<List<FormEntity>, List<FormEntityAction>> call(
                                    Enrollment enrollment, Program program) {
                                List<TrackedEntityAttributeValue> trackedEntityAttributeValues = trackedEntityAttributeValueInteractor.store().query(enrollment.trackedEntityInstance());
                                TrackedEntityInstance trackedEntityInstance = trackedEntityInstanceInteractor.store().queryByUid(enrollment.trackedEntityInstance());
                                if (trackedEntityAttributeValues != null && trackedEntityInstance != null) {
                                    trackedEntityInstance = trackedEntityInstance.toBuilder().trackedEntityAttributeValues(trackedEntityAttributeValues).build();
                                }

                                List<ProgramTrackedEntityAttribute> programTrackedEntityAttributes =
                                        program.programTrackedEntityAttributes();
                                List<FormEntity> formEntities = transformProgramTrackedEntityAttributes(
                                        trackedEntityInstance, programTrackedEntityAttributes);

                                return new AbstractMap.SimpleEntry<>(formEntities, (List<FormEntityAction>) new ArrayList<FormEntityAction>());
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<AbstractMap.SimpleEntry<List<FormEntity>, List<FormEntityAction>>>() {
                            @Override
                            public void call(AbstractMap.SimpleEntry<List<FormEntity>, List<FormEntityAction>> result) {
                                if (dataEntryView != null) {
                                    dataEntryView.showDataEntryForm(result.getKey(), result.getValue());
                                }
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                logger.e(TAG, "Something went wrong during form construction", throwable);
                            }
                        }));

        subscription.add(saveTrackedEntityAttributeValues());
//        subscription.add(subscribeToEngine());

    }

    private void createEventDataEntryForm(final String eventId, final String programId, final String programStageUid, final String programStageSectionUid) {
        if(programStageSectionUid != null) {
            createDataEntryFormSection(eventId, programId, programStageUid, programStageSectionUid);
        }
        else {
            createDataEntryFormStage(eventId, programId, programStageUid);
        }
    }

    @Override
    public void createDataEntryFormStage(final String eventId, final String programId, final String programStageId) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }

        final String username = currentUserInteractor.username();

        subscription = new CompositeSubscription();
        subscription.add(engine().take(1).switchMap(
                new Func1<List<FormEntityAction>, Observable<AbstractMap.SimpleEntry<List<FormEntity>, List<FormEntityAction>>>>() {
                    @Override
                    public Observable<AbstractMap.SimpleEntry<List<FormEntity>, List<FormEntityAction>>> call(
                            final List<FormEntityAction> formEntityActions) {
                        return Observable.zip(getEvent(eventId), getProgram(programId),
                                new Func2<Event, Program, AbstractMap.SimpleEntry<List<FormEntity>, List<FormEntityAction>>>() {

                                    @Override
                                    public AbstractMap.SimpleEntry<List<FormEntity>, List<FormEntityAction>> call(
                                            Event event, Program program) {
                                        ProgramStage currentProgramStage = null;
                                        for (ProgramStage programStage : program.programStages()) {
                                            if (programStage.uid().equals(event.programStage())) {
                                                currentProgramStage = programStage;
                                            }
                                        }

                                        if (currentProgramStage == null) {
                                            throw new IllegalArgumentException("No program stage uid found for programStageId: " + event.programStage());
                                        }
                                        List<TrackedEntityDataValue> dataValues = trackedEntityDataValueInteractor.store().query(eventId);
                                        if (dataValues != null) {
                                            event = event.toBuilder().trackedEntityDataValues(dataValues).build();
                                        }
                                        List<ProgramStageDataElement> dataElements =
                                                currentProgramStage.programStageDataElements();
                                        List<FormEntity> formEntities = transformDataElements(
                                                username, event, dataElements);

                                        return new AbstractMap.SimpleEntry<>(formEntities, formEntityActions);
                                    }
                                });
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<AbstractMap.SimpleEntry<List<FormEntity>, List<FormEntityAction>>>() {
                    @Override
                    public void call(AbstractMap.SimpleEntry<List<FormEntity>, List<FormEntityAction>> result) {
                        if (dataEntryView != null) {
                            dataEntryView.showDataEntryForm(result.getKey(), result.getValue());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, "Something went wrong during form construction", throwable);
                    }
                }));

        subscription.add(saveTrackedEntityDataValues());
        subscription.add(subscribeToEngine());
    }

    @Override
    public void createDataEntryFormSection(final String eventId,
                                           final String programId,
                                           final String programStageId,
                                           final String programStageSectionId) {
        logger.d(TAG, "ProgramStageSectionId: " + programStageSectionId);

        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }

        final String username = currentUserInteractor.username();

        subscription = new CompositeSubscription();
        subscription.add(engine().take(1).switchMap(
                new Func1<List<FormEntityAction>, Observable<AbstractMap.SimpleEntry<List<FormEntity>, List<FormEntityAction>>>>() {
                    @Override
                    public Observable<AbstractMap.SimpleEntry<List<FormEntity>, List<FormEntityAction>>> call(
                            final List<FormEntityAction> formEntityActions) {
                        return Observable.zip(getEvent(eventId), getProgram(programId),
                                new Func2<Event, Program, AbstractMap.SimpleEntry<List<FormEntity>, List<FormEntityAction>>>() {

                                    @Override
                                    public AbstractMap.SimpleEntry<List<FormEntity>, List<FormEntityAction>> call(
                                            Event event, Program program) {
                                        ProgramStage currentProgramStage = null;
                                        ProgramStageSection currentProgramStageSection = null;

                                        for (ProgramStage stage : program.programStages()) {
                                            if (stage.uid().equals(programStageId)) {
                                                currentProgramStage = stage;
                                            }
                                        }

                                        if (currentProgramStage == null) {
                                            throw new IllegalArgumentException("No program stage found for programStageId: " + programStageId);
                                        }

                                        for (ProgramStageSection stageSection : currentProgramStage.programStageSections()) {
                                            if (stageSection.uid().equals(programStageSectionId)) {
                                                currentProgramStageSection = stageSection;
                                            }
                                        }

                                        if (currentProgramStageSection == null) {
                                            throw new IllegalArgumentException("No program stage section found for programStageSectionId: " + programStageSectionId);
                                        }
                                        List<ProgramStageDataElement> programStageDataElements =
                                                new ArrayList<>(currentProgramStageSection.programStageDataElements());

                                        // sort ProgramStageDataElements by sortOrder
                                        if (programStageDataElements != null) {
                                            Collections.sort(programStageDataElements,
                                                    ProgramStageDataElement.DESCENDING_SORT_ORDER_COMPARATOR);
                                        }

                                        List<FormEntity> formEntities = transformDataElements(
                                                username, event, programStageDataElements);
                                        return new AbstractMap.SimpleEntry<>(formEntities, formEntityActions);
                                    }
                                });
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<AbstractMap.SimpleEntry<List<FormEntity>, List<FormEntityAction>>>() {
                    @Override
                    public void call(AbstractMap.SimpleEntry<List<FormEntity>, List<FormEntityAction>> entry) {
                        if (dataEntryView != null) {
                            dataEntryView.showDataEntryForm(entry.getKey(), entry.getValue());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, "Something went wrong during form construction", throwable);
                    }
                }));

        subscription.add(saveTrackedEntityDataValues());
        subscription.add(subscribeToEngine());
    }

    private Observable<List<FormEntityAction>> engine() {
        return rxRulesEngine.observable()
                .map(new Func1<List<RuleEffect>, List<FormEntityAction>>() {
                    @Override
                    public List<FormEntityAction> call(List<RuleEffect> effects) {
                        return transformRuleEffects(effects);
                    }
                });
    }

    private Subscription subscribeToEngine() {
        return engine().subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<FormEntityAction>>() {
                    @Override
                    public void call(List<FormEntityAction> actions) {
                        if (dataEntryView != null) {
                            dataEntryView.updateDataEntryForm(actions);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, "Failed to calculate rules", throwable);
                    }
                });
    }

    private Subscription saveTrackedEntityDataValues() {
        return Observable.create(onValueChangedListener)
                .debounce(512, TimeUnit.MILLISECONDS)
                .switchMap(new Func1<FormEntity, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(FormEntity formEntity) {
                        return onFormEntityChanged(formEntity);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean isSaved) {
                        if (isSaved) {
                            logger.d(TAG, "data value is saved successfully");

                            // fire rule engine execution
                            rxRulesEngine.notifyDataSetChanged();
                        } else {
                            logger.d(TAG, "Failed to save value");
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, "Failed to save value", throwable);
                    }
                });
    }


    //TODO: Reintroduce rxRulesEngine.notifyDataSetChanged
    private Subscription saveTrackedEntityAttributeValues() {
        return Observable.create(onValueChangedListener)
                .debounce(512, TimeUnit.MILLISECONDS)
                .switchMap(new Func1<FormEntity, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(FormEntity formEntity) {
                        return onFormEntityAttributeChanged(formEntity);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean isSaved) {
                        if (isSaved) {
                            logger.d(TAG, "data value is saved successfully");

                            // fire rule engine execution
//                            rxRulesEngine.notifyDataSetChanged();
                        } else {
                            logger.d(TAG, "Failed to save value");
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, "Failed to save value", throwable);
                    }
                });
    }

    private List<FormEntity> transformProgramTrackedEntityAttributes(TrackedEntityInstance trackedEntityInstance,
                                                                     List<ProgramTrackedEntityAttribute> programTrackedEntityAttributes) {
        if (programTrackedEntityAttributes == null || programTrackedEntityAttributes.isEmpty()) {
            return new ArrayList<>();
        }

        // List<DataElement> dataElements = new ArrayList<>();
        for (ProgramTrackedEntityAttribute programTrackedEntityAttribute : programTrackedEntityAttributes) {
            TrackedEntityAttribute trackedEntityAttribute = programTrackedEntityAttribute.trackedEntityAttribute();
            if (trackedEntityAttribute == null) {
                throw new RuntimeException("Malformed metadata: Program" +
                        "StageDataElement " + programTrackedEntityAttribute.uid() +
                        " does not have reference to DataElement");
            }

            OptionSet optionSet = trackedEntityAttribute.optionSet();
            if (optionSet != null) {
                List<Option> options = getOptionSets(
                        trackedEntityAttribute.optionSet().uid()).toBlocking().first().options();
                optionSet.toBuilder().options(options).build();
            }
        }

        Map<String, TrackedEntityAttributeValue> attributeValueMap = new HashMap<>();

        if (trackedEntityInstance.trackedEntityAttributeValues() != null && !trackedEntityInstance.trackedEntityAttributeValues().isEmpty()) {
            for (TrackedEntityAttributeValue trackedEntityAttributeValue : trackedEntityInstance.trackedEntityAttributeValues()) {
                attributeValueMap.put(trackedEntityAttributeValue.trackedEntityAttributeUid(), trackedEntityAttributeValue);
            }
        }

        List<FormEntity> formEntities = new ArrayList<>();
        for (ProgramTrackedEntityAttribute programTrackedEntityAttribute : programTrackedEntityAttributes) {
            TrackedEntityAttribute trackedEntityAttribute = programTrackedEntityAttribute.trackedEntityAttribute();
            formEntities.add(transformTrackedEntityAttribute(trackedEntityInstance,
                    attributeValueMap.get(trackedEntityAttribute.uid()),
                    programTrackedEntityAttribute));
        }

        return formEntities;
    }

    private FormEntity transformTrackedEntityAttribute(TrackedEntityInstance trackedEntityInstance,
                                                       TrackedEntityAttributeValue trackedEntityAttributeValue,
                                                       ProgramTrackedEntityAttribute programTrackedEntityAttribute) {
        TrackedEntityAttribute trackedEntityAttribute = programTrackedEntityAttribute.trackedEntityAttribute();

        // logger.d(TAG, "DataElement: " + trackedEntityAttribute.getDisplayName());
        // logger.d(TAG, "ValueType: " + trackedEntityAttribute.getValueType());

        // create TrackedEntityDataValue upfront
        if (trackedEntityAttributeValue == null) {
            trackedEntityAttributeValue = TrackedEntityAttributeValue.builder()
                    .trackedEntityAttributeUid(programTrackedEntityAttribute.trackedEntityAttribute().uid())
                    .trackedEntityInstanceUid(trackedEntityInstance.uid())
                    .state(State.TO_POST)
                    .build();
        }

        // logger.d(TAG, "transformDataElement() -> TrackedEntityDataValue: " +
        //        trackedEntityAttributeValue + " localId: " + trackedEntityAttributeValue.getId());

        // in case if we have option set linked to data-element, we
        // need to process it regardless of data-element value type
        if (trackedEntityAttribute.optionSet() != null) {
            OptionSet optionSet = optionSetInteractor.store().queryByUid(trackedEntityAttribute.optionSet().uid());
            List<Option> options = optionSet.options();

            Picker picker = new Picker.Builder()
                    .hint(trackedEntityAttribute.displayName())
                    .build();
            if (options != null && !options.isEmpty()) {
                for (Option option : options) {
                    Picker childPicker = new Picker.Builder()
                            .id(option.code())
                            .name(option.displayName())
                            .parent(picker)
                            .build();
                    picker.addChild(childPicker);

                    if (option.code().equals(trackedEntityAttributeValue.value())) {
                        picker.setSelectedChild(childPicker);
                    }
                }
            }

            FormEntityFilter formEntityFilter = new FormEntityFilter(trackedEntityAttribute.uid(),
                    getFormEntityLabel(programTrackedEntityAttribute), trackedEntityAttributeValue);
            formEntityFilter.setPicker(picker);
            formEntityFilter.setOnFormEntityChangeListener(onValueChangedListener);

            return formEntityFilter;
        }

        switch (trackedEntityAttribute.valueType()) {
            case TEXT: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(trackedEntityAttribute.uid(),
                        getFormEntityLabel(programTrackedEntityAttribute), InputType.TEXT, trackedEntityAttributeValue);
                formEntityEditText.setValue(trackedEntityAttributeValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case LONG_TEXT: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(trackedEntityAttribute.uid(),
                        getFormEntityLabel(programTrackedEntityAttribute), InputType.LONG_TEXT, trackedEntityAttributeValue);
                formEntityEditText.setValue(trackedEntityAttributeValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case PHONE_NUMBER: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(trackedEntityAttribute.uid(),
                        getFormEntityLabel(programTrackedEntityAttribute), InputType.TEXT, trackedEntityAttributeValue);
                formEntityEditText.setValue(trackedEntityAttributeValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case EMAIL: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(trackedEntityAttribute.uid(),
                        getFormEntityLabel(programTrackedEntityAttribute), InputType.TEXT, trackedEntityAttributeValue);
                formEntityEditText.setValue(trackedEntityAttributeValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case NUMBER: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(trackedEntityAttribute.uid(),
                        getFormEntityLabel(programTrackedEntityAttribute), InputType.NUMBER, trackedEntityAttributeValue);
                formEntityEditText.setValue(trackedEntityAttributeValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case INTEGER: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(trackedEntityAttribute.uid(),
                        getFormEntityLabel(programTrackedEntityAttribute), InputType.INTEGER, trackedEntityAttributeValue);
                formEntityEditText.setValue(trackedEntityAttributeValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case INTEGER_POSITIVE: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(trackedEntityAttribute.uid(),
                        getFormEntityLabel(programTrackedEntityAttribute), InputType.INTEGER_POSITIVE, trackedEntityAttributeValue);
                formEntityEditText.setValue(trackedEntityAttributeValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case INTEGER_NEGATIVE: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(trackedEntityAttribute.uid(),
                        getFormEntityLabel(programTrackedEntityAttribute), InputType.INTEGER_NEGATIVE, trackedEntityAttributeValue);
                formEntityEditText.setValue(trackedEntityAttributeValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case INTEGER_ZERO_OR_POSITIVE: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(trackedEntityAttribute.uid(),
                        getFormEntityLabel(programTrackedEntityAttribute), InputType.INTEGER_ZERO_OR_POSITIVE, trackedEntityAttributeValue);
                formEntityEditText.setValue(trackedEntityAttributeValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case DATE: {
                FormEntityDate formEntityDate = new FormEntityDate(trackedEntityAttribute.uid(),
                        getFormEntityLabel(programTrackedEntityAttribute), trackedEntityAttributeValue);
                formEntityDate.setValue(trackedEntityAttributeValue.value(), false);
                formEntityDate.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityDate;
            }
            case BOOLEAN: {
                FormEntityRadioButtons formEntityRadioButtons = new FormEntityRadioButtons(
                        trackedEntityAttribute.uid(), getFormEntityLabel(programTrackedEntityAttribute), trackedEntityAttributeValue);
                formEntityRadioButtons.setValue(trackedEntityAttributeValue.value(), false);
                formEntityRadioButtons.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityRadioButtons;
            }
            case TRUE_ONLY: {
                FormEntityCheckBox formEntityCheckBox = new FormEntityCheckBox(
                        trackedEntityAttribute.uid(), getFormEntityLabel(programTrackedEntityAttribute), trackedEntityAttributeValue);
                formEntityCheckBox.setValue(trackedEntityAttributeValue.value(), false);
                formEntityCheckBox.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityCheckBox;
            }
            default:
                logger.d(TAG, "Unsupported FormEntity type: " + trackedEntityAttribute.valueType());

                FormEntityText formEntityText = new FormEntityText(trackedEntityAttribute.uid(),
                        getFormEntityLabel(programTrackedEntityAttribute));
                formEntityText.setValue("Unsupported value type: " +
                        trackedEntityAttribute.valueType(), false);

                return formEntityText;
        }
    }


    private List<FormEntity> transformDataElements(
            String username, Event event, List<ProgramStageDataElement> stageDataElements) {
        if (stageDataElements == null || stageDataElements.isEmpty()) {
            return new ArrayList<>();
        }

        // List<DataElement> dataElements = new ArrayList<>();
        for (ProgramStageDataElement stageDataElement : stageDataElements) {
            DataElement dataElement = stageDataElement.dataElement();
            if (dataElement == null) {
                throw new RuntimeException("Malformed metadata: Program" +
                        "StageDataElement " + stageDataElement.uid() +
                        " does not have reference to DataElement");
            }

            OptionSet optionSet = dataElement.optionSet();
            if (optionSet != null) {
                List<Option> options = getOptionSets(
                        dataElement.optionSet().uid()).toBlocking().first().options();
                optionSet.toBuilder().options(options).build();
            }
        }

        Map<String, TrackedEntityDataValue> dataValueMap = new HashMap<>();
        if (event.trackedEntityDataValues() != null && !event.trackedEntityDataValues().isEmpty()) {
            for (TrackedEntityDataValue dataValue : event.trackedEntityDataValues()) {
                dataValueMap.put(dataValue.dataElement(), dataValue);
            }
        }

        List<FormEntity> formEntities = new ArrayList<>();
        for (ProgramStageDataElement stageDataElement : stageDataElements) {
            DataElement dataElement = stageDataElement.dataElement();
            formEntities.add(transformDataElement(
                    username, event, dataValueMap.get(dataElement.uid()), stageDataElement));
        }

        return formEntities;
    }

    private FormEntity transformDataElement(String username, Event event,
                                            TrackedEntityDataValue dataValue,
                                            ProgramStageDataElement stageDataElement) {
        DataElement dataElement = stageDataElement.dataElement();

        // logger.d(TAG, "DataElement: " + dataElement.getDisplayName());
        // logger.d(TAG, "ValueType: " + dataElement.getValueType());

        // create TrackedEntityDataValue upfront
        if (dataValue == null) {
            dataValue = TrackedEntityDataValue.builder()
                    .event(event.uid())
                    .dataElement(dataElement.uid())
                    .storedBy(username)
                    .build();
        }

        // logger.d(TAG, "transformDataElement() -> TrackedEntityDataValue: " +
        //        dataValue + " localId: " + dataValue.getId());

        // in case if we have option set linked to data-element, we
        // need to process it regardless of data-element value type
        if (dataElement.optionSet() != null) {
            OptionSet optionSet = optionSetInteractor.store().queryByUid(dataElement.optionSet().uid());
            List<Option> options = optionSet.options();

            Picker picker = new Picker.Builder()
                    .hint(dataElement.displayName())
                    .build();
            if (options != null && !options.isEmpty()) {
                for (Option option : options) {
                    Picker childPicker = new Picker.Builder()
                            .id(option.code())
                            .name(option.displayName())
                            .parent(picker)
                            .build();
                    picker.addChild(childPicker);

                    if (option.code().equals(dataValue.value())) {
                        picker.setSelectedChild(childPicker);
                    }
                }
            }

            FormEntityFilter formEntityFilter = new FormEntityFilter(dataElement.uid(),
                    getFormEntityLabel(stageDataElement), dataValue);
            formEntityFilter.setPicker(picker);
            formEntityFilter.setOnFormEntityChangeListener(onValueChangedListener);

            return formEntityFilter;
        }

        switch (dataElement.valueType()) {
            case TEXT: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(dataElement.uid(),
                        getFormEntityLabel(stageDataElement), InputType.TEXT, dataValue);
                formEntityEditText.setValue(dataValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case LONG_TEXT: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(dataElement.uid(),
                        getFormEntityLabel(stageDataElement), InputType.LONG_TEXT, dataValue);
                formEntityEditText.setValue(dataValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case PHONE_NUMBER: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(dataElement.uid(),
                        getFormEntityLabel(stageDataElement), InputType.TEXT, dataValue);
                formEntityEditText.setValue(dataValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case EMAIL: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(dataElement.uid(),
                        getFormEntityLabel(stageDataElement), InputType.TEXT, dataValue);
                formEntityEditText.setValue(dataValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case NUMBER: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(dataElement.uid(),
                        getFormEntityLabel(stageDataElement), InputType.NUMBER, dataValue);
                formEntityEditText.setValue(dataValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case INTEGER: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(dataElement.uid(),
                        getFormEntityLabel(stageDataElement), InputType.INTEGER, dataValue);
                formEntityEditText.setValue(dataValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case INTEGER_POSITIVE: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(dataElement.uid(),
                        getFormEntityLabel(stageDataElement), InputType.INTEGER_POSITIVE, dataValue);
                formEntityEditText.setValue(dataValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case INTEGER_NEGATIVE: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(dataElement.uid(),
                        getFormEntityLabel(stageDataElement), InputType.INTEGER_NEGATIVE, dataValue);
                formEntityEditText.setValue(dataValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case INTEGER_ZERO_OR_POSITIVE: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(dataElement.uid(),
                        getFormEntityLabel(stageDataElement), InputType.INTEGER_ZERO_OR_POSITIVE, dataValue);
                formEntityEditText.setValue(dataValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case DATE: {
                FormEntityDate formEntityDate = new FormEntityDate(dataElement.uid(),
                        getFormEntityLabel(stageDataElement), dataValue);
                formEntityDate.setValue(dataValue.value(), false);
                formEntityDate.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityDate;
            }
            case BOOLEAN: {
                FormEntityRadioButtons formEntityRadioButtons = new FormEntityRadioButtons(
                        dataElement.uid(), getFormEntityLabel(stageDataElement), dataValue);
                formEntityRadioButtons.setValue(dataValue.value(), false);
                formEntityRadioButtons.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityRadioButtons;
            }
            case TRUE_ONLY: {
                FormEntityCheckBox formEntityCheckBox = new FormEntityCheckBox(
                        dataElement.uid(), getFormEntityLabel(stageDataElement), dataValue);
                formEntityCheckBox.setValue(dataValue.value(), false);
                formEntityCheckBox.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityCheckBox;
            }
            default:
                logger.d(TAG, "Unsupported FormEntity type: " + dataElement.valueType());

                FormEntityText formEntityText = new FormEntityText(dataElement.uid(),
                        getFormEntityLabel(stageDataElement));
                formEntityText.setValue("Unsupported value type: " +
                        dataElement.valueType(), false);

                return formEntityText;
        }
    }

    private List<FormEntityAction> transformRuleEffects(List<RuleEffect> ruleEffects) {
        List<FormEntityAction> entityActions = new ArrayList<>();
        if (ruleEffects == null || ruleEffects.isEmpty()) {
            return entityActions;
        }

        for (RuleEffect ruleEffect : ruleEffects) {
            if (ruleEffect == null || ruleEffect.getProgramRuleActionType() == null) {
                logger.d(TAG, "failed processing broken RuleEffect");
                continue;
            }

            switch (ruleEffect.getProgramRuleActionType()) {
                case HIDEFIELD: {
                    if (ruleEffect.getDataElement() != null) {
                        String dataElementUid = ruleEffect.getDataElement().uid();
                        FormEntityAction formEntityAction = new FormEntityAction(
                                dataElementUid, null, FormEntityAction.FormEntityActionType.HIDE);
                        entityActions.add(formEntityAction);
                    }
                    break;
                }
                case ASSIGN: {
                    if (ruleEffect.getDataElement() != null) {
                        String dataElementUid = ruleEffect.getDataElement().uid();
                        FormEntityAction formEntityAction = new FormEntityAction(
                                dataElementUid, ruleEffect.getData(), FormEntityAction.FormEntityActionType.ASSIGN);
                        entityActions.add(formEntityAction);
                    }
                    break;
                }
            }
        }

        return entityActions;
    }

    private String getFormEntityLabel(ProgramTrackedEntityAttribute programTrackedEntityAttribute) {
        TrackedEntityAttribute trackedEntityAttribute = programTrackedEntityAttribute.trackedEntityAttribute();
        String label = isEmpty(trackedEntityAttribute.displayName()) ?
                trackedEntityAttribute.displayName() : trackedEntityAttribute.displayName();

        if (programTrackedEntityAttribute.mandatory()) {
            label = label + " (*)";
        }

        return label;
    }

    private String getFormEntityLabel(ProgramStageDataElement stageDataElement) {
        DataElement dataElement = stageDataElement.dataElement();
        String label = isEmpty(dataElement.displayFormName()) ?
                dataElement.displayName() : dataElement.displayFormName();

        if (stageDataElement.compulsory()) {
            label = label + " (*)";
        }

        return label;
    }

    private Observable<Boolean> onFormEntityChanged(FormEntity formEntity) {
        return Observable.just(trackedEntityDataValueInteractor.store().save(mapFormEntityToDataValue(formEntity)));
    }

    private Observable<Boolean> onFormEntityAttributeChanged(FormEntity formEntity) {
        return Observable.just(trackedEntityAttributeValueInteractor.store().save(mapFormEntityToAttributeValue(formEntity)));
    }

    private TrackedEntityAttributeValue mapFormEntityToAttributeValue(FormEntity entity) {
        if (entity instanceof FormEntityFilter) {
            Picker picker = ((FormEntityFilter) entity).getPicker();

            String value = "";
            if (picker != null && picker.getSelectedChild() != null) {
                value = picker.getSelectedChild().getId();
            }

            TrackedEntityAttributeValue trackedEntityAttributeValue;
            if (entity.getTag() != null) {
                trackedEntityAttributeValue = (TrackedEntityAttributeValue) entity.getTag();
            } else {
                throw new IllegalArgumentException("TrackedEntityAttributeValue must be " +
                        "assigned to FormEntity upfront");
            }

            TrackedEntityAttributeValue.Builder trackedEntityAttributeValueBuilder = trackedEntityAttributeValue.toBuilder();
            trackedEntityAttributeValueBuilder.state(State.TO_POST);
            trackedEntityAttributeValueBuilder.value(value);
            trackedEntityAttributeValue = trackedEntityAttributeValueBuilder.build();

            logger.d(TAG, "New value " + value + " is emitted for " + entity.getLabel());

            return trackedEntityAttributeValue;
        } else if (entity instanceof FormEntityCharSequence) {
            String value = ((FormEntityCharSequence) entity).getValue().toString();

            TrackedEntityAttributeValue trackedEntityAttributeValue;
            if (entity.getTag() != null) {
                trackedEntityAttributeValue = (TrackedEntityAttributeValue) entity.getTag();
            } else {
                throw new IllegalArgumentException("TrackedEntityAttributeValue must be " +
                        "assigned to FormEntity upfront");
            }

            TrackedEntityAttributeValue.Builder trackedEntityAttributeValueBuilder = trackedEntityAttributeValue.toBuilder();
            trackedEntityAttributeValueBuilder.state(State.TO_POST);
            trackedEntityAttributeValueBuilder.value(value);
            trackedEntityAttributeValue = trackedEntityAttributeValueBuilder.build();
            logger.d(TAG, "New value " + value + " is emitted for " + entity.getLabel());

            return trackedEntityAttributeValue;
        }

        return null;
    }

    private TrackedEntityDataValue mapFormEntityToDataValue(FormEntity entity) {
        if (entity instanceof FormEntityFilter) {
            Picker picker = ((FormEntityFilter) entity).getPicker();

            String value = "";
            if (picker != null && picker.getSelectedChild() != null) {
                value = picker.getSelectedChild().getId();
            }

            TrackedEntityDataValue dataValue;
            if (entity.getTag() != null) {
                dataValue = (TrackedEntityDataValue) entity.getTag();
            } else {
                throw new IllegalArgumentException("TrackedEntityDataValue must be " +
                        "assigned to FormEntity upfront");
            }

            TrackedEntityDataValue.Builder trackedEntityDataValueBuilder = dataValue.toBuilder();
            trackedEntityDataValueBuilder.state(State.TO_POST);
            trackedEntityDataValueBuilder.value(value);
            dataValue = trackedEntityDataValueBuilder.build();

            logger.d(TAG, "New value " + value + " is emitted for " + entity.getLabel());

            return dataValue;
        } else if (entity instanceof FormEntityCharSequence) {
            String value = ((FormEntityCharSequence) entity).getValue().toString();

            TrackedEntityDataValue dataValue;
            if (entity.getTag() != null) {
                dataValue = (TrackedEntityDataValue) entity.getTag();
            } else {
                throw new IllegalArgumentException("TrackedEntityDataValue must be " +
                        "assigned to FormEntity upfront");
            }

            TrackedEntityDataValue.Builder trackedEntityDataValueBuilder = dataValue.toBuilder();
            trackedEntityDataValueBuilder.state(State.TO_POST);
            trackedEntityDataValueBuilder.value(value);
            dataValue = trackedEntityDataValueBuilder.build();
            logger.d(TAG, "New value " + value + " is emitted for " + entity.getLabel());

            return dataValue;
        }

        return null;
    }

    private Observable<Event> getEvent(String uid) {
        return Observable.just(eventInteractor.store().query(uid));
    }

    private Observable<Enrollment> getEnrollment(String enrollmentUid) {
        return Observable.just(enrollmentInteractor.store().query(enrollmentUid));
    }

    private Observable<TrackedEntityInstance> getTrackedEntityInstance(String trackedEntityInstanceUid) {
        return Observable.just(trackedEntityInstanceInteractor.store().queryByUid(trackedEntityInstanceUid));
    }

    private Observable<Program> getProgram(String uid) {
        return Observable.just(programInteractor.store().queryByUid(uid));
    }

    private Observable<OptionSet> getOptionSets(String uid) {
        return Observable.just(optionSetInteractor.store().queryByUid(uid));
    }
}
