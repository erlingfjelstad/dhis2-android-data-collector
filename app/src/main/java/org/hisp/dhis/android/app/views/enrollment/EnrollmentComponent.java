package org.hisp.dhis.android.app.views.enrollment;

import org.hisp.dhis.android.app.PerActivity;

import dagger.Subcomponent;

@PerActivity
@Subcomponent(
        modules = {
                EnrollmentModule.class
        }
)
public interface EnrollmentComponent {

    //------------------------------------------------------------------------
    // Injection targets
    //------------------------------------------------------------------------

    void inject(EnrollmentActivity enrollmentActivity);

}
