package org.hisp.dhis.android.app.views.dashboard.navigation;

import org.hisp.dhis.android.app.views.dashboard.TeiDashboardPresenter;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;

/**
 * Created by thomaslindsjorn on 19/10/16.
 */

public class TeiNavigationPresenterImpl implements TeiNavigationPresenter {

    TeiNavigationView teiNavigationView;

    TeiDashboardPresenter teiDashboardPresenter;

    @Override
    public void attachView(View view) {
        teiNavigationView = (TeiNavigationView) view;
    }

    @Override
    public void detachView() {
        teiNavigationView = null;
    }
}
