package org.hisp.dhis.android.app;


import org.hisp.dhis.client.sdk.models.common.State;
import org.hisp.dhis.client.sdk.models.dataelement.DataElement;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.option.Option;
import org.hisp.dhis.client.sdk.models.option.OptionSet;
import org.hisp.dhis.client.sdk.models.program.ProgramStageDataElement;
import org.hisp.dhis.client.sdk.models.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.client.sdk.ui.bindings.commons.RxOnValueChangedListener;
import org.hisp.dhis.client.sdk.ui.models.FormEntity;
import org.hisp.dhis.client.sdk.ui.models.FormEntityCharSequence;
import org.hisp.dhis.client.sdk.ui.models.FormEntityCheckBox;
import org.hisp.dhis.client.sdk.ui.models.FormEntityDate;
import org.hisp.dhis.client.sdk.ui.models.FormEntityEditText;
import org.hisp.dhis.client.sdk.ui.models.FormEntityFilter;
import org.hisp.dhis.client.sdk.ui.models.FormEntityRadioButtons;
import org.hisp.dhis.client.sdk.ui.models.FormEntityText;
import org.hisp.dhis.client.sdk.ui.models.Picker;

import java.util.List;

import static org.hisp.dhis.client.sdk.utils.StringUtils.isEmpty;

public class FormUtils {
    private FormUtils() {
        // no instances
    }


    public static FormEntity transformTrackedEntityAttribute(String trackedEntityInstanceUid,
                                                             TrackedEntityAttributeValue trackedEntityAttributeValue,
                                                             ProgramTrackedEntityAttribute programTrackedEntityAttribute,
                                                             OptionSet optionSet,
                                                             RxOnValueChangedListener onValueChangedListener) {
        TrackedEntityAttribute trackedEntityAttribute = programTrackedEntityAttribute.trackedEntityAttribute();
        String formEntityLabel = programTrackedEntityAttribute.trackedEntityAttribute().displayName();

        // logger.d(TAG, "DataElement: " + trackedEntityAttribute.getDisplayName());
        // logger.d(TAG, "ValueType: " + trackedEntityAttribute.getValueType());

        // create TrackedEntityDataValue upfront
        if (trackedEntityAttributeValue == null) {
            trackedEntityAttributeValue = TrackedEntityAttributeValue.builder()
                    .trackedEntityAttributeUid(programTrackedEntityAttribute.trackedEntityAttribute().uid())
                    .trackedEntityInstanceUid(trackedEntityInstanceUid)
                    .state(State.TO_POST)
                    .build();
        }

        // logger.d(TAG, "transformDataElement() -> TrackedEntityDataValue: " +
        //        trackedEntityAttributeValue + " localId: " + trackedEntityAttributeValue.getId());

        // in case if we have option set linked to data-element, we
        // need to process it regardless of data-element value type
        if (trackedEntityAttribute.optionSet() != null) {
//            OptionSet optionSet = optionSetInteractor.store().queryByUid(trackedEntityAttribute.optionSet().uid());
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
                    formEntityLabel, trackedEntityAttributeValue);
            formEntityFilter.setPicker(picker);
            formEntityFilter.setOnFormEntityChangeListener(onValueChangedListener);
            formEntityFilter.setMandatory(programTrackedEntityAttribute.mandatory());
            return formEntityFilter;
        }

        FormEntityCharSequence formEntity;

        switch (trackedEntityAttribute.valueType()) {
            case TEXT: {
                formEntity = new FormEntityEditText(trackedEntityAttribute.uid(),
                        formEntityLabel, FormEntityEditText.InputType.TEXT, trackedEntityAttributeValue);
                break;
            }
            case LONG_TEXT: {
                formEntity = new FormEntityEditText(trackedEntityAttribute.uid(),
                        formEntityLabel, FormEntityEditText.InputType.LONG_TEXT, trackedEntityAttributeValue);
                break;
            }
            case PHONE_NUMBER: {
                formEntity = new FormEntityEditText(trackedEntityAttribute.uid(),
                        formEntityLabel, FormEntityEditText.InputType.TEXT, trackedEntityAttributeValue);
                break;
            }
            case EMAIL: {
                formEntity = new FormEntityEditText(trackedEntityAttribute.uid(),
                        formEntityLabel, FormEntityEditText.InputType.TEXT, trackedEntityAttributeValue);
                break;
            }
            case NUMBER: {
                formEntity = new FormEntityEditText(trackedEntityAttribute.uid(),
                        formEntityLabel, FormEntityEditText.InputType.NUMBER, trackedEntityAttributeValue);
                break;
            }
            case INTEGER: {
                formEntity = new FormEntityEditText(trackedEntityAttribute.uid(),
                        formEntityLabel, FormEntityEditText.InputType.INTEGER, trackedEntityAttributeValue);
                break;
            }
            case INTEGER_POSITIVE: {
                formEntity = new FormEntityEditText(trackedEntityAttribute.uid(),
                        formEntityLabel, FormEntityEditText.InputType.INTEGER_POSITIVE, trackedEntityAttributeValue);
                break;
            }
            case INTEGER_NEGATIVE: {
                formEntity = new FormEntityEditText(trackedEntityAttribute.uid(),
                        formEntityLabel, FormEntityEditText.InputType.INTEGER_NEGATIVE, trackedEntityAttributeValue);
                break;
            }
            case INTEGER_ZERO_OR_POSITIVE: {
                formEntity = new FormEntityEditText(trackedEntityAttribute.uid(),
                        formEntityLabel, FormEntityEditText.InputType.INTEGER_ZERO_OR_POSITIVE, trackedEntityAttributeValue);
                break;
            }
            case DATE: {
                formEntity = new FormEntityDate(trackedEntityAttribute.uid(),
                        formEntityLabel, trackedEntityAttributeValue);
                break;
            }
            case BOOLEAN: {
                formEntity = new FormEntityRadioButtons(
                        trackedEntityAttribute.uid(), formEntityLabel, trackedEntityAttributeValue);
                break;
            }
            case TRUE_ONLY: {
                formEntity = new FormEntityCheckBox(
                        trackedEntityAttribute.uid(), formEntityLabel, trackedEntityAttributeValue);
                break;
            }
            default: {
//                logger.d(TAG, "Unsupported FormEntity type: " + trackedEntityAttribute.valueType());

                formEntity = new FormEntityText(trackedEntityAttribute.uid(),
                        formEntityLabel);
                formEntity.setValue("Unsupported value type: " +
                        trackedEntityAttribute.valueType(), false);
                return formEntity;
            }

        }

