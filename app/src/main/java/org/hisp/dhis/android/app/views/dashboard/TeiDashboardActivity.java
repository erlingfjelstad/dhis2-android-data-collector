package org.hisp.dhis.android.app.views.dashboard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;

import org.hisp.dhis.android.app.FormComponent;
import org.hisp.dhis.android.app.R;
import org.hisp.dhis.android.app.SkeletonApp;
import org.hisp.dhis.android.app.views.dashboard.navigation.TeiNavigationFragment;
import org.hisp.dhis.client.sdk.ui.activities.ReportEntitySelection;

import javax.inject.Inject;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

public class TeiDashboardActivity extends FragmentActivity implements TeiDashboardView, ReportEntitySelection {

    private static final String ARG_ITEM_UID = "arg:itemUid";
    private static final String ARG_PROGRAM_UID = "arg:programUid";
    @Inject
    TeiDashboardPresenter teiDashboardPresenter;

    private DrawerLayout drawerLayout;
    private String selectedUid;


    public static void navigateTo(Activity activity, String itemUid, String programUid) {
        navigateToItem(activity, itemUid, programUid);
    }

    private static void navigateToItem(Activity activity, String itemUid, String programUid) {
        isNull(activity, "activity must not be null");

        Intent intent = new Intent(activity, TeiDashboardActivity.class);
        intent.putExtra(ARG_ITEM_UID, itemUid);
        intent.putExtra(ARG_PROGRAM_UID, programUid);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tei_dashboard);

//        getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.data_entry_pane, new DataEntryContainerFragment())
//                .commit();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.data_entry_pane, TeiNavigationFragment.newInstance(getItemUid(), getProgramUid()))
                .commit();

        // if using two-pane layout (tablets in landscape mode), drawerLayout will be null
//        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        FormComponent formComponent = ((SkeletonApp) getApplication()).getFormComponent();

        // first time activity is created
        if (savedInstanceState == null) {
            // it means we found old component and we have to release it
            if (formComponent != null) {
                // create new instance of component
                ((SkeletonApp) getApplication()).releaseFormComponent();
            }

            formComponent = ((SkeletonApp) getApplication()).createFormComponent();
        } else {
            formComponent = ((SkeletonApp) getApplication()).getFormComponent();
        }

        // inject dependencies
        formComponent.inject(this);

//        teiDashboardPresenter.configureAppBar(getItemUid(), getProgramUid() );
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
//        if (drawerLayout != null) {
//            drawerLayout.closeDrawers();
//        }
    }

    @Override
    public void openDrawer() {
//        if (drawerLayout != null) {
//            drawerLayout.openDrawer(GravityCompat.END);
//        }
    }

    @Override
    public void setSelectedUid(String uid) {
        selectedUid = uid;
//        closeDrawer();
    }

    @Override
    public String getSelectedUid() {
        return selectedUid;
    }

    private String getItemUid() {
        if (getIntent().getExtras() == null || getIntent().getExtras()
                .getString(ARG_ITEM_UID, null) == null) {
            throw new IllegalArgumentException("You must pass item uid in intent extras");
        }

        return getIntent().getExtras().getString(ARG_ITEM_UID, null);
    }

    private String getProgramUid() {
        if (getIntent().getExtras() == null || getIntent().getExtras()
                .getString(ARG_PROGRAM_UID, null) == null) {
            throw new IllegalArgumentException("You must pass program uid in intent extras");
        }

        return getIntent().getExtras().getString(ARG_PROGRAM_UID, null);
    }
}
