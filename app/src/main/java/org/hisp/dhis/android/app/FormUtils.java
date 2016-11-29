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
        String formEntityLabel = getFormEntityLabel(programTrackedEntityAttribute);

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

            return formEntityFilter;
        }

        switch (trackedEntityAttribute.valueType()) {
            case TEXT: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(trackedEntityAttribute.uid(),
                        formEntityLabel, FormEntityEditText.InputType.TEXT, trackedEntityAttributeValue);
                formEntityEditText.setValue(trackedEntityAttributeValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case LONG_TEXT: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(trackedEntityAttribute.uid(),
                        formEntityLabel, FormEntityEditText.InputType.LONG_TEXT, trackedEntityAttributeValue);
                formEntityEditText.setValue(trackedEntityAttributeValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case PHONE_NUMBER: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(trackedEntityAttribute.uid(),
                        formEntityLabel, FormEntityEditText.InputType.TEXT, trackedEntityAttributeValue);
                formEntityEditText.setValue(trackedEntityAttributeValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case EMAIL: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(trackedEntityAttribute.uid(),
                        formEntityLabel, FormEntityEditText.InputType.TEXT, trackedEntityAttributeValue);
                formEntityEditText.setValue(trackedEntityAttributeValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case NUMBER: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(trackedEntityAttribute.uid(),
                        formEntityLabel, FormEntityEditText.InputType.NUMBER, trackedEntityAttributeValue);
                formEntityEditText.setValue(trackedEntityAttributeValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case INTEGER: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(trackedEntityAttribute.uid(),
                        formEntityLabel, FormEntityEditText.InputType.INTEGER, trackedEntityAttributeValue);
                formEntityEditText.setValue(trackedEntityAttributeValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case INTEGER_POSITIVE: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(trackedEntityAttribute.uid(),
                        formEntityLabel, FormEntityEditText.InputType.INTEGER_POSITIVE, trackedEntityAttributeValue);
                formEntityEditText.setValue(trackedEntityAttributeValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case INTEGER_NEGATIVE: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(trackedEntityAttribute.uid(),
                        formEntityLabel, FormEntityEditText.InputType.INTEGER_NEGATIVE, trackedEntityAttributeValue);
                formEntityEditText.setValue(trackedEntityAttributeValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case INTEGER_ZERO_OR_POSITIVE: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(trackedEntityAttribute.uid(),
                        formEntityLabel, FormEntityEditText.InputType.INTEGER_ZERO_OR_POSITIVE, trackedEntityAttributeValue);
                formEntityEditText.setValue(trackedEntityAttributeValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case DATE: {
                FormEntityDate formEntityDate = new FormEntityDate(trackedEntityAttribute.uid(),
                        formEntityLabel, trackedEntityAttributeValue);
                formEntityDate.setValue(trackedEntityAttributeValue.value(), false);
                formEntityDate.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityDate;
            }
            case BOOLEAN: {
                FormEntityRadioButtons formEntityRadioButtons = new FormEntityRadioButtons(
                        trackedEntityAttribute.uid(), formEntityLabel, trackedEntityAttributeValue);
                formEntityRadioButtons.setValue(trackedEntityAttributeValue.value(), false);
                formEntityRadioButtons.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityRadioButtons;
            }
            case TRUE_ONLY: {
                FormEntityCheckBox formEntityCheckBox = new FormEntityCheckBox(
                        trackedEntityAttribute.uid(), formEntityLabel, trackedEntityAttributeValue);
                formEntityCheckBox.setValue(trackedEntityAttributeValue.value(), false);
                formEntityCheckBox.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityCheckBox;
            }
            default:
//                logger.d(TAG, "Unsupported FormEntity type: " + trackedEntityAttribute.valueType());

                FormEntityText formEntityText = new FormEntityText(trackedEntityAttribute.uid(),
                        formEntityLabel);
                formEntityText.setValue("Unsupported value type: " +
                        trackedEntityAttribute.valueType(), false);

                return formEntityText;
        }
    }

    private static String getFormEntityLabel(ProgramTrackedEntityAttribute programTrackedEntityAttribute) {
        TrackedEntityAttribute trackedEntityAttribute = programTrackedEntityAttribute.trackedEntityAttribute();
        String label = isEmpty(trackedEntityAttribute.displayName()) ?
                trackedEntityAttribute.displayName() : trackedEntityAttribute.displayName();

        if (programTrackedEntityAttribute.mandatory()) {
            label = label + " (*)";
        }

        return label;
    }

    private static String getFormEntityLabel(ProgramStageDataElement stageDataElement) {
        DataElement dataElement = stageDataElement.dataElement();
        String label = isEmpty(dataElement.displayFormName()) ?
                dataElement.displayName() : dataElement.displayFormName();

        if (stageDataElement.compulsory()) {
            label = label + " (*)";
        }

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

            return formEntityFilter;
        }

        switch (dataElement.valueType()) {
            case TEXT: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(dataElement.uid(),
                        getFormEntityLabel(stageDataElement), FormEntityEditText.InputType.TEXT, dataValue);
                formEntityEditText.setValue(dataValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case LONG_TEXT: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(dataElement.uid(),
                        getFormEntityLabel(stageDataElement), FormEntityEditText.InputType.LONG_TEXT, dataValue);
                formEntityEditText.setValue(dataValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case PHONE_NUMBER: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(dataElement.uid(),
                        getFormEntityLabel(stageDataElement), FormEntityEditText.InputType.TEXT, dataValue);
                formEntityEditText.setValue(dataValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case EMAIL: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(dataElement.uid(),
                        getFormEntityLabel(stageDataElement), FormEntityEditText.InputType.TEXT, dataValue);
                formEntityEditText.setValue(dataValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case NUMBER: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(dataElement.uid(),
                        getFormEntityLabel(stageDataElement), FormEntityEditText.InputType.NUMBER, dataValue);
                formEntityEditText.setValue(dataValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case INTEGER: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(dataElement.uid(),
                        getFormEntityLabel(stageDataElement), FormEntityEditText.InputType.INTEGER, dataValue);
                formEntityEditText.setValue(dataValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case INTEGER_POSITIVE: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(dataElement.uid(),
                        getFormEntityLabel(stageDataElement), FormEntityEditText.InputType.INTEGER_POSITIVE, dataValue);
                formEntityEditText.setValue(dataValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case INTEGER_NEGATIVE: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(dataElement.uid(),
                        getFormEntityLabel(stageDataElement), FormEntityEditText.InputType.INTEGER_NEGATIVE, dataValue);
                formEntityEditText.setValue(dataValue.value(), false);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                return formEntityEditText;
            }
            case INTEGER_ZERO_OR_POSITIVE: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(dataElement.uid(),
                        getFormEntityLabel(stageDataElement), FormEntityEditText.InputType.INTEGER_ZERO_OR_POSITIVE, dataValue);
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
//                logger.d(TAG, "Unsupported FormEntity type: " + dataElement.valueType());

                FormEntityText formEntityText = new FormEntityText(dataElement.uid(),
                        getFormEntityLabel(stageDataElement));
                formEntityText.setValue("Unsupported value type: " +
                        dataElement.valueType(), false);

                return formEntityText;
        }
    }
}
