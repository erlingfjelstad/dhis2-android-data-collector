package org.hisp.dhis.android.app.views.dashboard.navigation;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hisp.dhis.android.app.R;
import org.hisp.dhis.android.app.SkeletonApp;
import org.hisp.dhis.android.app.views.dashboard.navigation.event.TeiProgramStageFragment;
import org.hisp.dhis.android.app.views.dashboard.navigation.profile.TeiProfileFragment;
import org.hisp.dhis.android.app.views.dashboard.navigation.widget.TeiWidgetFragment;
import org.hisp.dhis.client.sdk.ui.models.FormEntityText;
import org.hisp.dhis.client.sdk.ui.views.FontTextView;

import java.util.List;

import javax.inject.Inject;

public class TeiNavigationFragment extends Fragment implements TeiNavigationView {
    private static final String ARG_ITEM_UID = "arg:itemUid";
    private static final String ARG_PROGRAM_UID = "arg:programUid";
    private static final String TAG = TeiNavigationFragment.class.getSimpleName();
    private static final int VIEW_PAGER_ITEM_PROGRAM_STAGES = 0;
    private static final int VIEW_PAGER_ITEM_TEI_PROFILE = 1;
    private static final int VIEW_PAGER_ITEM_WIDGETS = 2;

    @Inject
    TeiNavigationPresenter teiNavigationPresenter;

    private FontTextView firstAttribute, secondAttribute;

    public TeiNavigationFragment() {
        // Required empty public constructor
    }

    public static TeiNavigationFragment newInstance(String itemUid, String programUid) {
        TeiNavigationFragment fragment = new TeiNavigationFragment();
        Bundle arguments = new Bundle();
        arguments.putString(ARG_ITEM_UID, itemUid);
        arguments.putString(ARG_PROGRAM_UID, programUid);

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
        initViewPager(view);
        initAppBarLayout(view);

        teiNavigationPresenter.configureAppBar(getItemUid(), getProgramUid());
    }

    private void initAppBarLayout(View view) {
        firstAttribute = (FontTextView) view.findViewById(R.id.first_attribute);
        secondAttribute = (FontTextView) view.findViewById(R.id.second_attribute);
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsingtoolbarlayout);
        final ViewPager viewPager = (ViewPager) view.findViewById(R.id.view_pager);
        collapsingToolbarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(VIEW_PAGER_ITEM_TEI_PROFILE, true);
            }
        });
    }

    //TODO: Fix deprecated tabLayout.setOnTabSelectedListener
    private void initViewPager(View view) {
        final ViewPager viewPager = ((ViewPager) view.findViewById(R.id.view_pager));
        viewPager.setAdapter(new DashboardPageAdapter(
                getActivity().getSupportFragmentManager()));

        viewPager.setOffscreenPageLimit(2);
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(
                (tabLayout)));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
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
        if(formEntities.size() > 1) {
            FormEntityText formEntityText1 = formEntities.get(0);
            FormEntityText formEntityText2 = formEntities.get(1);
            String displayText1 = formEntityText1.getId() + ": " + formEntityText1.getLabel();
            String displayText2 = formEntityText2.getId() + ": " + formEntityText2.getLabel();
            firstAttribute.setText(displayText1);
            secondAttribute.setText(displayText2);
        }
    }


    private class DashboardPageAdapter extends FragmentPagerAdapter {

        public DashboardPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                default:
                case VIEW_PAGER_ITEM_PROGRAM_STAGES:
                    return TeiProgramStageFragment.newInstance(getItemUid(), getProgramUid());
                case VIEW_PAGER_ITEM_TEI_PROFILE:
                    return new TeiProfileFragment();
                case VIEW_PAGER_ITEM_WIDGETS:
                    return new TeiWidgetFragment();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

    }
}
