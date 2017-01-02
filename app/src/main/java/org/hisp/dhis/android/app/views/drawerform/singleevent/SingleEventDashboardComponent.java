
package org.hisp.dhis.android.app.views.drawerform.singleevent;

import org.hisp.dhis.android.app.PerActivity;
import org.hisp.dhis.android.app.views.drawerform.NavigationLockController;
import org.hisp.dhis.android.app.views.drawerform.form.FormFragment;
import org.hisp.dhis.android.app.views.drawerform.form.dataentry.DataEntryFragment;
import org.hisp.dhis.android.app.views.drawerform.singleevent.drawer.WidgetDrawerFragment;

import dagger.Subcomponent;

@PerActivity
@Subcomponent(
        modules = {
                SingleEventDashboardModule.class
        }
)
public interface SingleEventDashboardComponent {

    //------------------------------------------------------------------------
    // Injection targets
    //------------------------------------------------------------------------

    void inject(SingleEventDashboardActivity teiDashboardActivity);

    void inject(NavigationLockController navigationLockController);

    void inject(WidgetDrawerFragment widgetDrawerFragment);

    void inject(FormFragment formFragment);

    void inject(DataEntryFragment dataEntryFragment);

}
