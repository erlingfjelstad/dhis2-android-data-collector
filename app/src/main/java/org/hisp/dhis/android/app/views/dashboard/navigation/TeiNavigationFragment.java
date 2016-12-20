package org.hisp.dhis.android.app.views.dashboard.navigation;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.hisp.dhis.android.app.R;
import org.hisp.dhis.android.app.SkeletonApp;
import org.hisp.dhis.android.app.views.create.event.CreateEventActivity;
import org.hisp.dhis.android.app.views.dashboard.RightNavDrawerController;
import org.hisp.dhis.android.app.views.dashboard.navigation.event.TeiProgramStageFragment;
import org.hisp.dhis.android.app.views.dashboard.navigation.profile.TeiProfileFragment;
import org.hisp.dhis.android.app.views.dashboard.navigation.widget.TeiWidgetFragment;
import org.hisp.dhis.client.sdk.ui.models.FormEntityText;
import org.hisp.dhis.client.sdk.ui.views.FontTextView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;
import static org.hisp.dhis.android.app.views.create.event.CreateEventActivity.CREATE_EVENT_REQUEST_CODE;
import static org.hisp.dhis.client.sdk.ui.AnimationUtils.playFabShrinkPopAnimation;

public class TeiNavigationFragment extends Fragment implements TeiNavigationView {

    private static final String ARG_ITEM_UID = "arg:itemUid";
    private static final String ARG_PROGRAM_UID = "arg:programUid";
    private static final String TAG = TeiNavigationFragment.class.getSimpleName();

    private static final String ARG_TWO_PANE_LAYOUT = "arg:twoPaneLayout";
    private static final String ARG_REGISTRATION_COMPLETE = "arg:registrationComplete";

    @Inject
    TeiNavigationPresenter teiNavigationPresenter;

    @Inject
    RightNavDrawerController rightNavDrawerController;

    private FontTextView firstAttribute, secondAttribute;
    private FloatingActionButton floatingActionButton;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ArrayList<FormEntityText> appBarTeiIdentifiableFormEntities;

    public TeiNavigationFragment() {
        // Required empty public constructor
    }

    public static TeiNavigationFragment newInstance(String itemUid, String programUid, boolean twoPaneLayout, boolean registrationComplete) {
        TeiNavigationFragment fragment = new TeiNavigationFragment();
        Bundle arguments = new Bundle();
        arguments.putString(ARG_ITEM_UID, itemUid);
        arguments.putString(ARG_PROGRAM_UID, programUid);
        arguments.putBoolean(ARG_TWO_PANE_LAYOUT, twoPaneLayout);
        arguments.putBoolean(ARG_REGISTRATION_COMPLETE, registrationComplete);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard_overview, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            ((SkeletonApp) getActivity().getApplication())
                    .getFormComponent().inject(this);

            // attach view is called in this case from onCreate(),
            // in order to prevent unnecessary work which should be done
            // if case it will be i onResume()
            teiNavigationPresenter.attachView(this);
        } catch (Exception e) {
            Log.e(TAG, "Activity or Application is null. Vital resources have been killed.", e);
        }

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpFloatingActionButton(view);
        initViewPager(view);
        initAppBarLayout(view);

