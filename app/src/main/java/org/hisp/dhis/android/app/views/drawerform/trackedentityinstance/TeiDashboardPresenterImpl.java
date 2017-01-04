package org.hisp.dhis.android.app.views.drawerform.trackedentityinstance;

import org.hisp.dhis.android.app.views.drawerform.eventbus.DrawerFormBus;
import org.hisp.dhis.android.app.views.drawerform.eventbus.ToggleDrawerEvent;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class TeiDashboardPresenterImpl implements TeiDashboardPresenter {

    private final DrawerFormBus eventBus;

    TeiDashboardView teiDashboardView;

    public TeiDashboardPresenterImpl(DrawerFormBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void attachView(View view) {
        teiDashboardView = (TeiDashboardView) view;

        // Subscribe to toggle drawer events
        eventBus.observable(ToggleDrawerEvent.class)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ToggleDrawerEvent>() {
                    @Override
                    public void call(ToggleDrawerEvent event) {
                        teiDashboardView.toggleDrawerState();
                    }
                });
    }

    @Override
    public void detachView() {
        teiDashboardView = null;

        // Unsubscribe if we have no view to handle event changes
        eventBus.observable(ToggleDrawerEvent.class).unsubscribeOn(AndroidSchedulers.mainThread());
    }
}
