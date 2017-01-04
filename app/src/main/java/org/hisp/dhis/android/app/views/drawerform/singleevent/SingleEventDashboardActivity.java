package org.hisp.dhis.android.app.views.drawerform.singleevent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;

import org.hisp.dhis.android.app.R;
import org.hisp.dhis.android.app.SkeletonApp;
import org.hisp.dhis.android.app.views.drawerform.form.FormFragment;
import org.hisp.dhis.android.app.views.drawerform.singleevent.drawer.WidgetDrawerFragment;

import javax.inject.Inject;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

public class SingleEventDashboardActivity extends AppCompatActivity implements SingleEventDashboardView {

    private static final String ARG_EVENT_UID = "arg:itemUid";
    private static final String ARG_PROGRAM_UID = "arg:programUid";
    private static final String ARG_PROGRAM_STAGE_UID = "arg:programStageUid";
    private DrawerLayout drawerLayout;

    @Inject
    SingleEventDashboardPresenter singleEventDashboardPresenter;

    public static void navigateToItem(Activity activity, String eventUid, String programUid, String programStageUid) {
        isNull(activity, "activity must not be null");
        Intent intent = new Intent(activity, SingleEventDashboardActivity.class);
        intent.putExtra(ARG_EVENT_UID, eventUid);
        intent.putExtra(ARG_PROGRAM_UID, programUid);
        intent.putExtra(ARG_PROGRAM_STAGE_UID, programStageUid);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // if using two-pane layout (tablets in landscape mode), drawerLayout will be null
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        injectDependencies(savedInstanceState);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.data_entry_pane,
                        FormFragment.newForm(
                                getIntent().getStringExtra(ARG_EVENT_UID),
                                getIntent().getStringExtra(ARG_PROGRAM_UID),
                                getIntent().getStringExtra(ARG_PROGRAM_STAGE_UID),
                                useTwoPaneLayout()))
                .replace(R.id.navigation_view,
                        WidgetDrawerFragment.newInstance(
                                getIntent().getStringExtra(ARG_EVENT_UID),
                                getIntent().getStringExtra(ARG_PROGRAM_UID),
                                useTwoPaneLayout()))
                .commit();
    }

    private void injectDependencies(Bundle savedInstanceState) {
        SingleEventDashboardComponent component = ((SkeletonApp) getApplication()).getSingleEventDashboardComponent();

        // first time activity is created
        if (savedInstanceState == null) {
            // it means we found old component and we have to release it
            if (component != null) {
                ((SkeletonApp) getApplication()).releaseSingleEventDashboardComponent();
            }
        }

        if (component == null) {
            // create new instance of component
            component = ((SkeletonApp) getApplication()).createSingleEventDashboardComponent();
        }

        component.inject(this);

        singleEventDashboardPresenter.attachView(this);
    }

    private boolean useTwoPaneLayout() {
        return drawerLayout == null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        singleEventDashboardPresenter.detachView();
        ((SkeletonApp) getApplication()).releaseSingleEventDashboardComponent();
    }

    @Override
    public void toggleDrawerState() {
        if (drawerLayout == null) {
            return;
        }

        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawers();
        } else {
            drawerLayout.openDrawer(GravityCompat.END);
        }
    }
}