        teiNavigationPresenter.configureAppBar(getItemUid(), getProgramUid());
    }

    private boolean isRegistrationComplete() {
        return getArguments().containsKey(ARG_REGISTRATION_COMPLETE) && getArguments().getBoolean(ARG_REGISTRATION_COMPLETE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        teiNavigationPresenter.detachView();
    }

    private void initAppBarLayout(View view) {
        firstAttribute = (FontTextView) view.findViewById(R.id.first_attribute);
        secondAttribute = (FontTextView) view.findViewById(R.id.second_attribute);
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsingtoolbarlayout);
        final ViewPager viewPager = (ViewPager) view.findViewById(R.id.view_pager);
        collapsingToolbarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(TAB_PROFILE, true);
            }
        });

        if (!useTwoPaneLayout()) {
            ((Toolbar) view.findViewById(R.id.toolbar)).setNavigationIcon(R.drawable.ic_arrow_forward);
            ((Toolbar) view.findViewById(R.id.toolbar)).
                    setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            rightNavDrawerController.hideMenu();
                        }
                    });

        }
    }

    private boolean useTwoPaneLayout() {
        return getArguments() != null && getArguments().getBoolean(ARG_TWO_PANE_LAYOUT);
    }

    /**
     * Default icon is ic_add because teiProfileStageFragment is default
     *
     * @param view
     */
    private void setUpFloatingActionButton(View view) {
        floatingActionButton = (FloatingActionButton) view.findViewById(R.id.fab_tei_dashboard);
        floatingActionButton.setImageResource(R.drawable.ic_add);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (viewPager.getCurrentItem()) {
                    case TAB_PROGRAM_STAGES: {
                        //startActivityForResult();
                        CreateEventActivity.navigateTo(getActivity(),
                                getProgramUid(),
                                getItemUid(),
                                "");
                        break;
                    }
                    case TAB_PROFILE: {
                        teiNavigationPresenter.onProfileClick();
                        break;
                    }
                    case TAB_WIDGETS: {
                        Toast.makeText(v.getContext(), "Hello from Widgets", Toast.LENGTH_LONG).show();
                        break;
                    }
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_EVENT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (data.getIntExtra(CreateEventActivity.ARG_EVENT_TYPE, -1) == CreateEventActivity.EVENT_TYPE_SCHEDULED) {
                    // refresh list of Events? This should happen automagically if the presenter is subscribing to db update events
                }
                if (data.getIntExtra(CreateEventActivity.ARG_EVENT_TYPE, -1) == CreateEventActivity.EVENT_TYPE_ACTIVE) {

                    String programUid = data.getStringExtra(CreateEventActivity.ARG_CONTENT_ID);
                    String programStageUid = data.getStringExtra(CreateEventActivity.ARG_PROGRAM_STAGE_UID);
                    String orgUnitUid = data.getStringExtra(CreateEventActivity.ARG_ORG_UNIT_UID);
                    String enrollmentUid = data.getStringExtra(CreateEventActivity.ARG_IDENTIFIABLE_ID);

                    teiNavigationPresenter.createNewEvent(programUid, programStageUid, orgUnitUid, enrollmentUid);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initViewPager(View view) {
        viewPager = ((ViewPager) view.findViewById(R.id.view_pager));
        viewPager.setAdapter(new DashboardPageAdapter(
                getActivity().getSupportFragmentManager()));

        viewPager.setOffscreenPageLimit(2);
        tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);

        viewPager.addOnPageChangeListener(new DashboardViewPager());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (isRegistrationComplete()) {
                    viewPager.setCurrentItem(tab.getPosition());
                } else {
                    if (tab.getPosition() != TAB_WIDGETS) {
                        Toast.makeText(getContext(), R.string.please_complete_enrollment, Toast.LENGTH_SHORT).show();
                    }
                    tabLayout.getTabAt(TAB_WIDGETS).select();
                    viewPager.setCurrentItem(TAB_WIDGETS);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        if (!isRegistrationComplete()) {
            tabLayout.getTabAt(TAB_WIDGETS).select();
        }
    }

    private String getItemUid() {
        if (getArguments() == null || getArguments()
                .getString(ARG_ITEM_UID, null) == null) {
            throw new IllegalArgumentException("You must pass item uid in intent extras");
        }

        return getArguments().getString(ARG_ITEM_UID, null);
    }

    private String getProgramUid() {
        if (getArguments() == null || getArguments()
                .getString(ARG_PROGRAM_UID, null) == null) {
            throw new IllegalArgumentException("You must pass program uid in intent extras");
        }

        return getArguments().getString(ARG_PROGRAM_UID, null);
    }

    //TODO: Fix this hack and maybe move to recyclerView
    @Override
    public void populateAppBar(List<FormEntityText> formEntities) {
        storeIdentifiableFormEntities(formEntities);
        if (formEntities.size() > 1) {
            FormEntityText formEntityText1 = formEntities.get(0);
            FormEntityText formEntityText2 = formEntities.get(1);
            String displayText1 = formEntityText1.getLabel() + ": " + formEntityText1.getValue();
            String displayText2 = formEntityText2.getLabel() + ": " + formEntityText2.getValue();
            firstAttribute.setText(displayText1);
            secondAttribute.setText(displayText2);
        }
    }

    @Override
    public void setMenuButtonVisibility(boolean showButtons) {

        getArguments().putBoolean(ARG_TWO_PANE_LAYOUT, !showButtons);
        if (getView() != null) {
            if (showButtons) {
                ((Toolbar) getView().findViewById(R.id.toolbar)).setNavigationIcon(R.drawable.ic_arrow_forward);
                ((Toolbar) getView().findViewById(R.id.toolbar)).setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rightNavDrawerController.hideMenu();
                    }
                });
            } else {
                ((Toolbar) getView().findViewById(R.id.toolbar)).setNavigationIcon(null);
            }
        }
    }

    @Override
    public void setRegistrationComplete(boolean registrationComplete) {
        getArguments().putBoolean(ARG_REGISTRATION_COMPLETE, registrationComplete);
    }

    @Override
    public void selectTab(@TabPosition int position) {
        tabLayout.getTabAt(position).select();
    }

    private void storeIdentifiableFormEntities(List<FormEntityText> formEntities) {
        if (appBarTeiIdentifiableFormEntities == null) {
            appBarTeiIdentifiableFormEntities = new ArrayList<>();
        } else {
            appBarTeiIdentifiableFormEntities.clear();
        }
        appBarTeiIdentifiableFormEntities.addAll(formEntities);
    }

    private class DashboardPageAdapter extends FragmentPagerAdapter {

        public DashboardPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                default:
                case TAB_PROGRAM_STAGES:
                    return TeiProgramStageFragment.newInstance(getItemUid(), getProgramUid());
                case TAB_PROFILE:
                    return TeiProfileFragment.newInstance(getItemUid(), getProgramUid());
                case TAB_WIDGETS:
                    return new TeiWidgetFragment();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

    }

    private void setSelectedTab(int position) {
        TabLayout.Tab tab = tabLayout.getTabAt(position);
        if (tab != null) {
            tab.select();
        }
    }

    private class DashboardViewPager implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (!isRegistrationComplete()) {
                tabLayout.setScrollPosition(TAB_WIDGETS, 0, true);
            } else {
                tabLayout.setScrollPosition(position, positionOffset, true);
            }
        }

        @Override
        public void onPageSelected(int position) {
            if (!isRegistrationComplete()) {
                playFabShrinkPopAnimation(floatingActionButton);
                floatingActionButton.setVisibility(View.GONE);
                setSelectedTab(TAB_WIDGETS);
                return;
            }
            switch (position) {
                case TAB_PROGRAM_STAGES: {
                    if (floatingActionButton != null) {
                        playFabShrinkPopAnimation(floatingActionButton);
                        floatingActionButton.setImageResource(R.drawable.ic_add);
                        setSelectedTab(position);
                    }

                    break;
                }
                case TAB_PROFILE: {
                    if (floatingActionButton != null) {
                        playFabShrinkPopAnimation(floatingActionButton);
                        floatingActionButton.setImageResource(R.drawable.ic_edit_white);
                        setSelectedTab(position);
                    }
                    break;
                }
                case TAB_WIDGETS: {
                    playFabShrinkPopAnimation(floatingActionButton);
                    floatingActionButton.setVisibility(View.GONE);
                    setSelectedTab(position);
                    break;
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }
}
