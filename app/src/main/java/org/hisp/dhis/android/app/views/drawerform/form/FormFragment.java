package org.hisp.dhis.android.app.views.drawerform.form;


import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.hisp.dhis.android.app.R;
import org.hisp.dhis.android.app.SkeletonApp;
import org.hisp.dhis.android.app.views.drawerform.NavigationLockController;
import org.hisp.dhis.android.app.views.drawerform.RightDrawerController;
import org.hisp.dhis.android.app.views.drawerform.form.dataentry.DataEntryFragment;
import org.hisp.dhis.android.app.views.drawerform.singleevent.SingleEventDashboardComponent;
import org.hisp.dhis.android.app.views.drawerform.trackedentityinstance.TeiDashboardComponent;
import org.hisp.dhis.client.sdk.models.enrollment.EnrollmentStatus;
import org.hisp.dhis.client.sdk.models.event.EventStatus;
import org.hisp.dhis.client.sdk.ui.adapters.OnPickerItemClickListener;
import org.hisp.dhis.client.sdk.ui.fragments.DatePickerDialogFragment;
import org.hisp.dhis.client.sdk.ui.fragments.FilterableDialogFragment;
import org.hisp.dhis.client.sdk.ui.models.Form;
import org.hisp.dhis.client.sdk.ui.models.FormEntity;
import org.hisp.dhis.client.sdk.ui.models.FormSection;
import org.hisp.dhis.client.sdk.ui.models.Picker;
import org.hisp.dhis.client.sdk.utils.LocaleUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

import static android.view.View.GONE;
import static org.hisp.dhis.client.sdk.utils.StringUtils.isEmpty;

/**
 * A simple {@link Fragment} subclass.
 */
public class FormFragment extends Fragment implements FormView, Toolbar.OnMenuItemClickListener {
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String ARG_REGISTRATION_COMPLETE = "arg:registrationComplete";
    private static final String ARG_TWO_PANE_LAYOUT = "arg:twoPaneLayout";
    private static final String ARG_FORM = "arg:form";
    private static final String ARG_ENROLLMENT_UID = "arg:enrollmentUid";
    private static final String ARG_PROGRAM_UID = "arg:programUid";
    private static final String ARG_PROGRAM_STAGE_UID = "arg:programStageUid";
    private static final String ARG_EVENT_UID = "arg:eventUid";
    private static final String ARG_EMPTY_FORM = "arg:emptyForm";

    // Injected dependencies
    @Inject
    FormPresenter formPresenter;
    @Inject
    RightDrawerController rightNavDrawerController;
    @Inject
    NavigationLockController navigationLockController;

    private CoordinatorLayout coordinatorLayout;
    private TextView textViewReportDate;
    private LinearLayout linearLayoutCoordinates;
    private EditText editTextLatitude;
    private EditText editTextLongitude;
    private AppCompatImageView locationIcon;
    private AppCompatImageView locationIconCancel;
    private CircularProgressBar locationProgressBar;
    private FrameLayout locationButtonLayout;
    private AlertDialog alertDialog;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FloatingActionButton fabComplete;
    private String eventUid;
    private FilterableDialogFragment sectionDialogFragment;
    private Toolbar toolbar;
    private Form form;
    private View emptyPlaceholderView;

    public FormFragment() {
        // Required empty public constructor
    }

    public static FormFragment newForm(String eventUid, String programUid, String programStageUid, boolean twoPaneLayout) {
        FormFragment fragment = new FormFragment();
        Bundle arguments = new Bundle();
        arguments.putBoolean(ARG_TWO_PANE_LAYOUT, twoPaneLayout);
        arguments.putString(ARG_EVENT_UID, eventUid);
        arguments.putString(ARG_PROGRAM_UID, programUid);
        arguments.putString(ARG_PROGRAM_STAGE_UID, programStageUid);
        arguments.putBoolean(ARG_REGISTRATION_COMPLETE, true);
        fragment.setArguments(arguments);
        return fragment;
    }

