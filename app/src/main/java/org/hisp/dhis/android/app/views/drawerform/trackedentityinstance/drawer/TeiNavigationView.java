package org.hisp.dhis.android.app.views.drawerform.trackedentityinstance.drawer;

import android.support.annotation.IntDef;

import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.FormEntityText;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public interface TeiNavigationView extends View {

    int TAB_PROGRAM_STAGES = 0;
    int TAB_PROFILE = 1;
    int TAB_WIDGETS = 2;

    void populateAppBar(List<FormEntityText> formEntities);

    void setMenuButtonVisibility(boolean showMenuButton);

    void setRegistrationComplete(boolean registrationComplete);

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            TAB_PROGRAM_STAGES,
            TAB_PROFILE,
            TAB_WIDGETS
    })
    @interface TabPosition {
    }

    void selectTab(@TabPosition int position);
}