        formEntity.setValue(trackedEntityAttributeValue.value(), false);
        formEntity.setOnFormEntityChangeListener(onValueChangedListener);
        formEntity.setMandatory(programTrackedEntityAttribute.mandatory());
        return formEntity;
    }

    private static String getFormEntityLabel(ProgramStageDataElement stageDataElement) {
        DataElement dataElement = stageDataElement.dataElement();
        String label = isEmpty(dataElement.displayFormName()) ?
                dataElement.displayName() : dataElement.displayFormName();
        return label;
    }

    public static FormEntity transformDataElement(String username, Event event,
                                                  TrackedEntityDataValue dataValue,
                                                  ProgramStageDataElement stageDataElement,
                                                  OptionSet optionSet,
                                                  RxOnValueChangedListener onValueChangedListener) {
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
            formEntityFilter.setMandatory(stageDataElement.compulsory());
            return formEntityFilter;
        }

        FormEntityCharSequence formEntity;

        switch (dataElement.valueType()) {
            case TEXT: {
                formEntity = new FormEntityEditText(dataElement.uid(),
                        getFormEntityLabel(stageDataElement), FormEntityEditText.InputType.TEXT, dataValue);
                break;
            }
            case LONG_TEXT: {
                formEntity = new FormEntityEditText(dataElement.uid(),
                        getFormEntityLabel(stageDataElement), FormEntityEditText.InputType.LONG_TEXT, dataValue);
                break;
            }
            case PHONE_NUMBER: {
                formEntity = new FormEntityEditText(dataElement.uid(),
                        getFormEntityLabel(stageDataElement), FormEntityEditText.InputType.TEXT, dataValue);
                break;
            }
            case EMAIL: {
                formEntity = new FormEntityEditText(dataElement.uid(),
                        getFormEntityLabel(stageDataElement), FormEntityEditText.InputType.TEXT, dataValue);
                break;
            }
            case NUMBER: {
                formEntity = new FormEntityEditText(dataElement.uid(),
                        getFormEntityLabel(stageDataElement), FormEntityEditText.InputType.NUMBER, dataValue);
                break;
            }
            case INTEGER: {
                formEntity = new FormEntityEditText(dataElement.uid(),
                        getFormEntityLabel(stageDataElement), FormEntityEditText.InputType.INTEGER, dataValue);
                break;
            }
            case INTEGER_POSITIVE: {
                formEntity = new FormEntityEditText(dataElement.uid(),
                        getFormEntityLabel(stageDataElement), FormEntityEditText.InputType.INTEGER_POSITIVE, dataValue);
                break;
            }
            case INTEGER_NEGATIVE: {
                formEntity = new FormEntityEditText(dataElement.uid(),
                        getFormEntityLabel(stageDataElement), FormEntityEditText.InputType.INTEGER_NEGATIVE, dataValue);
                break;
            }
            case INTEGER_ZERO_OR_POSITIVE: {
                formEntity = new FormEntityEditText(dataElement.uid(),
                        getFormEntityLabel(stageDataElement), FormEntityEditText.InputType.INTEGER_ZERO_OR_POSITIVE, dataValue);
                break;
            }
            case DATE: {
                formEntity = new FormEntityDate(dataElement.uid(),
                        getFormEntityLabel(stageDataElement), dataValue);
                break;
            }
            case BOOLEAN: {
                formEntity = new FormEntityRadioButtons(
                        dataElement.uid(), getFormEntityLabel(stageDataElement), dataValue);
                break;
            }
            case TRUE_ONLY: {
                formEntity = new FormEntityCheckBox(
                        dataElement.uid(), getFormEntityLabel(stageDataElement), dataValue);
                break;
            }
            default: {
//                logger.d(TAG, "Unsupported FormEntity type: " + dataElement.valueType());

                FormEntityText formEntityText = new FormEntityText(dataElement.uid(),
                        getFormEntityLabel(stageDataElement));
                formEntityText.setValue("Unsupported value type: " +
                        dataElement.valueType(), false);

                return formEntityText;
            }
        }

        formEntity.setValue(dataValue.value(), false);
        formEntity.setOnFormEntityChangeListener(onValueChangedListener);
        formEntity.setMandatory(stageDataElement.compulsory());
        return formEntity;
    }
}
