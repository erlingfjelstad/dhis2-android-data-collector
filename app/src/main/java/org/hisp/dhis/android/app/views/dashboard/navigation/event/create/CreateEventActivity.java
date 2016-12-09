package org.hisp.dhis.android.app.views.dashboard.navigation.event.create;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import org.hisp.dhis.client.sdk.ui.models.FormEntityText;
import org.hisp.dhis.client.sdk.ui.rows.filterablerowview.FilterableRowView;
import org.hisp.dhis.client.sdk.ui.rows.filterablerowview.FilterableRowViewHolder;
import org.hisp.dhis.client.sdk.ui.views.QuickSelectionContainer;

import java.util.ArrayList;
import java.util.Date;

import javax.inject.Inject;

public class CreateEventActivity extends AppCompatActivity implements CreateEventView, View.OnClickListener {

    public static final String ARG_IDENTIFIABLE_FORM_ENTITIES = "arg:identifiableFormEntities";
    public static final String ARG_PROGRAM_UID = "arg:programUid";
    public static final String ARG_ENROLLMENT_UID = "arg:enrollmentUid";
    public static final String ARG_PROGRAM_STAGE_UID = "arg:programStageUid";
    public static final String ARG_ORG_UNIT_UID = "arg:orgUnitUid";
    public static final int CREATE_EVENT_REQUEST_CODE = 1;
    public static final String ARG_EVENT_TYPE = "arg:eventType";
    public static final int EVENT_TYPE_ACTIVE = 1;
    public static final int EVENT_TYPE_SCHEDULED = 2;

    @Inject
    CreateEventPresenter createEventPresenter;

    private FilterableRowViewHolder orgUnitViewHolder;
    private QuickSelectionContainer programStageView;
    private FormEntityFilter organisationUnits;

    public static void navigateTo(Fragment fragment,
                                  String programUid,
                                  String enrollmentUid,
                                  ArrayList<FormEntityText> identifiableFormEntities) {
        Intent intent = new Intent(fragment.getContext(), CreateEventActivity.class);
        intent.putExtra(ARG_PROGRAM_UID, programUid);
        intent.putExtra(ARG_ENROLLMENT_UID, enrollmentUid);
        intent.putParcelableArrayListExtra(ARG_IDENTIFIABLE_FORM_ENTITIES, identifiableFormEntities);
        fragment.startActivityForResult(intent, CREATE_EVENT_REQUEST_CODE);
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
        createEventPresenter.drawViews(getProgramUid());
    }

    private String getEnrollmentUid() {
        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(ARG_ENROLLMENT_UID)) {
            return getIntent().getExtras().getString(ARG_ENROLLMENT_UID);
        }
        return "";
    }

    private String getProgramUid() {
        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(ARG_PROGRAM_UID)) {
            return getIntent().getExtras().getString(ARG_PROGRAM_UID);
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
            data.putString(ARG_PROGRAM_UID, getProgramUid());
            data.putString(ARG_ORG_UNIT_UID, orgUnitUid);
            data.putString(ARG_PROGRAM_STAGE_UID, programStageUid);
            data.putString(ARG_ENROLLMENT_UID, getEnrollmentUid());
            resultOkIntent.putExtras(data);
            setResult(RESULT_OK, resultOkIntent);
            finish();
        } else if (v.getId() == R.id.schedule_button) {
            DatePickerDialogFragment datePicker = DatePickerDialogFragment.newInstance(true);
            datePicker.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    Date scheduledDate = new Date(year, month, dayOfMonth);
                    createEventPresenter.storeScheduledEvent(scheduledDate, orgUnitUid, programStageUid, getProgramUid(), getEnrollmentUid());

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
