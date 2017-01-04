
package org.hisp.dhis.android.app.views.drawerform.singleevent;

import org.hisp.dhis.android.app.PerActivity;
import org.hisp.dhis.android.app.views.drawerform.form.FormComponent;
import org.hisp.dhis.android.app.views.drawerform.singleevent.drawer.WidgetDrawerFragment;

import dagger.Subcomponent;

@PerActivity
@Subcomponent(
        modules = {
                SingleEventDashboardModule.class
        }
)
public interface SingleEventDashboardComponent extends FormComponent {

    //------------------------------------------------------------------------
    // Injection targets
    //------------------------------------------------------------------------

    void inject(SingleEventDashboardActivity teiDashboardActivity);

    void inject(WidgetDrawerFragment widgetDrawerFragment);

}
