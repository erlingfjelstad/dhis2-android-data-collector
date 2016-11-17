package org.hisp.dhis.android.app.views.dashboard.navigation.profile;

//import org.hisp.dhis.client.sdk.android.enrollment.EnrollmentInteractor;
import org.hisp.dhis.client.sdk.core.enrollment.EnrollmentInteractor;
import org.hisp.dhis.client.sdk.core.trackedentity.TrackedEntityInstanceInteractor;
import org.hisp.dhis.client.sdk.models.enrollment.Enrollment;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.FormEntity;
import org.hisp.dhis.client.sdk.ui.models.FormEntityText;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Action1;

public class TeiProfilePresenterImpl implements TeiProfilePresenter {

    private final EnrollmentInteractor enrollmentInteractor;
    private TeiProfileView teiProfileView;

    //TODO: Maybe refactor to TeiProfilePresenterImpl(TrackedEntityInstanceInteractor teii ) { ???
    public TeiProfilePresenterImpl(EnrollmentInteractor enrollmentInteractor) {
        this.enrollmentInteractor = enrollmentInteractor;
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
    public void drawProfile(String enrollmentUid) {
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
}