    public static FormFragment newEmptyForm(boolean twoPaneLayout) {
        FormFragment fragment = new FormFragment();
        Bundle arguments = new Bundle();
        arguments.putBoolean(ARG_EMPTY_FORM, true);
        arguments.putBoolean(ARG_TWO_PANE_LAYOUT, twoPaneLayout);
        arguments.putBoolean(ARG_REGISTRATION_COMPLETE, true);
        fragment.setArguments(arguments);
        return fragment;
    }

    public static FormFragment newEnrollmentForm(String enrollmentUid, String programUid, boolean twoPaneLayout) {
        FormFragment fragment = new FormFragment();
        Bundle arguments = new Bundle();
        arguments.putBoolean(ARG_REGISTRATION_COMPLETE, false);
        arguments.putString(ARG_ENROLLMENT_UID, enrollmentUid);
        arguments.putString(ARG_PROGRAM_UID, programUid);
        arguments.putBoolean(ARG_TWO_PANE_LAYOUT, twoPaneLayout);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_form, container, false);

        /*try {
            ((SkeletonApp) getActivity().getApplication()).getTeiDashboardComponent().inject(this);
        } catch (Exception e) {
            Log.e("FormFragment", "Activity or Application is null. Vital resources have been killed.", e);
            ((SkeletonApp) getActivity().getApplication()).getSingleEventDashboardComponent().inject(this);
        }*/


        try {
            TeiDashboardComponent component = ((SkeletonApp) getActivity().getApplication()).getTeiDashboardComponent();
            component.inject(this);
        } catch (Exception e) {
            SingleEventDashboardComponent component = ((SkeletonApp) getActivity().getApplication()).getSingleEventDashboardComponent();
            component.inject(this);
        }

        formPresenter.attachView(this);
        setupCoordinatorLayout(rootView);
        setupToolbar(rootView);
        setupPickers(rootView);
        setupViewPager(rootView);
        setupFloatingActionButton(rootView);
        emptyPlaceholderView = rootView.findViewById(R.id.empty_state_placeholder);

        if (savedInstanceState != null && savedInstanceState.containsKey(ARG_FORM)) {
            // retain form from instance state if we have one
            form = savedInstanceState.getParcelable(ARG_FORM);
        } else if (formIsEmpty()) {
            // no form is selected. Show empty placeholder
            clearForm();
            return rootView;
        } else {
            // build the form from the provided arguments
            buildFormFromArguments();
        }
        formPresenter.buildForm(form);

