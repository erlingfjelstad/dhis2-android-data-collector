package org.hisp.dhis.android.app.views.create.identifiable;

import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.FormEntityFilter;

public interface CreateIdentifiableItemView extends View {
    void drawViews(FormEntityFilter programStageFormEntityFilter, FormEntityFilter orgUnitFormEntity);

    void itemCreated(String itemId, String identifiableId);
}
