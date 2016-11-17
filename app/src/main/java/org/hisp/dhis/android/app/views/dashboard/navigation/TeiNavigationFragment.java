package org.hisp.dhis.android.app.views.dashboard.navigation;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hisp.dhis.android.app.R;
import org.hisp.dhis.android.app.views.dashboard.TeiDashboardPresenter;
import org.hisp.dhis.android.app.views.dashboard.navigation.event.TeiProgramStageFragment;
import org.hisp.dhis.android.app.views.dashboard.navigation.profile.TeiProfileFragment;
import org.hisp.dhis.android.app.views.dashboard.navigation.widget.TeiWidgetFragment;

import javax.inject.Inject;

public class TeiNavigationFragment extends Fragment implements TeiNavigationView {

    @Inject
    TeiDashboardPresenter teiDashboardPresenter;

    public TeiNavigationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard_overview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViewPager(view);
    }

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

    private class DashboardPageAdapter extends FragmentPagerAdapter {

        public DashboardPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                default:
                case 0:
                    return TeiProgramStageFragment.newInstance("Enrollment uid");
                case 1:
                    return new TeiProfileFragment();
                case 2:
                    return new TeiWidgetFragment();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

    }
}
