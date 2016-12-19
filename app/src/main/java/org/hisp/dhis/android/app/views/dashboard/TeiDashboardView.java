package org.hisp.dhis.android.app.views.dashboard;

import org.hisp.dhis.client.sdk.ui.bindings.views.View;

public interface TeiDashboardView extends View {

    void closeDrawer();

    void openDrawer();

    void setRegistrationComplete();

    boolean isRegistrationComplete();
}
