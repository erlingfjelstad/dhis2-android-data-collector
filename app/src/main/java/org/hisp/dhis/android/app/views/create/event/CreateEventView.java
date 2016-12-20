package org.hisp.dhis.android.app.views.create.event;

import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.FormEntityFilter;

public interface CreateEventView extends View {
    void drawViews(FormEntityFilter programStageFormEntityFilter, FormEntityFilter orgUnitFormEntity);
}
