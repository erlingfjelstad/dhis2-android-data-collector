package org.hisp.dhis.android.app.views.dashboard.navigation.profile;

import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.FormEntity;

import java.util.List;

public interface TeiProfileView extends View {
    void drawProfileItems(List<FormEntity> formEntities);
}
