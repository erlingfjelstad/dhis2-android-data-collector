package org.hisp.dhis.android.app.views.dashboard.navigation;

import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.FormEntityText;

import java.util.List;

public interface TeiNavigationView extends View {
    void populateAppBar(List<FormEntityText> formEntities);
}
