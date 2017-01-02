package org.hisp.dhis.android.app.views.drawerform;

public class RightDrawerControllerImpl implements RightDrawerController {

    private final RightDrawerController presenter;

    public RightDrawerControllerImpl(RightDrawerController presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showMenu() {
        presenter.showMenu();
    }

    @Override
    public void hideMenu() {
        presenter.hideMenu();
    }

}