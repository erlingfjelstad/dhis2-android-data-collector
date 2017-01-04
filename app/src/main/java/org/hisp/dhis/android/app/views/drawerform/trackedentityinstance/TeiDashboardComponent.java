package org.hisp.dhis.android.app.views.drawerform.trackedentityinstance;

import org.hisp.dhis.android.app.PerActivity;
import org.hisp.dhis.android.app.views.drawerform.form.FormComponent;
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
public interface TeiDashboardComponent extends FormComponent {

    //------------------------------------------------------------------------
    // Injection targets
    //------------------------------------------------------------------------

    void inject(TeiDashboardActivity teiDashboardActivity);

    void inject(TeiProgramStageFragment teiProgramStageFragment);

    void inject(TeiProfileFragment teiProfileFragment);

    void inject(TeiWidgetFragment teiWidgetFragment);

    void inject(TeiNavigationFragment teiNavigationFragment);

}
