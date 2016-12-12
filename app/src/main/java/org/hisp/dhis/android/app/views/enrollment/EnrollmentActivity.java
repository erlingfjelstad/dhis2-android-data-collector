package org.hisp.dhis.android.app.views.enrollment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import org.hisp.dhis.android.app.R;
import org.hisp.dhis.android.app.SkeletonApp;
import org.hisp.dhis.android.app.views.dashboard.trackedentityinstance.TeiDashboardActivity;
import org.hisp.dhis.client.sdk.ui.adapters.ReportEntityAdapter;
import org.hisp.dhis.client.sdk.ui.models.ReportEntity;
import org.hisp.dhis.client.sdk.ui.models.ReportEntityFilter;
import org.hisp.dhis.client.sdk.ui.views.DividerDecoration;

import java.util.List;

import javax.inject.Inject;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

public class EnrollmentActivity extends AppCompatActivity implements EnrollmentView {

    private static final String ARG_TRACKED_ENTITY_INSTANCE_UID = "arg:trackedEntityInstanceUid";

    @Inject
    EnrollmentPresenter enrollmentPresenter;

    private RecyclerView reportEntityRecyclerView;
    private ReportEntityAdapter reportEntityAdapter;
    private FloatingActionButton floatingActionButton;
    private Toolbar toolbar;

    public static void navigateToForTrackedEntityInstance(Activity activity, String trackedEntityInstanceUid) {
        isNull(activity, "activity must not be null");

        Intent intent = new Intent(activity, EnrollmentActivity.class);
        intent.putExtra(ARG_TRACKED_ENTITY_INSTANCE_UID, trackedEntityInstanceUid);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment);

        setupRecyclerView();
        setupFloatingActionButton();
        setupToolbar();

        // if using two-pane layout (tablets in landscape mode), drawerLayout will be null
//        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        EnrollmentComponent enrollmentComponent = ((SkeletonApp) getApplication()).getEnrollmentComponent();

        // first time activity is created
        if (savedInstanceState == null) {
            // it means we found old component and we have to release it
            if (enrollmentComponent != null) {
                // create new instance of component
                ((SkeletonApp) getApplication()).releaseEnrollmentComponent();
            }

            enrollmentComponent = ((SkeletonApp) getApplication()).createEnrollmentComponent();
        } else {
            enrollmentComponent = ((SkeletonApp) getApplication()).getEnrollmentComponent();
        }

        // inject dependencies
        enrollmentComponent.inject(this);
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(org.hisp.dhis.client.sdk.ui.R.id.toolbar);
        Drawable buttonDrawable = DrawableCompat.wrap(ContextCompat
                .getDrawable(this, org.hisp.dhis.client.sdk.ui.R.drawable.ic_arrow_back));
        DrawableCompat.setTint(buttonDrawable, ContextCompat
                .getColor(this, android.R.color.white));

        toolbar.setNavigationIcon(buttonDrawable);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setTitle(getString(R.string.enrollments));
    }

    private void setupRecyclerView() {
        reportEntityRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_enrollment);
        reportEntityAdapter = new ReportEntityAdapter(this);
        reportEntityAdapter.setOnReportEntityInteractionListener(new ReportEntityAdapter.OnReportEntityInteractionListener() {
            @Override
            public void onReportEntityClicked(ReportEntity reportEntity) {
                EnrollmentActivity.this.onReportEntityClicked(reportEntity);
            }

            @Override
            public void onDeleteReportEntity(ReportEntity reportEntity) {

            }
        });
        reportEntityRecyclerView.setAdapter(reportEntityAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        reportEntityRecyclerView.setLayoutManager(layoutManager);

        reportEntityRecyclerView.setItemAnimator(new DefaultItemAnimator());
        reportEntityRecyclerView.addItemDecoration(new DividerDecoration(
                ContextCompat.getDrawable(this, R.drawable.divider)));
    }

    private void setupFloatingActionButton() {
        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingactionbutton_enrollment);
        floatingActionButton.setOnClickListener(new OnCreateNewEnrollmentButtonClickListener());
    }

    private void onReportEntityClicked(ReportEntity reportEntity) {
        enrollmentPresenter.navigateToEnrollment(reportEntity, getTrackedEntityInstanceUid());
    }

    @Override
    public String getString(@StringId String stringId) {
        isNull(stringId, "stringId must not be null");

        switch (stringId) {
            case ID_PROGRAM:
                return getString(R.string.program);

            default:
                throw new IllegalArgumentException("Unsupported StringId");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        enrollmentPresenter.attachView(this);
        enrollmentPresenter.drawEnrollments(getTrackedEntityInstanceUid());
    }

    @Override
    protected void onPause() {
        super.onPause();
        enrollmentPresenter.detachView();
    }

    @Override
    public void updateReportEntityFilters(List<ReportEntityFilter> reportEntityFilters) {
        reportEntityAdapter.notifyFiltersChanged(reportEntityFilters);
    }

    @Override
    public void drawEnrollments(List<ReportEntity> enrollmentReportEntities) {
        reportEntityAdapter.swapData(enrollmentReportEntities);
    }

    @Override
    public void navigateToTeiDashboardWithEnrollment(String programUid, String enrollmentUid) {
        TeiDashboardActivity.navigateTo(this, enrollmentUid, programUid);
        finish();
    }

    @Override
    public void navigateToCreateNewEnrollment(String trackedEntityInstanceUid) {
        Toast.makeText(this, "very enrollment", Toast.LENGTH_SHORT).show();
    }

    private String getTrackedEntityInstanceUid() {
        if (getIntent().getExtras() == null || getIntent().getExtras()
                .getString(ARG_TRACKED_ENTITY_INSTANCE_UID, null) == null) {
            throw new IllegalArgumentException("You must pass tei uid in intent extras");
        }

        return getIntent().getExtras().getString(ARG_TRACKED_ENTITY_INSTANCE_UID, null);
    }

    private class OnCreateNewEnrollmentButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            enrollmentPresenter.createNewEnrollment(getTrackedEntityInstanceUid());
        }
    }
}
