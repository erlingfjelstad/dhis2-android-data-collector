package org.hisp.dhis.android.app.views.drawerform.singleevent;

import org.hisp.dhis.android.app.views.drawerform.eventbus.DrawerFormBus;
import org.hisp.dhis.android.app.views.drawerform.eventbus.ToggleDrawerEvent;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class SingleEventDashboardPresenterImpl implements SingleEventDashboardPresenter {

    private final DrawerFormBus eventBus;
    private SingleEventDashboardView singleEventDashboardView;

    public SingleEventDashboardPresenterImpl(DrawerFormBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void attachView(View view) {
        singleEventDashboardView = (SingleEventDashboardView) view;

        // Subscribe to event click events. Close the drawer when an event is selected
        // TODO: dont need this for single event! need to listen for button click though
        eventBus.observable(ToggleDrawerEvent.class)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ToggleDrawerEvent>() {
                    @Override
                    public void call(ToggleDrawerEvent event) {
                        singleEventDashboardView.toggleDrawerState();
                    }
                });
    }

    @Override
    public void detachView() {
        singleEventDashboardView = null;

        // Unsubscribe if we have no view to handle event changes
        eventBus.observable(ToggleDrawerEvent.class).unsubscribeOn(AndroidSchedulers.mainThread());
    }
}
