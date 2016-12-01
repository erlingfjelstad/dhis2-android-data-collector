package org.hisp.dhis.android.app.views.dashboard.navigation;

import org.hisp.dhis.android.app.views.dashboard.navigation.profile.TeiProfilePresenter;
import org.hisp.dhis.client.sdk.core.enrollment.EnrollmentInteractor;
import org.hisp.dhis.client.sdk.core.program.ProgramInteractor;
import org.hisp.dhis.client.sdk.core.trackedentity.TrackedEntityAttributeValueInteractor;
import org.hisp.dhis.client.sdk.core.trackedentity.TrackedEntityInstanceInteractor;
import org.hisp.dhis.client.sdk.models.enrollment.Enrollment;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.FormEntityText;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by thomaslindsjorn on 19/10/16.
 */

public class TeiNavigationPresenterImpl implements TeiNavigationPresenter {

    private static final String TAG = TeiNavigationPresenterImpl.class.getSimpleName();
    TeiNavigationView teiNavigationView;
    private CompositeSubscription subscription;
    TeiProfilePresenter teiProfilePresenter;

    private final EnrollmentInteractor enrollmentInteractor;
    private final TrackedEntityInstanceInteractor trackedEntityInstanceInteractor;
    private final TrackedEntityAttributeValueInteractor trackedEntityAttributeValueInteractor;
    private final ProgramInteractor programInteractor;
    private final Logger logger;

    public TeiNavigationPresenterImpl(EnrollmentInteractor enrollmentInteractor,
                                      TrackedEntityInstanceInteractor trackedEntityInstanceInteractor,
                                      TrackedEntityAttributeValueInteractor trackedEntityAttributeValueInteractor,
                                      ProgramInteractor programInteractor, Logger logger) {
        this.enrollmentInteractor = enrollmentInteractor;
        this.trackedEntityInstanceInteractor = trackedEntityInstanceInteractor;
        this.trackedEntityAttributeValueInteractor = trackedEntityAttributeValueInteractor;
        this.programInteractor = programInteractor;
        this.logger = logger;
    }

    @Override
    public void attachView(View view) {
        teiNavigationView = (TeiNavigationView) view;
    }

    @Override
    public void detachView() {
        teiNavigationView = null;
    }

    @Override
    public void configureAppBar(String enrollmentUid, String programUid) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }

        subscription = new CompositeSubscription();

        subscription.add(Observable.zip(getEnrollment(enrollmentUid), getProgram(programUid),
                new Func2<Enrollment, Program, List<FormEntityText>>() {
                    @Override
                    public List<FormEntityText> call(Enrollment enrollment, Program program) {
                        TrackedEntityInstance trackedEntityInstance = trackedEntityInstanceInteractor.store().queryByUid(enrollment.trackedEntityInstance());
                        List<TrackedEntityAttributeValue> trackedEntityAttributeValues = trackedEntityAttributeValueInteractor.store().query(trackedEntityInstance.uid());
                        Map<String, TrackedEntityAttribute> trackedEntityAttributeMap = new HashMap<>();
                        List<FormEntityText> formEntities = new ArrayList<>();

                        if (program != null && program.programTrackedEntityAttributes() != null && !program.programTrackedEntityAttributes().isEmpty()) {
                            for (ProgramTrackedEntityAttribute programTrackedEntityAttribute : program.programTrackedEntityAttributes()) {
                                if (programTrackedEntityAttribute.displayInList() != null && programTrackedEntityAttribute.displayInList()) {
                                    trackedEntityAttributeMap.put(programTrackedEntityAttribute.trackedEntityAttribute().uid(), programTrackedEntityAttribute.trackedEntityAttribute());
                                }
                            }
                        }
                        if (trackedEntityAttributeValues != null && !trackedEntityAttributeValues.isEmpty()) {
                            for (TrackedEntityAttributeValue trackedEntityAttributeValue : trackedEntityAttributeValues) {
                                if (trackedEntityAttributeMap.containsKey(trackedEntityAttributeValue.trackedEntityAttributeUid())) {
                                    TrackedEntityAttribute trackedEntityAttribute = trackedEntityAttributeMap.get(trackedEntityAttributeValue.trackedEntityAttributeUid());
                                    if (trackedEntityAttribute.displayName() != null) {
                                        FormEntityText formEntityText = new FormEntityText(trackedEntityAttribute.displayName(), trackedEntityAttributeValue.value());
                                        formEntities.add(formEntityText);
                                    }
                                }
                            }
                        }

                        return formEntities;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<FormEntityText>>() {
                    @Override
                    public void call(List<FormEntityText> formEntities) {
                        if (teiNavigationView != null) {
                            teiNavigationView.populateAppBar(formEntities);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, "Something went wrong during view construction", throwable);
                    }
                }));


    }

    @Override
    public void onProfileClick() {
        teiProfilePresenter.toggleLockStatus();
    }

    @Override
    public void attachProfilePresenter(TeiProfilePresenter teiProfilePresenter) {
        this.teiProfilePresenter = teiProfilePresenter;
    }

    private Observable<Enrollment> getEnrollment(final String enrollmentUid) {
        return Observable.just(enrollmentInteractor.store().query(enrollmentUid));
    }

    private Observable<Program> getProgram(final String programUid) {
        return Observable.just(programInteractor.store().queryByUid(programUid));
    }
}
