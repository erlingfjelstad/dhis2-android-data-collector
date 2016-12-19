package org.hisp.dhis.android.app.views.dashboard.navigation.event;

import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.ExpansionPanel;

import java.util.List;

public interface TeiProgramStageView extends View {

    void drawProgramStages(List<ExpansionPanel> programStages);
}
