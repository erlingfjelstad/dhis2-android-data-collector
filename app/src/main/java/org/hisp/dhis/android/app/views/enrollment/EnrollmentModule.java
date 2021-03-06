package org.hisp.dhis.android.app.views.enrollment;

import android.support.annotation.Nullable;

import org.hisp.dhis.android.app.PerActivity;
import org.hisp.dhis.client.sdk.core.enrollment.EnrollmentInteractor;
import org.hisp.dhis.client.sdk.core.program.ProgramInteractor;
import org.hisp.dhis.client.sdk.core.trackedentity.TrackedEntityAttributeValueInteractor;

import dagger.Module;
import dagger.Provides;

@Module
public class EnrollmentModule {

    public EnrollmentModule() {
        // explicit empty constructor
    }

    @Provides
    @PerActivity
    public EnrollmentPresenter providesEnrollmentPresenter(
            @Nullable EnrollmentInteractor enrollmentInteractor,
            @Nullable ProgramInteractor programInteractor,
            @Nullable TrackedEntityAttributeValueInteractor trackedEntityAttributeValueInteractor) {
        return new EnrollmentPresenterImpl(enrollmentInteractor, programInteractor, trackedEntityAttributeValueInteractor);
    }
}
