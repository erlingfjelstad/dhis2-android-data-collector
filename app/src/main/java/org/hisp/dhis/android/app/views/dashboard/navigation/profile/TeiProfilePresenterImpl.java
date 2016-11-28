package org.hisp.dhis.android.app.views.dashboard.navigation.profile;

import org.hisp.dhis.android.app.FormUtils;
import org.hisp.dhis.client.sdk.core.ModelUtils;
import org.hisp.dhis.client.sdk.core.enrollment.EnrollmentInteractor;
import org.hisp.dhis.client.sdk.core.option.OptionSetInteractor;
import org.hisp.dhis.client.sdk.core.program.ProgramInteractor;
import org.hisp.dhis.client.sdk.core.trackedentity.TrackedEntityAttributeValueInteractor;
import org.hisp.dhis.client.sdk.core.trackedentity.TrackedEntityInstanceInteractor;
import org.hisp.dhis.client.sdk.models.enrollment.Enrollment;
import org.hisp.dhis.client.sdk.models.option.OptionSet;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.client.sdk.ui.bindings.commons.RxOnValueChangedListener;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.FormEntity;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class TeiProfilePresenterImpl implements TeiProfilePresenter {

    private final ProgramInteractor programInteractor;
    private final EnrollmentInteractor enrollmentInteractor;
    private final TrackedEntityInstanceInteractor trackedEntityInstanceInteractor;
    private final TrackedEntityAttributeValueInteractor trackedEntityAttributeValueInteractor;
    private final OptionSetInteractor optionSetInteractor;
    private TeiProfileView teiProfileView;
    private CompositeSubscription subscription;
    private final RxOnValueChangedListener onValueChangedListener;
    private static final String TAG = TeiProfilePresenter.class.getSimpleName();
    private final Logger logger;

    public TeiProfilePresenterImpl(ProgramInteractor programInteractor,
                                   EnrollmentInteractor enrollmentInteractor,
                                   TrackedEntityInstanceInteractor trackedEntityInstanceInteractor,
                                   TrackedEntityAttributeValueInteractor trackedEntityAttributeValueInteractor, OptionSetInteractor optionSetInteractor, Logger logger) {
        this.programInteractor = programInteractor;
        this.enrollmentInteractor = enrollmentInteractor;
        this.trackedEntityInstanceInteractor = trackedEntityInstanceInteractor;
        this.trackedEntityAttributeValueInteractor = trackedEntityAttributeValueInteractor;
        this.optionSetInteractor = optionSetInteractor;
        this.logger = logger;
        this.onValueChangedListener = new RxOnValueChangedListener();
    }

    @Override
    public void attachView(View view) {
        teiProfileView = (TeiProfileView) view;
    }

    @Override
    public void detachView() {
        teiProfileView = null;
    }

    @Override
    public void drawProfile(String enrollmentUid, String programUid) {

        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }

        subscription = new CompositeSubscription();

        Observable.zip(getEnrollment(enrollmentUid), getProgram(programUid),
                new Func2<Enrollment, Program, List<FormEntity>>() {

                    @Override
                    public List<FormEntity> call(
                            Enrollment enrollment, Program program) {
                        List<TrackedEntityAttributeValue> trackedEntityAttributeValues = trackedEntityAttributeValueInteractor.store().query(enrollment.trackedEntityInstance());
                        TrackedEntityInstance trackedEntityInstance = trackedEntityInstanceInteractor.store().queryByUid(enrollment.trackedEntityInstance());
                        if (trackedEntityAttributeValues != null && trackedEntityInstance != null) {
                            trackedEntityInstance = trackedEntityInstance.toBuilder().trackedEntityAttributeValues(trackedEntityAttributeValues).build();
                        }

                        List<ProgramTrackedEntityAttribute> programTrackedEntityAttributes =
                                program.programTrackedEntityAttributes();
                        List<FormEntity> formEntities = new ArrayList<>();

                        Map<String, TrackedEntityAttributeValue> trackedEntityAttributeValueMap =
                                ModelUtils.toAttributeAttributeValueMap(trackedEntityInstance.trackedEntityAttributeValues());

                        for (ProgramTrackedEntityAttribute programTrackedEntityAttribute : programTrackedEntityAttributes) {
                            if (programTrackedEntityAttribute.trackedEntityAttribute() == null) {
                                throw new RuntimeException("Malformed metadata: Program" +
                                        "Tracked entity attribute" + programTrackedEntityAttribute.uid() +
                                        " does not have reference to tracked entity attribute");
                            }

                            TrackedEntityAttributeValue attributeValue = trackedEntityAttributeValueMap.get(programTrackedEntityAttribute.trackedEntityAttribute().uid());
                            OptionSet optionSet = null;
                            if (programTrackedEntityAttribute.trackedEntityAttribute().optionSet() != null) {
                                optionSet = optionSetInteractor.store().queryByUid(programTrackedEntityAttribute.trackedEntityAttribute().optionSet().uid());
                            }
                            FormEntity formEntity = FormUtils.transformTrackedEntityAttribute(trackedEntityInstance.uid(), attributeValue, programTrackedEntityAttribute, optionSet, onValueChangedListener);
                            formEntities.add(formEntity);
                        }

                        return formEntities;
                    }
                })
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<FormEntity>>() {
                    @Override
                    public void call(List<FormEntity> formEntities) {
                        if (teiProfileView != null) {
                            teiProfileView.drawProfileItems(formEntities);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, "Something went wrong during form construction", throwable);
                    }
                });


        /*

        //something like this, but figure out how to get the tei instance.
        Enrollment e = enrollmentInteractor.store().query(enrollmentUid);
        e.trackedEntityInstance();
        TrackedEntityInstanceInteractor tei;
        tei.store().queryByUid(e.trackedEntityInstance()).trackedEntityAttributeValues();*/

        /*if (enrollmentInteractor != null) {

            enrollmentInteractor.get(enrollmentUid).subscribe(new Action1<Enrollment>() {
                @Override
                public void call(Enrollment enrollment) {
                    teiProfileView.drawProfileItems(transformTrackedEntityAttributeValues(enrollment
                            .getTrackedEntityAttributeValues()));
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    //logger.e(TAG, null, throwable);
                }
            });
        }*/
    }

    private List<FormEntity> transformTrackedEntityAttributeValues(
            List<TrackedEntityAttributeValue> trackedEntityAttributeValues) {

        if (trackedEntityAttributeValues == null || trackedEntityAttributeValues.isEmpty()) {
            return new ArrayList<>();
        }

        List<FormEntity> formEntities = new ArrayList<>();

        for (TrackedEntityAttributeValue trackedEntityAttributeValue : trackedEntityAttributeValues) {
            formEntities.add(transformTrackedEntityAttribute(trackedEntityAttributeValue));
        }

        return formEntities;
    }


    //TODO: NOTIFY listener is by default set to false
    private FormEntity transformTrackedEntityAttribute(TrackedEntityAttributeValue trackedEntityAttributeValue) {
      /*  FormEntityText formEntityText = new FormEntityText(trackedEntityAttributeValue.getTrackedEntityAttributeUId(), "");
        formEntityText.setValue(trackedEntityAttributeValue.getValue(), false);
        return formEntityText;*/
        return null;

    }

    private Observable<Enrollment> getEnrollment(final String enrollmentUid) {
        return Observable.just(enrollmentInteractor.store().query(enrollmentUid));
    }

    private Observable<Program> getProgram(final String programUid) {
        return Observable.just(programInteractor.store().queryByUid(programUid));
    }
}
