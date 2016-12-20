package org.hisp.dhis.android.app.views.selectedcontent;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import org.hisp.dhis.android.app.R;
import org.hisp.dhis.android.app.SkeletonApp;
import org.hisp.dhis.android.app.views.FormSectionActivity;
import org.hisp.dhis.android.app.views.FormSectionContextType;
import org.hisp.dhis.android.app.views.create.event.CreateEventActivity;
import org.hisp.dhis.android.app.views.create.identifiable.CreateIdentifiableItemActivity;
import org.hisp.dhis.android.app.views.enrollment.EnrollmentActivity;
import org.hisp.dhis.client.sdk.ui.adapters.ReportEntityAdapter;
import org.hisp.dhis.client.sdk.ui.models.ContentEntity;
import org.hisp.dhis.client.sdk.ui.models.ReportEntity;
import org.hisp.dhis.client.sdk.ui.models.ReportEntityFilter;
import org.hisp.dhis.client.sdk.ui.views.DividerDecoration;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

public class SelectedContentFragment extends Fragment implements SelectedContentView, Toolbar.OnMenuItemClickListener {
    private static final String TAG = SelectedContentFragment.class.getSimpleName();
    private static final String LAYOUT_MANAGER_KEY = "LAYOUT_MANAGER_KEY";

    private static final String ARG_CONTENT_ID = "arg:contentId";
    private static final String ARG_CONTENT_TITLE = "arg:contentTitle";
    private static final String ARG_CONTENT_TYPE = "arg:contentType";
    private static final String STATE_IS_REFRESHING = "state:isRefreshing";

    private SwipeRefreshLayout swipeRefreshLayout;
    private AlertDialog alertDialog;
    private SearchView searchView;

    public static SelectedContentFragment newInstance(String id, String title, String type) {
        SelectedContentFragment fragment = new SelectedContentFragment();
        Bundle args = new Bundle();

        args.putString(ARG_CONTENT_ID, id);
        args.putString(ARG_CONTENT_TITLE, title);
        args.putString(ARG_CONTENT_TYPE, type);
        fragment.setArguments(args);

        return fragment;
    }

    @Inject
    SelectedContentPresenter selectedContentPresenter;

    @Inject
    Logger logger;

