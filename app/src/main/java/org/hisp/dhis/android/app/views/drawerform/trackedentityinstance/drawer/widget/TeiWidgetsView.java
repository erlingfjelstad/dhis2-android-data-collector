package org.hisp.dhis.android.app.views.drawerform.trackedentityinstance.drawer.widget;

import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.ExpansionPanel;

import java.util.List;

public interface TeiWidgetsView extends View {

    void drawWidgets(List<ExpansionPanel> widgets);
}
