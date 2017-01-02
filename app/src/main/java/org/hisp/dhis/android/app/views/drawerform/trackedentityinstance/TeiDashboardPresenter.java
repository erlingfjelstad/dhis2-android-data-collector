package org.hisp.dhis.android.app.views.drawerform.trackedentityinstance;

import org.hisp.dhis.android.app.views.drawerform.RightDrawerController;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.Presenter;
import org.hisp.dhis.client.sdk.ui.models.Form;

public interface TeiDashboardPresenter extends Presenter, RightDrawerController {

    void showForm(Form form);

    void lockNavigation();

    void unlockNavigation();
}
