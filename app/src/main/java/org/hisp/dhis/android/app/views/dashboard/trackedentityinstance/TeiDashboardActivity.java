package org.hisp.dhis.android.app.views.dashboard.trackedentityinstance;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;

import org.hisp.dhis.android.app.FormComponent;
import org.hisp.dhis.android.app.R;
import org.hisp.dhis.android.app.SkeletonApp;
import org.hisp.dhis.android.app.views.DashboardContextType;
import org.hisp.dhis.android.app.views.dashboard.dataentry.FormFragment;
import org.hisp.dhis.android.app.views.dashboard.navigation.TeiNavigationFragment;
import org.hisp.dhis.android.app.views.dashboard.navigation.TeiNavigationView;
import org.hisp.dhis.client.sdk.ui.activities.ReportEntitySelection;

import javax.inject.Inject;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

public class TeiDashboardActivity extends FragmentActivity implements TeiDashboardView, ReportEntitySelection {

    private static final String ARG_ITEM_UID = "arg:itemUid";
    private static final String ARG_PROGRAM_UID = "arg:programUid";
    private static final String TAG_FORM_FRAGMENT = "tag:formFragment";
    private static final String TAG_NAVIGATION_FRAGMENT = "tag:navigationFragment";
    private static final String ARG_DRAWER_STATE = "arg:drawerState";
    private static final String ARG_CONTEXT_TYPE = "arg:contextType";

    @Inject
    TeiDashboardPresenter teiDashboardPresenter;

    private String selectedUid;

    private DrawerLayout drawerLayout;


    public static void navigateToNewItem(Activity activity, String itemUid, String programUid) {
        navigateToItem(activity, itemUid, programUid, DashboardContextType.REGISTRATION);
    }

    public static void navigateTo(Activity activity, String itemUid, String programUid) {
        navigateToItem(activity, itemUid, programUid, DashboardContextType.EXISTING_ITEM);
    }

    private static void navigateToItem(Activity activity, String itemUid, String programUid, DashboardContextType contextType) {
        isNull(activity, "activity must not be null");
        Intent intent = new Intent(activity, TeiDashboardActivity.class);
        intent.putExtra(ARG_ITEM_UID, itemUid);
        intent.putExtra(ARG_PROGRAM_UID, programUid);
        intent.putExtra(ARG_CONTEXT_TYPE, contextType);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tei_dashboard);

        // if using two-pane layout (tablets in landscape mode), drawerLayout will be null
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        injectDependencies(savedInstanceState);

        teiDashboardPresenter.attachView(this);

        FormFragment formFragment;
        TeiNavigationFragment teiNavigationFragment;
        if (savedInstanceState == null) {

            if (isRegistrationComplete()) {
                formFragment = FormFragment.newForm(useTwoPaneLayout());

                // Drawer is open by default
                openDrawer();
            } else {
                formFragment = FormFragment.newEnrollmentForm(getItemUid(), getProgramUid(), useTwoPaneLayout());
                closeDrawer();
            }

            teiNavigationFragment = TeiNavigationFragment.newInstance(getItemUid(), getProgramUid(), useTwoPaneLayout(), isRegistrationComplete());


        } else {
            retainDrawerPosition(savedInstanceState);

            formFragment = retainFormFragment();
            formFragment.setMenuButtonVisibility(!useTwoPaneLayout());

            teiNavigationFragment = retainTeiNavigationFragment();
            teiNavigationFragment.setMenuButtonVisibility(!useTwoPaneLayout());
            teiNavigationFragment.setRegistrationComplete(isRegistrationComplete());
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.data_entry_pane, formFragment, TAG_FORM_FRAGMENT)
                .replace(R.id.navigation_view, teiNavigationFragment, TAG_NAVIGATION_FRAGMENT)
                .commit();
    }

    private void injectDependencies(Bundle savedInstanceState) {

        FormComponent formComponent = ((SkeletonApp) getApplication()).getFormComponent();

        // first time activity is created
        if (savedInstanceState == null) {

            // it means we found old component and we have to release it
            if (formComponent != null) {
                ((SkeletonApp) getApplication()).releaseFormComponent();
            }

            // create new instance of component
            formComponent = ((SkeletonApp) getApplication()).createFormComponent();
        }

        formComponent.inject(this);
    }

    private void retainDrawerPosition(Bundle savedInstanceState) {

        if (!useTwoPaneLayout() && savedInstanceState.containsKey(ARG_DRAWER_STATE)) {
            if (savedInstanceState.getBoolean(ARG_DRAWER_STATE)) {
                openDrawer();
            } else {
                closeDrawer();
            }
        }
    }


    private FormFragment retainFormFragment() {
        if (getSupportFragmentManager().findFragmentByTag(TAG_FORM_FRAGMENT) != null) {
            return (FormFragment) getSupportFragmentManager().findFragmentByTag(TAG_FORM_FRAGMENT);
        } else {
            if (isRegistrationComplete()) {
                return FormFragment.newForm(useTwoPaneLayout());
            } else {
                return FormFragment.newEnrollmentForm(getItemUid(), getProgramUid(), useTwoPaneLayout());
            }
        }
    }

    private TeiNavigationFragment retainTeiNavigationFragment() {
        if (getSupportFragmentManager().findFragmentByTag(TAG_NAVIGATION_FRAGMENT) != null) {
            return (TeiNavigationFragment) getSupportFragmentManager().findFragmentByTag(TAG_NAVIGATION_FRAGMENT);
        } else {
            return TeiNavigationFragment.newInstance(getItemUid(), getProgramUid(), useTwoPaneLayout(), isRegistrationComplete());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        teiDashboardPresenter.detachView();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        saveFragmentInstanceStates();
        if (drawerLayout != null) {
            outState.putBoolean(ARG_DRAWER_STATE, drawerLayout.isDrawerOpen(Gravity.RIGHT));
        }
        super.onSaveInstanceState(outState);
    }

    private void saveFragmentInstanceStates() {

        Fragment formFragment = getSupportFragmentManager().findFragmentByTag(TAG_FORM_FRAGMENT);
        if (formFragment != null) {
            getSupportFragmentManager().saveFragmentInstanceState(formFragment);
        }

        Fragment teiNavigationFragment = getSupportFragmentManager().findFragmentByTag(TAG_NAVIGATION_FRAGMENT);
        if (teiNavigationFragment != null) {
            getSupportFragmentManager().saveFragmentInstanceState(teiNavigationFragment);
        }
    }

    private boolean useTwoPaneLayout() {
        return drawerLayout == null;
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
    public void setRegistrationComplete() {
        setIntent(
                getIntent().putExtra(ARG_CONTEXT_TYPE, DashboardContextType.EXISTING_ITEM));
        retainTeiNavigationFragment().setRegistrationComplete(true);
        // When registration is complete set list of events as the selected tab in the navigation view
        retainTeiNavigationFragment().selectTab(TeiNavigationView.TAB_PROGRAM_STAGES);
    }

    @Override
    public boolean isRegistrationComplete() {
        return getIntent() == null || getIntent().getSerializableExtra(ARG_CONTEXT_TYPE) == null ||
                getIntent().getSerializableExtra(ARG_CONTEXT_TYPE) != DashboardContextType.REGISTRATION;

    }

    @Override
    public void setSelectedUid(String uid) {
        selectedUid = uid;
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
