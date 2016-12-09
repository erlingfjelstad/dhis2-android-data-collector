package org.hisp.dhis.android.app.views.dashboard.navigation.event;

import android.support.v4.app.FragmentActivity;

import org.hisp.dhis.android.app.views.FormSectionContextType;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.Presenter;

public interface TeiProgramStagePresenter extends Presenter {

    void drawProgramStages(String enrollmentUid, String programUid);

    void onEventClicked(String eventUid);

    void navigateTo(String itemUid);

    void navigateToExistingItem(FragmentActivity activity, String itemUid, String programUid,
                                String programStageUid, FormSectionContextType report);
}
