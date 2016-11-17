package org.hisp.dhis.android.app.views.dashboard.navigation.profile;

import org.hisp.dhis.client.sdk.ui.bindings.presenters.Presenter;

public interface TeiProfilePresenter extends Presenter {
    void drawProfile(String enrollmentUid);
}
