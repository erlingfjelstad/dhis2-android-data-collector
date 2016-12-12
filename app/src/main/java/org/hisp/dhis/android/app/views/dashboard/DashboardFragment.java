package org.hisp.dhis.android.app.views.dashboard;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.hisp.dhis.android.app.R;
import org.hisp.dhis.android.app.SkeletonApp;
import org.hisp.dhis.android.app.views.SelectorFragment;
import org.hisp.dhis.android.app.views.selectedcontent.SelectedContentActivity;
import org.hisp.dhis.client.sdk.ui.adapters.ContentEntityAdapter;
import org.hisp.dhis.client.sdk.ui.adapters.ReportEntityAdapter;
import org.hisp.dhis.client.sdk.ui.fragments.BaseFragment;
import org.hisp.dhis.client.sdk.ui.models.ContentEntity;
import org.hisp.dhis.client.sdk.ui.views.DividerDecoration;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

public class DashboardFragment extends BaseFragment implements DashboardView, Toolbar.OnMenuItemClickListener {

    private static final String TAG = DashboardFragment.class.getSimpleName();
    private static final String LAYOUT_MANAGER_KEY = "LAYOUT_MANAGER_KEY";
    private static final String STATE_IS_REFRESHING = "state:isRefreshing";

    @Inject
    DashboardPresenter dashboardPresenter;

    @Inject
    Logger logger;

    private RecyclerView recyclerView;
    private ContentEntityAdapter contentEntityAdapter;
    private AlertDialog alertDialog;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SearchView searchView;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        ((SkeletonApp) getActivity().getApplication()).getUserComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = (RecyclerView) view.findViewById(R.id.dashboard_recyclerview);
        setupToolbar();
        setupAdapter();
        setupSwipeRefreshLayout(view, savedInstanceState);

        recyclerView.setAdapter(contentEntityAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(layoutManager);
        if (savedInstanceState != null) {
            layoutManager.onRestoreInstanceState(savedInstanceState.getParcelable(LAYOUT_MANAGER_KEY));
        }

        if (savedInstanceState != null) {
            contentEntityAdapter.onRestoreInstanceState(
                    savedInstanceState.getBundle(ReportEntityAdapter.REPORT_ENTITY_LIST_KEY));
        }

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerDecoration(
                ContextCompat.getDrawable(getActivity(), R.drawable.divider)));

        dashboardPresenter.populateDashboard();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_dashboard, menu);

        final MenuItem filter = menu.findItem(R.id.action_search);
        searchView = (SearchView) filter.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!searchView.isIconified()) {
                    searchView.setIconified(true);
                }

                filter.collapseActionView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                contentEntityAdapter.filter(s);
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }


    private void setupSwipeRefreshLayout(final View rootView, final Bundle savedInstanceState) {
        swipeRefreshLayout = (SwipeRefreshLayout) rootView
                .findViewById(R.id.swiperefreshlayout_dashboard);
        swipeRefreshLayout.setColorSchemeResources(R.color.color_primary_default);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                sync();
            }
        });

        if (savedInstanceState != null) {
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(savedInstanceState
                            .getBoolean(STATE_IS_REFRESHING, false));
                }
            });
        }
    }

    private void sync() {
        try {
            dashboardPresenter.sync();
        } catch (IOException e) {
            dashboardPresenter.handleError(e);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        dashboardPresenter.detachView();
    }

    @Override
    public void onResume() {
        super.onResume();
        dashboardPresenter.attachView(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_IS_REFRESHING, swipeRefreshLayout.isRefreshing());

        outState.putParcelable(ReportEntityAdapter.REPORT_ENTITY_LIST_KEY, contentEntityAdapter.onSaveInstanceState());
        outState.putParcelable(LAYOUT_MANAGER_KEY, recyclerView.getLayoutManager().onSaveInstanceState());

    }

    @Override
    protected void toggleNavigationDrawer() {
        super.toggleNavigationDrawer();
    }

    @Nullable
    @Override
    protected Toolbar getParentToolbar() {
        return super.getParentToolbar();
    }

    @Override
    public boolean onBackPressed() {
        return super.onBackPressed();
    }

    private void setupToolbar() {
        Drawable buttonDrawable = DrawableCompat.wrap(ContextCompat
                .getDrawable(getActivity(), R.drawable.ic_menu));
        DrawableCompat.setTint(buttonDrawable, ContextCompat
                .getColor(getContext(), android.R.color.white));
        if (getParentToolbar() != null) {
            getParentToolbar().inflateMenu(R.menu.menu_dashboard);
            getParentToolbar().setNavigationIcon(buttonDrawable);
            getParentToolbar().setOnMenuItemClickListener(this);
        }
    }

    private void setupAdapter() {
        contentEntityAdapter = new ContentEntityAdapter(getActivity());
        contentEntityAdapter.setOnContentItemClickListener(new ContentEntityAdapter.OnContentItemClicked() {
            @Override
            public void onContentItemClicked(ContentEntity contentEntity) {
                SelectedContentActivity.navigateTo(getActivity(),
                        contentEntity.getId(), contentEntity.getTitle(), contentEntity.getType());
            }
        });
    }

    @Override
    public void swapData(List<ContentEntity> contentEntities) {
        this.contentEntityAdapter.swapData(contentEntities);
    }

    @Override
    public void showProgressBar() {
        logger.d(SelectorFragment.class.getSimpleName(), "showProgressBar()");

        // this workaround is necessary because of the message queue
        // implementation in android. If you will try to setRefreshing(true) right away,
        // this call will be placed in UI message queue by SwipeRefreshLayout BEFORE
        // message to hide progress bar which probably is created by layout
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    @Override
    public void hideProgressBar() {
        logger.d(SelectorFragment.class.getSimpleName(), "hideProgressBar()");
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void showError(String message) {
        showErrorDialog(getString(R.string.title_error), message);
    }

    @Override
    public void showUnexpectedError(String message) {
        showErrorDialog(getString(R.string.title_error_unexpected), message);
    }

    private void showErrorDialog(String title, String message) {
        if (alertDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setPositiveButton(R.string.option_confirm, null);
            alertDialog = builder.create();
        }
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }
}
