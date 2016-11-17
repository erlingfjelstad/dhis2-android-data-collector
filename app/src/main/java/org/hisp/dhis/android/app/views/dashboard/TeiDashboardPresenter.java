package org.hisp.dhis.android.app.views.dashboard;

import org.hisp.dhis.client.sdk.ui.bindings.presenters.Presenter;

public interface TeiDashboardPresenter extends Presenter {

    void hideMenu();

    void showDataEntryForEvent(String eventid);
}