    private RecyclerView recyclerView;
    private ReportEntityAdapter reportEntityAdapter;
    private FloatingActionButton floatingActionButton;
    private FloatingActionMenu floatingActionMenu;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        ((SkeletonApp) getActivity().getApplication())
                .getUserComponent().inject(this);

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_selected_content, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar(view);
        setUpFloatingActionButton(view);
        setupSwipeRefreshLayout(view, savedInstanceState);
        setupReportEntityRecyclerView(view, savedInstanceState);
    }

    private void setupReportEntityRecyclerView(View view, Bundle savedInstanceState) {
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_items);

        setupAdapter();
        recyclerView.setAdapter(reportEntityAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        if (savedInstanceState != null) {
            layoutManager.onRestoreInstanceState(savedInstanceState.getParcelable(LAYOUT_MANAGER_KEY));
        }

        if (savedInstanceState != null) {
            reportEntityAdapter.onRestoreInstanceState(
                    savedInstanceState.getBundle(ReportEntityAdapter.REPORT_ENTITY_LIST_KEY));
        }

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerDecoration(
                ContextCompat.getDrawable(getActivity(), R.drawable.divider)));
    }

    private void setupAdapter() {
        reportEntityAdapter = new ReportEntityAdapter(getActivity());
        reportEntityAdapter.setHideLabelsWithoutValues(true);
        reportEntityAdapter.setOnReportEntityInteractionListener(new ReportEntityAdapter.OnReportEntityInteractionListener() {
            @Override
            public void onReportEntityClicked(ReportEntity reportEntity) {
                SelectedContentFragment.this.onReportEntityClicked(reportEntity);
            }

            @Override
            public void onDeleteReportEntity(ReportEntity reportEntity) {
                logger.d(TAG, "ReportEntity id to be deleted: " + reportEntity.getId());
                selectedContentPresenter.deleteItem(reportEntity, getContentType());
            }
        });
        selectedContentPresenter.configureFilters(getContentId(), getContentType());

    }

    private void onReportEntityClicked(ReportEntity reportEntity) {
        switch (getContentType()) {
            case ContentEntity.TYPE_TRACKED_ENTITY: {
                EnrollmentActivity.navigateToForTrackedEntityInstance(getActivity(), reportEntity.getId());
                break;
            }
            case ContentEntity.TYPE_PROGRAM: {
                FormSectionActivity.navigateToExistingItem(getActivity(), reportEntity.getId(), getContentId(), null, FormSectionContextType.REGISTRATION);
                break;
            }
        }

    }

    private void setupSwipeRefreshLayout(final View view, final Bundle savedInstanceState) {
        swipeRefreshLayout = (SwipeRefreshLayout) view
                .findViewById(R.id.swiperefreshlayout_selected_content);
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

    private void setUpFloatingActionButton(View view) {
        floatingActionButton = (com.github.clans.fab.FloatingActionButton) view.findViewById(R.id.fab_create_item);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedContentPresenter.navigate(getContentId(), getContentTitle(), getContentType());
            }
        });

        selectedContentPresenter.configureFloatingActionMenu(getContentId());
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        toolbar.inflateMenu(R.menu.menu_selected_content);
        toolbar.setOnMenuItemClickListener(this);

        toolbar.setTitle(getContentTitle());

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_selected_content, menu);

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
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        selectedContentPresenter.attachView(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        selectedContentPresenter.detachView();
    }

    private void sync() {
        try {
            selectedContentPresenter.sync();
        } catch (IOException e) {
            selectedContentPresenter.handleError(e);
        }
    }

    @Override
    public void showError(String message) {
        showErrorDialog(getString(R.string.title_error), message);
    }

    @Override
    public void showUnexpectedError(String message) {
        showErrorDialog(getString(R.string.title_error_unexpected), message);
    }

    @Override
    public void showReportEntities(List<ReportEntity> reportEntities) {
        logger.d(TAG, "amount of report entities: " + reportEntities.size());
        reportEntityAdapter.swapData(reportEntities);

    }

    @Override
    public void hideProgressBar() {
        logger.d(TAG, "hideProgressBar()");
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void showProgressBar() {
        logger.d(TAG, "showProgressBar()");

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
    public void notifyFiltersChanged(List<ReportEntityFilter> reportEntityFilters) {
        reportEntityAdapter.notifyFiltersChanged(reportEntityFilters);

        selectedContentPresenter.updateContents(getContentId(), getContentType());
    }

    @Override
    public void setActionsToFab(List<ContentEntity> contentEntities) {
//        for (ContentEntity contentEntity : contentEntities) {
//            FloatingActionButton floatingActionButton = new FloatingActionButton(getContext());
//            floatingActionButton.setLabelText(contentEntity.getTitle());
//
//            floatingActionMenu.addMenuButton(floatingActionButton);
//        }


    }

    @Override
    public void navigateTo(String contentId, String contentTitle) {
        switch (getContentType()) {
            case ContentEntity.TYPE_TRACKED_ENTITY: {
                CreateIdentifiableItemActivity.navigateTo(getActivity(), getContentId(), getContentTitle());
                break;
            }
            case ContentEntity.TYPE_PROGRAM: {
                CreateEventActivity.navigateTo(getActivity(), contentId, null, getContentTitle());
                break;
            }
            default:
                break;
        }

    }

    @Override
    public void navigateToFormSectionActivity(String contentId, String contentTitle, String uid, FormSectionContextType contextType) {
        FormSectionActivity.navigateToNewItem(getActivity(), contentId, contentTitle, uid, contextType);
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

    private String getContentId() {
        if (getArguments() == null || getArguments()
                .getString(ARG_CONTENT_ID, null) == null) {
            throw new IllegalArgumentException("You must pass content id in intent extras");
        }

        return getArguments().getString(ARG_CONTENT_ID, null);
    }

    private String getContentTitle() {
        if (getArguments() == null || getArguments()
                .getString(ARG_CONTENT_TITLE, null) == null) {
            throw new IllegalArgumentException("You must pass content type in intent extras");
        }

        return getArguments().getString(ARG_CONTENT_TITLE, null);
    }

    private String getContentType() {
        if (getArguments() == null || getArguments()
                .getString(ARG_CONTENT_TYPE, null) == null) {
            throw new IllegalArgumentException("You must pass content type in intent extras");
        }

        return getArguments().getString(ARG_CONTENT_TYPE, null);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh: {
                sync();
                return true;
            }
            case R.id.action_search: {
                Toast.makeText(getActivity(), "blabla", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
