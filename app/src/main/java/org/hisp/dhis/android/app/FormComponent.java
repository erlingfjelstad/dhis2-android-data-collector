package org.hisp.dhis.android.app;

import org.hisp.dhis.android.app.views.DataEntryFragment;
import org.hisp.dhis.android.app.views.FormSectionActivity;
import org.hisp.dhis.android.app.views.dashboard.TeiDashboardActivity;
import org.hisp.dhis.android.app.views.dashboard.dataentry.FormFragment;
import org.hisp.dhis.android.app.views.dashboard.navigation.TeiNavigationFragment;
import org.hisp.dhis.android.app.views.dashboard.navigation.event.TeiProgramStageFragment;
import org.hisp.dhis.android.app.views.dashboard.navigation.profile.TeiProfileFragment;
import org.hisp.dhis.android.app.views.dashboard.navigation.widget.TeiWidgetFragment;

import dagger.Subcomponent;

@PerActivity
@Subcomponent(
        modules = {
                FormModule.class
        }
)
public interface FormComponent {

    //------------------------------------------------------------------------
    // Injection targets
    //------------------------------------------------------------------------

    void inject(TeiDashboardActivity teiDashboardActivity);

    void inject(FormSectionActivity formSectionActivity);

    void inject(FormFragment formFragment);

    void inject(DataEntryFragment dataEntryFragment);

    void inject(TeiProgramStageFragment teiProgramStageFragment);

    void inject(TeiProfileFragment teiProfileFragment);

    void inject(TeiWidgetFragment teiWidgetFragment);

    void inject(TeiNavigationFragment teiNavigationFragment);

}
