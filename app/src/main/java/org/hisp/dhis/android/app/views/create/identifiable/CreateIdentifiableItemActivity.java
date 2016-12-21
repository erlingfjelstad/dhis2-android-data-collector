package org.hisp.dhis.android.app.views.create.identifiable;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.hisp.dhis.android.app.R;
import org.hisp.dhis.android.app.SkeletonApp;
import org.hisp.dhis.android.app.views.dashboard.trackedentityinstance.TeiDashboardActivity;
import org.hisp.dhis.client.sdk.ui.models.FormEntityFilter;
import org.hisp.dhis.client.sdk.ui.rows.filterablerowview.FilterableRowView;
import org.hisp.dhis.client.sdk.ui.rows.filterablerowview.FilterableRowViewHolder;
import org.hisp.dhis.client.sdk.ui.views.QuickSelectionContainer;

import javax.inject.Inject;

public class CreateIdentifiableItemActivity extends AppCompatActivity implements CreateIdentifiableItemView, View.OnClickListener {

    public static final String ARG_TITLE = "arg:title";
    public static final String ARG_CONTENT_ID = "arg:programUid";
    public static final String ARG_IDENTIFIABLE_ID = "arg:enrollmentUid";
    public static final String ARG_PROGRAM_ID = "arg:programStageUid";
    public static final String ARG_ORG_UNIT_ID = "arg:orgUnitUid";
    public static final String ARG_EVENT_TYPE = "arg:eventType";
    public static final int EVENT_TYPE_ACTIVE = 1;
    public static final int EVENT_TYPE_SCHEDULED = 2;

    @Inject
    CreateIdentifiableItemPresenter createIdentifiableItemPresenter;

    private FilterableRowViewHolder orgUnitViewHolder;
    private QuickSelectionContainer programView;
    private FormEntityFilter organisationUnits;

    public static void navigateTo(Activity activity,
                                  String contentId,
                                  String title) {
        Intent intent = new Intent(activity, CreateIdentifiableItemActivity.class);
        intent.putExtra(ARG_CONTENT_ID, contentId);
        intent.putExtra(ARG_TITLE, title);
        activity.startActivity(intent);
    }

    public static void navigateToNewItemForInstance(Activity activity,
                                                    String contentId,
                                                    String identifiableId,
                                                    String title) {
        Intent intent = new Intent(activity, CreateIdentifiableItemActivity.class);
        intent.putExtra(ARG_CONTENT_ID, contentId);
        intent.putExtra(ARG_IDENTIFIABLE_ID, identifiableId);
        intent.putExtra(ARG_TITLE, title);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_item);
        setupToolbar();
        setUpButtons();
        initViews();
        ((SkeletonApp) getApplication())
                .getUserComponent().inject(this);
        createIdentifiableItemPresenter.attachView(this);
        createIdentifiableItemPresenter.drawViews(getContentId());
    }

    private String getIdentifiableId() {
        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(ARG_IDENTIFIABLE_ID)) {
            return getIntent().getExtras().getString(ARG_IDENTIFIABLE_ID);
        }
        return null;
    }

    private String getContentId() {
        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(ARG_CONTENT_ID)) {
            return getIntent().getExtras().getString(ARG_CONTENT_ID);
        }
        return "";
    }

    private String getTitleExtras() {
        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(ARG_TITLE)) {
            return getIntent().getExtras().getString(ARG_TITLE);
        }
        return "";
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_cancel_white);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setTitle(getTitleExtras());
            toolbar.setTitle("New " + getTitleExtras());
        }
    }

    private void initViews() {
        programView = (QuickSelectionContainer) findViewById(R.id.item_selection_container);
        View orgUnitView = findViewById(R.id.org_unit);
        FilterableRowView orgUnitRowView = new FilterableRowView(getSupportFragmentManager());
        orgUnitViewHolder = (FilterableRowViewHolder) orgUnitRowView.onCreateViewHolder(orgUnitView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpButtons() {
        findViewById(R.id.fab_create_item).setOnClickListener(this);
    }

    private boolean noOrgUnitIsSelected() {
        return organisationUnits.getPicker().getSelectedChild() == null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        createIdentifiableItemPresenter.attachView(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        createIdentifiableItemPresenter.detachView();
    }

    @Override
    public void drawViews(FormEntityFilter programStages, FormEntityFilter organisationUnits) {
        programView.setFormEntityFilter(programStages);
        this.organisationUnits = organisationUnits;
        orgUnitViewHolder.update(organisationUnits, getSupportFragmentManager());
    }

    @Override
    public void itemCreated(String itemUid, String programUid) {
        TeiDashboardActivity.navigateToNewItem(this, itemUid, programUid);
        finish();
    }

    @Override
    public void onClick(View v) {

        if (!formIsValid()) {
            return;
        }

        final Intent resultOkIntent = new Intent();
        final Bundle data = new Bundle();

        final String orgUnitUid = organisationUnits.getPicker().getSelectedChild().getId();
        final String programUid = programView.getSelection().getId();

        if (v.getId() == R.id.fab_create_item) {
            data.putInt(ARG_EVENT_TYPE, EVENT_TYPE_ACTIVE);
            data.putString(ARG_CONTENT_ID, getContentId());
            data.putString(ARG_ORG_UNIT_ID, orgUnitUid);
            data.putString(ARG_PROGRAM_ID, programUid);
            data.putString(ARG_IDENTIFIABLE_ID, getIdentifiableId());
            resultOkIntent.putExtras(data);

            if (getIdentifiableId() != null) {
                createIdentifiableItemPresenter.createNewItemForInstance(orgUnitUid, programUid, getIdentifiableId());
            } else {
                createIdentifiableItemPresenter.createItem(orgUnitUid, programUid, getContentId());
            }
        }
    }

    private boolean formIsValid() {
        if (noProgramIsSelected()) {
            Toast.makeText(this, "Please select Program Stage", Toast.LENGTH_SHORT).show();
            return false;
        } else if (noOrgUnitIsSelected()) {
            Toast.makeText(this, "Please select Organisation Unit", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean noProgramIsSelected() {
        return programView.getSelection() == null;
    }


}
