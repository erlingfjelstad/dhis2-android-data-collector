package org.hisp.dhis.android.app.views.drawerform.trackedentityinstance;

import org.hisp.dhis.client.sdk.ui.bindings.views.View;

public interface TeiDashboardView extends View {

    void setRegistrationComplete();

    boolean isRegistrationComplete();

    void toggleDrawerState();
}