        return rootView;

    }

    private void buildFormFromArguments() {
        Form.Builder formBuilder = new Form.Builder();

        if (!registrationIsComplete()) {
            // enrollment form
            formBuilder
                    .setDataModelUid(getArguments().getString(ARG_ENROLLMENT_UID))
                    .setProgramUid(getArguments().getString(ARG_PROGRAM_UID));
        } else {
            // event form
            formBuilder
                    .setDataModelUid(getArguments().getString(ARG_EVENT_UID))
                    .setProgramUid(getArguments().getString(ARG_PROGRAM_UID))
                    .setProgramStageUid(getArguments().getString(ARG_PROGRAM_STAGE_UID));

        }
        form = formBuilder.build();
    }

    private boolean formIsEmpty() {
        return getArguments().getBoolean(ARG_EMPTY_FORM);
    }

    private boolean registrationIsComplete() {
        return getArguments().getBoolean(ARG_REGISTRATION_COMPLETE);
    }

    private void setupCoordinatorLayout(View rootView) {
        coordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinatorlayout_form);
    }

    private void setupToolbar(View rootView) {
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        if (!useTwoPaneLayout()) {
            toolbar.inflateMenu(R.menu.show_right_nav_menu);
            toolbar.setOnMenuItemClickListener(this);
        } else if (toolbar.getMenu() != null) {
            toolbar.getMenu().clear();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.action_show_menu) {
            rightNavDrawerController.showMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean useTwoPaneLayout() {
        return getArguments() != null && getArguments().getBoolean(ARG_TWO_PANE_LAYOUT);
    }

    private void setupPickers(View rootView) {
        textViewReportDate = (TextView) rootView.findViewById(R.id.textview_report_date);
        linearLayoutCoordinates = (LinearLayout) rootView.findViewById(R.id.linearlayout_coordinates);
        editTextLatitude = (EditText) rootView.findViewById(R.id.edittext_latitude);
        editTextLongitude = (EditText) rootView.findViewById(R.id.edittext_longitude);
        locationIcon = (AppCompatImageView) rootView.findViewById(R.id.imagevew_location);
        locationIconCancel = (AppCompatImageView) rootView.findViewById(R.id.imagevew_location_cancel);
        locationProgressBar = (CircularProgressBar) rootView.findViewById(R.id.progress_bar_circular_location);
        locationButtonLayout = (FrameLayout) rootView.findViewById(R.id.button_location_layout);

        // set on click listener to text view report date
        textViewReportDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        // since coordinates are optional, initially they should be hidden
        linearLayoutCoordinates.setVisibility(GONE);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(ARG_FORM)) {
            form = savedInstanceState.getParcelable(ARG_FORM);
        }
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (form != null) {
            outState.putParcelable(ARG_FORM, form);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        formPresenter.detachView();
    }

    private void setupLocationCallback() {
        locationButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationManager locationManager = (LocationManager) getActivity().getSystemService(
                        Context.LOCATION_SERVICE);
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                    //we have permission ?
                    if (Build.VERSION.SDK_INT < 23 ||
                            ActivityCompat.checkSelfPermission(v.getContext(),
                                    Manifest.permission.ACCESS_FINE_LOCATION)
                                    == PackageManager.PERMISSION_GRANTED) {
                        //either if init or after cancel click:
                        if (locationIcon.getVisibility() == View.VISIBLE
                                || locationIconCancel.getVisibility() == GONE) {
                            // request location:
                            setLocationButtonState(false);
                            formPresenter.subscribeToLocations();
                        } else {
                            //cancel the location request:
                            setLocationButtonState(true);
                            formPresenter.stopLocationUpdates();
                        }
                    } else {
                        //don't have permissions, set them up !
                        setupLocationPermissions();
                    }
                } else {
                    showGpsDialog();
                }
            }
        });
    }

    private void setupLocationPermissions() {

    }

    public void showGpsDialog() {
        alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_gps_disabled)
                .setMessage(R.string.gps_disabled)
                .setPositiveButton(R.string.settings_option, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.cancel_option, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();

        alertDialog.show();
    }

    private void setupViewPager(View rootView) {
        tabLayout = (TabLayout) rootView.findViewById(R.id.tablayout_data_entry);
        viewPager = (ViewPager) rootView.findViewById(R.id.viewpager_dataentry);

        // hide tab layout initially in order to prevent UI
        // jumps in cases when we don't have sections
        tabLayout.setVisibility(GONE);
    }

    private void setupFloatingActionButton(View rootView) {
        fabComplete = (FloatingActionButton) rootView.findViewById(R.id.fab_complete_event);
        fabComplete.setVisibility(GONE);

        fabComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!registrationIsComplete()) {
                    getArguments().putBoolean(ARG_REGISTRATION_COMPLETE, true);
                    if (navigationLockController != null) {
                        navigationLockController.unlockNavigation();
                    }
                    fabComplete.setVisibility(GONE);
                    clearForm();
                    ((TextView) emptyPlaceholderView.findViewById(R.id.empty_state_placeholder_text)).setText(R.string.enrollment_complete_empty_form);
                    return;
                }

                //TODO: get status from model object and do not store state in fab
                boolean doComplete = !fabComplete.isActivated();

                String snackBarMessage;
                if (doComplete) {
                    snackBarMessage = getString(R.string.complete);
                } else {
                    snackBarMessage = getString(R.string.incomplete);
                }

                setEventCompleteStatus(!fabComplete.isActivated());
                fabComplete.setActivated(!fabComplete.isActivated());

                Snackbar.make(coordinatorLayout, snackBarMessage, Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.undo), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                boolean newStatus = !fabComplete.isActivated();
                                setEventCompleteStatus(newStatus);
                                fabComplete.setActivated(newStatus);
                            }
                        })
                        .show();
            }
        });
    }

    private void clearForm() {
        viewPager.setVisibility(GONE);
        emptyPlaceholderView.setVisibility(View.VISIBLE);
    }

    private void hideEmptyPlaceholder() {
        viewPager.setVisibility(View.VISIBLE);
        emptyPlaceholderView.setVisibility(View.GONE);
    }

    private void setEventCompleteStatus(boolean isComplete) {

        EventStatus eventStatus;

        if (isComplete) {
            eventStatus = EventStatus.COMPLETED;
        } else {
            eventStatus = EventStatus.ACTIVE;
        }

        formPresenter.saveEventStatus(form.getDataModelUid(), eventStatus);
    }

    @Override
    public void showFormDefaultSection(String formSectionId, String programUid, String programStageUid) {
        FormSingleSectionAdapter viewPagerAdapter =
                new FormSingleSectionAdapter(getActivity().getSupportFragmentManager());

        viewPagerAdapter.swapData(getEventUid(), programUid, programStageUid, formSectionId);

        // in order not to loose state of ViewPager, first we
        // have to fill FormSectionsAdapter with data, and only then set it to ViewPager
        viewPager.setAdapter(viewPagerAdapter);

        // hide tab layout
        tabLayout.setVisibility(GONE);
    }

    @Override
    public void showFormSections(List<FormSection> formSections, String programUid, String programStageUid) {
        FormSectionsAdapter viewPagerAdapter =
                new FormSectionsAdapter(getActivity().getSupportFragmentManager());
        //viewPagerAdapter.swapData(getEventUid(), programUid, programStageUid, formSections);

        // in order not to loose state of ViewPager, first we
        // have to fill FormSectionsAdapter with data, and only then set it to ViewPager
        viewPager.setAdapter(viewPagerAdapter);

        // show tab layout
        tabLayout.setVisibility(View.VISIBLE);

        // TabLayout will fail on you, if ViewPager which is going to be
        // attached does not contain ViewPagerAdapter set to it.
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void showForm(Form form) {
        this.form = form;
        hideEmptyPlaceholder();

        FragmentStatePagerAdapter viewPagerAdapter;

        // TODO: Use only one SectionAdapter and let the adapter handle having just one section internally
        if (form.getFormSections().size() == 1) {
            viewPagerAdapter = new FormSingleSectionAdapter(getActivity().getSupportFragmentManager());
            ((FormSingleSectionAdapter) viewPagerAdapter).swapData(form, form.getFormSections().get(0).getId());

        } else {
            viewPagerAdapter =
                    new FormSectionsAdapter(getActivity().getSupportFragmentManager());
            ((FormSectionsAdapter) viewPagerAdapter).swapData(form);
        }

        // in order not to loose state of ViewPager, first we
        // have to fill FormSectionsAdapter with data, and only then set it to ViewPager
        viewPager.setAdapter(viewPagerAdapter);

        refreshTabLayout(form);

    }

    @Override
    public void setMenuButtonVisibility(boolean showMenuButton) {

        getArguments().putBoolean(ARG_TWO_PANE_LAYOUT, !showMenuButton);

        if (toolbar != null) {
            if (showMenuButton) {
                if (toolbar.getMenu() == null || toolbar.getMenu().size() == 0) {
                    toolbar.inflateMenu(R.menu.show_right_nav_menu);
                    toolbar.setOnMenuItemClickListener(this);
                }
            } else if (toolbar.getMenu() != null) {
                toolbar.getMenu().clear();
            }
        }

    }

    private void refreshTabLayout(Form form) {

        // TODO: wrap TabLayout and Adapter into a separate entity and let it handle its own state?
        int visibility = View.VISIBLE;
        if (form.getFormSections().size() == 1) {
            visibility = GONE;
        }
        tabLayout.setVisibility(visibility);

        // TabLayout will fail on you, if ViewPager which is going to be
        // attached does not contain ViewPagerAdapter set to it.
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void setFormSectionsPicker(Picker picker) {
        sectionDialogFragment = FilterableDialogFragment.newInstance(picker);
        sectionDialogFragment.setOnPickerItemClickListener(new OnSearchSectionsClickListener());
    }

    @Override
    public void showReportDatePicker(String hint, String value) {
        String dateLabel = isEmpty(hint) ? getString(R.string.report_date) : hint;
        textViewReportDate.setHint(dateLabel);

        if (!isEmpty(value)) {
            textViewReportDate.setText(String.format(LocaleUtils.getLocale(),
                    "%s: %s", dateLabel, value));
        }
    }

    @Override
    public void showCoordinatesPicker(String latitude, String longitude) {
        if (linearLayoutCoordinates.getVisibility() == View.INVISIBLE ||
                linearLayoutCoordinates.getVisibility() == GONE) {
            linearLayoutCoordinates.setVisibility(View.VISIBLE);
            setupLocationCallback();
        }
        if (!isEmpty(latitude)) {
            editTextLatitude.setText(latitude);
        }
        if (!isEmpty(longitude)) {
            editTextLongitude.setText(longitude);
        }
    }

    @Override
    public void showFormTitle(String formTitle) {
        toolbar.setTitle(formTitle);
    }

    @Override
    public void showEventStatus(EventStatus eventStatus) {
        if (fabComplete != null && eventStatus != null) {
            fabComplete.setVisibility(View.VISIBLE);
            fabComplete.setActivated(EventStatus.COMPLETED.equals(eventStatus));
        }
    }

    @Override
    public void showEnrollmentStatus(EnrollmentStatus enrollmentStatus) {
        if (fabComplete != null && enrollmentStatus != null) {
            fabComplete.setVisibility(View.VISIBLE);
            fabComplete.setImageResource(R.drawable.ic_tick);
            fabComplete.setActivated(EnrollmentStatus.COMPLETED.equals(enrollmentStatus));
        }
    }

    @Override
    public String getFormSectionLabel(@FormSectionLabelId String formSectionLabelId) {
        switch (formSectionLabelId) {
            case FormView.ID_CHOOSE_SECTION: {
                return getString(R.string.choose_section);
            }
        }

        return null;
    }

    @Override
    public List<FormEntity> getInvalidFormEntities() {
        return ((DataEntryFragment) ((FormSingleSectionAdapter) viewPager.getAdapter()).getItem(0)).getInvalidFormEntities();
    }

    @Override
    public void setEventUid(String eventUid) {
        this.eventUid = eventUid;
    }

    @Override
    public void setForm(Form form) {
        this.form = form;
    }

    @Override
    public void setLocation(Location location) {

    }

    @Override
    public void setLocationButtonState(boolean enabled) {

    }

    public String getEventUid() {
        return eventUid;
    }

    private class OnSearchSectionsClickListener implements OnPickerItemClickListener {
        @Override
        public void onPickerItemClickListener(Picker selectedPicker) {
            PagerAdapter pagerAdapter = viewPager.getAdapter();

            if (pagerAdapter != null && (pagerAdapter instanceof FormSectionsAdapter)) {
                FormSectionsAdapter sectionsAdapter = (FormSectionsAdapter) pagerAdapter;
                List<FormSection> formSections = sectionsAdapter.getData();

                for (int position = 0; position < formSections.size(); position++) {
                    FormSection formSection = formSections.get(position);

                    if (formSection.getId().equals(selectedPicker.getId())) {
                        viewPager.setCurrentItem(position);
                        break;
                    }
                }
            }
        }
    }

    private void showDatePickerDialog() {
        final DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT, LocaleUtils.getLocale());
                String stringDate = simpleDateFormat
                        .format(calendar.getTime());
                String newValue = String.format(LocaleUtils.getLocale(), "%s: %s",
                        textViewReportDate.getHint(), stringDate);
                textViewReportDate.setText(newValue);

                Date currentDate = Calendar.getInstance().getTime();
                Date selectedDate = calendar.getTime();


                /*
                * in case when user selected today's date, we need to know about time as well.
                * selectedDateTime does not contain time information (only date), that's why we
                * need to create a new DateTime object by calling DateTime.now()
                */
                Date date = null;
                if (currentDate.equals(selectedDate)) {
                    date = currentDate;
                } else {
                    date = selectedDate;
                }

                if (getEventUid() != null) {
                    formPresenter.saveEventDate(getEventUid(), date);
                }

            }
        };

        DatePickerDialogFragment datePickerDialogFragment =
                DatePickerDialogFragment.newInstance(false);
        datePickerDialogFragment.setOnDateSetListener(onDateSetListener);
        datePickerDialogFragment.show(getActivity().getSupportFragmentManager());
    }

    /*
    *
    * This adapter exists only in order to satisfy cases when there is no
    * sections assigned to program stage. As the result, we have to
    * use program stage itself as section
    *
    */
    private static class FormSingleSectionAdapter extends FragmentStatePagerAdapter {
        private static final int DEFAULT_STAGE_COUNT = 1;
        private static final int DEFAULT_STAGE_POSITION = 0;
        private String itemId;
        private String programId;
        private String programStageId;
        private String formSectionId;

        public FormSingleSectionAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            if (DEFAULT_STAGE_POSITION == position && !isEmpty(formSectionId)) {
                return DataEntryFragment.newInstanceForItem(itemId, programId, programStageId);
//                return DataEntryFragment.newInstanceForStage(itemId, programId, programStageId);
            }

            return null;
        }

        @Override
        public int getCount() {
            return isEmpty(formSectionId) ? 0 : DEFAULT_STAGE_COUNT;
        }

        public void swapData(String itemId, String programId, String programStageId, String formSectionId) {
            this.itemId = itemId;
            this.programId = programId;
            this.programStageId = programStageId;
            this.formSectionId = formSectionId;
            this.notifyDataSetChanged();
        }

        public void swapData(Form form, String formSectionId) {
            itemId = form.getDataModelUid();
            programId = form.getProgramUid();
            programStageId = form.getProgramStageUid();
            this.formSectionId = formSectionId;
            notifyDataSetChanged();
        }
    }

    private static class FormSectionsAdapter extends FragmentStatePagerAdapter {
        private final List<FormSection> formSections;
        private String eventId;
        private String programId;
        private String programStageId;

        public FormSectionsAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            this.formSections = new ArrayList<>();
        }

        @Override
        public Fragment getItem(int position) {
            FormSection formSection = formSections.get(position);
            return DataEntryFragment.newInstanceForSection(eventId, programId, programStageId, formSection.getId());
        }

        @Override
        public int getCount() {
            return formSections.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            FormSection formSection = formSections.get(position);
            return formSection.getLabel();
        }

        @NonNull
        public List<FormSection> getData() {
            return formSections;
        }

        public void swapData(Form form) {
            eventId = form.getDataModelUid();
            programId = form.getProgramUid();
            programStageId = form.getProgramStageUid();
            formSections.clear();

            if (form.getFormSections() != null) {
                formSections.addAll(form.getFormSections());
            }
            notifyDataSetChanged();
        }
    }
}