package org.hisp.dhis.android.app.views.drawerform.singleevent.drawer;

import org.hisp.dhis.android.app.views.drawerform.singleevent.SingleEventDashboardPresenter;
import org.hisp.dhis.client.sdk.core.event.EventInteractor;
import org.hisp.dhis.client.sdk.core.program.ProgramInteractor;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.utils.Logger;

public class WidgetDrawerPresenterImpl implements WidgetDrawerPresenter {

    WidgetDrawerView widgetDrawerView;

    public WidgetDrawerPresenterImpl(
            SingleEventDashboardPresenter singleEventDashboardPresenter,
            EventInteractor eventInteractor,
            ProgramInteractor programInteractor,
            Logger logger) {
    }

    @Override
    public void attachView(View view) {
        widgetDrawerView = (WidgetDrawerView) view;
    }

    @Override
    public void detachView() {
        widgetDrawerView = null;
    }
}
