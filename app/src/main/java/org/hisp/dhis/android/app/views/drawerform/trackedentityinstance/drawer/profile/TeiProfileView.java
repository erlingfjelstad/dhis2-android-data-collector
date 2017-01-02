package org.hisp.dhis.android.app.views.drawerform.trackedentityinstance.drawer.profile;

import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.FormEntity;

import java.util.List;

public interface TeiProfileView extends View {
    void update(List<FormEntity> formEntities);

    void toggleLockStatus();
}
