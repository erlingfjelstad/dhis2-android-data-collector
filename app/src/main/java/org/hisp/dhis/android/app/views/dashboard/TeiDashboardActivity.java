package org.hisp.dhis.android.app.views.dashboard;

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
import org.hisp.dhis.android.app.views.dashboard.dataentry.FormFragment;
import org.hisp.dhis.android.app.views.dashboard.navigation.TeiNavigationFragment;
import org.hisp.dhis.client.sdk.ui.activities.ReportEntitySelection;

import javax.inject.Inject;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

public class TeiDashboardActivity extends FragmentActivity implements TeiDashboardView, ReportEntitySelection {

    private static final String ARG_ITEM_UID = "arg:itemUid";
    private static final String ARG_PROGRAM_UID = "arg:programUid";
    private static final String TAG_FORM_FRAGMENT = "tag:formFragment";
    private static final String TAG_NAVIGATION_FRAGMENT = "tag:navigationFragment";
    private static final String ARG_DRAWER_STATE = "arg:drawerState";
    @Inject
    TeiDashboardPresenter teiDashboardPresenter;

    private String selectedUid;

    private DrawerLayout drawerLayout;


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

        // if using two-pane layout (tablets in landscape mode), drawerLayout will be null
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        injectDependencies(savedInstanceState);

        teiDashboardPresenter.attachView(this);

        FormFragment formFragment;
        TeiNavigationFragment teiNavigationFragment;

        if (savedInstanceState == null) {

            formFragment = FormFragment.newInstance(useTwoPaneLayout());
            teiNavigationFragment = TeiNavigationFragment.newInstance(getItemUid(), getProgramUid(), useTwoPaneLayout());

            // Drawer is open by default
            openDrawer();

        } else {
            retainDrawerPosition(savedInstanceState);
            formFragment = retainFormFragment();
            formFragment.setMenuButtonVisibility(!useTwoPaneLayout());
            teiNavigationFragment = retainTeiNavigationFragment();
            teiNavigationFragment.setMenuButtonVisibility(!useTwoPaneLayout());
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

        if (useTwoPaneLayout()) {
            teiDashboardPresenter.hideMenuButtons();
            return;
        } else if (savedInstanceState.containsKey(ARG_DRAWER_STATE)) {
            if (savedInstanceState.getBoolean(ARG_DRAWER_STATE)) {
                openDrawer();
            } else {
                closeDrawer();
            }
        }

        teiDashboardPresenter.showMenuButtons();
    }


    private FormFragment retainFormFragment() {
        if (getSupportFragmentManager().findFragmentByTag(TAG_FORM_FRAGMENT) != null) {
            return (FormFragment) getSupportFragmentManager().findFragmentByTag(TAG_FORM_FRAGMENT);
        } else {
            return FormFragment.newInstance(useTwoPaneLayout());
        }
    }

    private TeiNavigationFragment retainTeiNavigationFragment() {
        if (getSupportFragmentManager().findFragmentByTag(TAG_NAVIGATION_FRAGMENT) != null) {
            return (TeiNavigationFragment) getSupportFragmentManager().findFragmentByTag(TAG_NAVIGATION_FRAGMENT);
        } else {
            return TeiNavigationFragment.newInstance(getItemUid(), getProgramUid(), useTwoPaneLayout());
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
        if (getSupportFragmentManager().findFragmentByTag(TAG_FORM_FRAGMENT) != null) {
            Fragment formFragment = getSupportFragmentManager().findFragmentByTag(TAG_FORM_FRAGMENT);
            getSupportFragmentManager().saveFragmentInstanceState(formFragment);
        }
        if (getSupportFragmentManager().findFragmentByTag(TAG_NAVIGATION_FRAGMENT) != null) {
            Fragment teiNavigationFragment = getSupportFragmentManager().findFragmentByTag(TAG_NAVIGATION_FRAGMENT);
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
