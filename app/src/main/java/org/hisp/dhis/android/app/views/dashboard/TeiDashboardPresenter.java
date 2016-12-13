package org.hisp.dhis.android.app.views.dashboard;

import org.hisp.dhis.client.sdk.ui.bindings.presenters.Presenter;
import org.hisp.dhis.client.sdk.ui.models.Form;

public interface TeiDashboardPresenter extends Presenter {

    void hideMenu();

    void showMenu();

    void showForm(Form form);

    void refreshMenuButtonVisibility(boolean showMenuButtons);

    void hideMenuButtons();

    void showMenuButtons();
}
