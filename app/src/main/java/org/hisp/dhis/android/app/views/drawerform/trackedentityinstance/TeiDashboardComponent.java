package org.hisp.dhis.android.app.views.drawerform.trackedentityinstance;

import org.hisp.dhis.android.app.PerActivity;
import org.hisp.dhis.android.app.views.drawerform.NavigationLockController;
import org.hisp.dhis.android.app.views.drawerform.form.FormFragment;
import org.hisp.dhis.android.app.views.drawerform.form.dataentry.DataEntryFragment;
import org.hisp.dhis.android.app.views.drawerform.trackedentityinstance.drawer.TeiNavigationFragment;
import org.hisp.dhis.android.app.views.drawerform.trackedentityinstance.drawer.event.TeiProgramStageFragment;
import org.hisp.dhis.android.app.views.drawerform.trackedentityinstance.drawer.profile.TeiProfileFragment;
import org.hisp.dhis.android.app.views.drawerform.trackedentityinstance.drawer.widget.TeiWidgetFragment;

import dagger.Subcomponent;

@PerActivity
@Subcomponent(
        modules = {
                TeiDashboardModule.class
        }
)
public interface TeiDashboardComponent {

    //------------------------------------------------------------------------
    // Injection targets
    //------------------------------------------------------------------------

    void inject(TeiDashboardActivity teiDashboardActivity);

    void inject(TeiProgramStageFragment teiProgramStageFragment);

    void inject(TeiProfileFragment teiProfileFragment);

    void inject(TeiWidgetFragment teiWidgetFragment);

    void inject(TeiNavigationFragment teiNavigationFragment);

    void inject(FormFragment formFragment);

    void inject(NavigationLockController navigationLockController);

    void inject(DataEntryFragment dataEntryFragment);

}
