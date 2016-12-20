package org.hisp.dhis.android.app.views.create.event;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import org.hisp.dhis.android.app.R;
import org.hisp.dhis.android.app.SkeletonApp;
import org.hisp.dhis.client.sdk.ui.fragments.DatePickerDialogFragment;
import org.hisp.dhis.client.sdk.ui.models.FormEntityFilter;
import org.hisp.dhis.client.sdk.ui.rows.filterablerowview.FilterableRowView;
import org.hisp.dhis.client.sdk.ui.rows.filterablerowview.FilterableRowViewHolder;
import org.hisp.dhis.client.sdk.ui.views.QuickSelectionContainer;

import java.util.Date;

import javax.inject.Inject;

public class CreateEventActivity extends AppCompatActivity implements CreateEventView, View.OnClickListener {

    public static final String ARG_CONTENT_TITLE = "arg:identifiableFormEntities";
    public static final String ARG_CONTENT_ID = "arg:programUid";
    public static final String ARG_IDENTIFIABLE_ID = "arg:enrollmentUid";
    public static final String ARG_PROGRAM_STAGE_UID = "arg:programStageUid";
    public static final String ARG_ORG_UNIT_UID = "arg:orgUnitUid";
    public static final String ARG_EVENT_TYPE = "arg:eventType";
    public static final int EVENT_TYPE_ACTIVE = 1;
    public static final int EVENT_TYPE_SCHEDULED = 2;
    public static final int CREATE_EVENT_REQUEST_CODE = 1;

    @Inject
    CreateEventPresenter createEventPresenter;

    private FilterableRowViewHolder orgUnitViewHolder;
    private QuickSelectionContainer programStageView;
    private FormEntityFilter organisationUnits;

    public static void navigateTo(Activity activity,
                                  String programUid,
                                  String enrollmentUid,
                                  String contentTitle) {
        Intent intent = new Intent(activity, CreateEventActivity.class);
        intent.putExtra(ARG_CONTENT_ID, programUid);
        intent.putExtra(ARG_IDENTIFIABLE_ID, enrollmentUid);
        intent.putExtra(ARG_CONTENT_TITLE, contentTitle);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        setupToolbar();
        setUpButtons();
        initViews();
        ((SkeletonApp) getApplication())
                .getUserComponent().inject(this);
        createEventPresenter.attachView(this);
        createEventPresenter.drawViews(contentId());
    }

    private String getIdentifiableId() {
        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(ARG_IDENTIFIABLE_ID)) {
            return getIntent().getExtras().getString(ARG_IDENTIFIABLE_ID);
        }
        return "";
    }

    private String contentId() {
        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(ARG_CONTENT_ID)) {
            return getIntent().getExtras().getString(ARG_CONTENT_ID);
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
        }
    }

    private void initViews() {
        programStageView = (QuickSelectionContainer) findViewById(R.id.program_stage_selection_container);
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
        findViewById(R.id.schedule_button).setOnClickListener(this);
        findViewById(R.id.next_button).setOnClickListener(this);
    }

    private boolean noOrgUnitIsSelected() {
        return organisationUnits.getPicker().getSelectedChild() == null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        createEventPresenter.attachView(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        createEventPresenter.detachView();
    }

    @Override
    public void drawViews(FormEntityFilter programStages, FormEntityFilter organisationUnits) {
        programStageView.setFormEntityFilter(programStages);
        this.organisationUnits = organisationUnits;
        orgUnitViewHolder.update(organisationUnits, getSupportFragmentManager());
    }

    @Override
    public void onClick(View v) {

        if (!formIsValid()) {
            return;
        }

        final Intent resultOkIntent = new Intent();
        final Bundle data = new Bundle();

        final String orgUnitUid = organisationUnits.getPicker().getSelectedChild().getId();
        final String programStageUid = programStageView.getSelection().getId();

        if (v.getId() == R.id.next_button) {
            data.putInt(ARG_EVENT_TYPE, EVENT_TYPE_ACTIVE);
            data.putString(ARG_CONTENT_ID, contentId());
            data.putString(ARG_ORG_UNIT_UID, orgUnitUid);
            data.putString(ARG_PROGRAM_STAGE_UID, programStageUid);
            data.putString(ARG_IDENTIFIABLE_ID, getIdentifiableId());
            resultOkIntent.putExtras(data);
            setResult(RESULT_OK, resultOkIntent);
            finish();
        } else if (v.getId() == R.id.schedule_button) {
            DatePickerDialogFragment datePicker = DatePickerDialogFragment.newInstance(true);
            datePicker.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    Date scheduledDate = new Date(year, month, dayOfMonth);
                    createEventPresenter.storeScheduledEvent(scheduledDate, orgUnitUid, programStageUid, contentId(), getIdentifiableId());

                    data.putInt(ARG_EVENT_TYPE, EVENT_TYPE_SCHEDULED);
                    resultOkIntent.putExtras(data);

                    setResult(RESULT_OK, resultOkIntent);
                    finish();
                }
            });
            datePicker.show(getSupportFragmentManager());
        }
    }

    private boolean formIsValid() {
        if (noProgramIsSelected()) {
            Toast.makeText(CreateEventActivity.this, "Please select Program Stage", Toast.LENGTH_SHORT).show();
            return false;
        } else if (noOrgUnitIsSelected()) {
            Toast.makeText(CreateEventActivity.this, "Please select Organisation Unit", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean noProgramIsSelected() {
        return programStageView.getSelection() == null;
    }

}
