package org.hisp.dhis.android.app.views.dashboard.navigation.widget;

import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.ExpansionPanel;

import java.util.List;

public interface TeiWidgetsView extends View {

    void drawWidgets(List<ExpansionPanel> widgets);
}
