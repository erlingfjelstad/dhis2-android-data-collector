package org.hisp.dhis.android.app.views.enrollment;

import android.support.annotation.Nullable;

import org.hisp.dhis.android.app.PerActivity;
import org.hisp.dhis.client.sdk.core.enrollment.EnrollmentInteractor;
import org.hisp.dhis.client.sdk.core.program.ProgramInteractor;
import org.hisp.dhis.client.sdk.core.trackedentity.TrackedEntityAttributeValueInteractor;
import org.hisp.dhis.client.sdk.core.trackedentity.TrackedEntityInstanceInteractor;
import org.hisp.dhis.client.sdk.utils.Logger;

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
            @Nullable TrackedEntityInstanceInteractor trackedEntityInstanceInteractor,
            @Nullable TrackedEntityAttributeValueInteractor trackedEntityAttributeValueInteractor,
            @Nullable Logger logger) {
        return new EnrollmentPresenterImpl(enrollmentInteractor, programInteractor, trackedEntityInstanceInteractor, trackedEntityAttributeValueInteractor, logger);
    }
}
