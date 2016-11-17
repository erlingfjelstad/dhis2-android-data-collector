package org.hisp.dhis.android.app.views.dashboard.navigation.widget;

import org.hisp.dhis.client.sdk.ui.bindings.presenters.Presenter;

/**
 * Created by thomaslindsjorn on 19/10/16.
 */

public interface TeiWidgetPresenter extends Presenter {

    void drawWidgets(String enrollmentUid);
}
