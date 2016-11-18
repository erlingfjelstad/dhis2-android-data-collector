package org.hisp.dhis.android.app.views.dashboard;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;

//import org.hisp.dhis.android.app.ActivityComponent;
import org.hisp.dhis.android.app.R;
import org.hisp.dhis.android.app.SkeletonApp;
import org.hisp.dhis.android.app.views.dashboard.dataentry.DataEntryContainerFragment;
import org.hisp.dhis.android.app.views.dashboard.navigation.TeiNavigationFragment;
import org.hisp.dhis.client.sdk.ui.activities.ReportEntitySelection;

import javax.inject.Inject;

public class TeiDashboardActivity extends FragmentActivity implements TeiDashboardView, ReportEntitySelection {

    @Inject
    TeiDashboardPresenter teiDashboardPresenter;
    private DrawerLayout drawerLayout;
    private String selectedUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tei_dashboard);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.data_entry_pane, new DataEntryContainerFragment())
                .commit();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.navigation_view, new TeiNavigationFragment())
                .commit();

        // if using two-pane layout (tablets in landscape mode), drawerLayout will be null
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
/*
//TODO: fix injection issues:
        ActivityComponent activityComponent = ((SkeletonApp) getApplication()).getActivityComponent();

        // first time activity is created
        if (savedInstanceState == null) {
            // it means we found old component and we have to release it
            if (activityComponent != null) {
                // create new instance of component
                ((TrackerCaptureApp) getApplication()).releaseFormComponent();
            }

            activityComponent = ((TrackerCaptureApp) getApplication()).createActivityComponent();
        } else {
            activityComponent = ((TrackerCaptureApp) getApplication()).getActivityComponent();
        }

        // inject dependencies
        activityComponent.inject(this);*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        teiDashboardPresenter.attachView(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        teiDashboardPresenter.detachView();
    }

    private boolean useTwoPaneLayout() {
        return getResources().getBoolean(R.bool.use_two_pane_layout);
    }

    @Override
    public void closeDrawer() {
        if (drawerLayout != null) {
            drawerLayout.closeDrawers();
        }
    }

    @Override
    public void openDrawer() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.END);
        }
    }

    @Override
    public void setSelectedUid(String uid) {
        selectedUid = uid;
        closeDrawer();
    }

    @Override
    public String getSelectedUid() {
        return selectedUid;
    }
}
