package org.hisp.dhis.android.app.views.dashboard.dataentry;


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
import android.util.Log;
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
import org.hisp.dhis.android.app.presenters.FormSectionPresenter;
import org.hisp.dhis.android.app.views.DataEntryFragment;
import org.hisp.dhis.android.app.views.FormSectionView;
import org.hisp.dhis.android.app.views.dashboard.RightNavDrawerController;
import org.hisp.dhis.client.sdk.models.enrollment.EnrollmentStatus;
import org.hisp.dhis.client.sdk.models.event.EventStatus;
import org.hisp.dhis.client.sdk.ui.adapters.OnPickerItemClickListener;
import org.hisp.dhis.client.sdk.ui.fragments.DatePickerDialogFragment;
import org.hisp.dhis.client.sdk.ui.fragments.FilterableDialogFragment;
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

import static org.hisp.dhis.client.sdk.utils.StringUtils.isEmpty;

/**
 * A simple {@link Fragment} subclass.
 */
public class FormSectionFragment extends Fragment implements FormSectionView, Toolbar.OnMenuItemClickListener {
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String ARG_TWO_PANE_LAYOUT = "arg:twoPaneLayout";

    // Injected dependencies
    @Inject
    FormSectionPresenter formSectionPresenter;
    @Inject
    RightNavDrawerController rightNavDrawerController;

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

    public FormSectionFragment() {
        // Required empty public constructor
    }

    public static FormSectionFragment newInstance(boolean twoPaneLayout) {
        FormSectionFragment fragment = new FormSectionFragment();
        Bundle arguments = new Bundle();
        arguments.putBoolean(ARG_TWO_PANE_LAYOUT, twoPaneLayout);
        fragment.setArguments(arguments);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_form_section, container, false);

        try {
            ((SkeletonApp) getActivity().getApplication()).getFormComponent().inject(this);
        } catch (Exception e) {
            Log.e("DataEntryFragment", "Activity or Application is null. Vital resources have been killed.", e);
        }

        formSectionPresenter.attachView(this);

        setupCoordinatorLayout(rootView);
        setupToolbar(rootView);
        setupPickers(rootView);
        setupViewPager(rootView);
        setupFloatingActionButton(rootView);
        return rootView;

    }

    private void setupCoordinatorLayout(View rootView) {
        coordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinatorlayout_form);
    }

    private void setupToolbar(View rootView) {
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        if (!useTwoPaneLayout()) {
            toolbar.inflateMenu(R.menu.show_right_nav_menu);
            toolbar.setOnMenuItemClickListener(this);
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
        linearLayoutCoordinates.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        formSectionPresenter.detachView();
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
                                || locationIconCancel.getVisibility() == View.GONE) {
                            // request location:
                            setLocationButtonState(false);
                            formSectionPresenter.subscribeToLocations();
                        } else {
                            //cancel the location request:
                            setLocationButtonState(true);
                            formSectionPresenter.stopLocationUpdates();
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
        tabLayout.setVisibility(View.GONE);
    }

    private void setupFloatingActionButton(View rootView) {
        fabComplete = (FloatingActionButton) rootView.findViewById(R.id.fab_complete_event);
        fabComplete.setVisibility(View.GONE);

        fabComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //TODO: get complete status from model object and do not store state in fab
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

    private void setEventCompleteStatus(boolean isComplete) {
        if (isComplete) {
            formSectionPresenter.saveEventStatus(getEventUid(), EventStatus.COMPLETED);
        } else {
            formSectionPresenter.saveEventStatus(getEventUid(), EventStatus.ACTIVE);
        }
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
        tabLayout.setVisibility(View.GONE);
    }

    @Override
    public void showFormSections(List<FormSection> formSections, String programUid, String programStageUid) {
        FormSectionsAdapter viewPagerAdapter =
                new FormSectionsAdapter(getActivity().getSupportFragmentManager());
        viewPagerAdapter.swapData(getEventUid(), programUid, programStageUid, formSections);

        // in order not to loose state of ViewPager, first we
        // have to fill FormSectionsAdapter with data, and only then set it to ViewPager
        viewPager.setAdapter(viewPagerAdapter);

        // hide tab layout
        tabLayout.setVisibility(View.VISIBLE);

        // TabLayout will fail on you, if ViewPager which is going to be
        // attached does not contain ViewPagerAdapter set to it.
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void setFormSectionsPicker(Picker picker) {
        sectionDialogFragment = FilterableDialogFragment.newInstance(picker);
        sectionDialogFragment.setOnPickerItemClickListener(new OnSearchSectionsClickListener());

        //supportInvalidateOptionsMenu();
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
                linearLayoutCoordinates.getVisibility() == View.GONE) {
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
            fabComplete.setImageResource(R.drawable.ic_add);
            fabComplete.setActivated(EnrollmentStatus.COMPLETED.equals(enrollmentStatus));
        }
    }

    @Override
    public String getFormSectionLabel(@FormSectionLabelId String formSectionLabelId) {
        switch (formSectionLabelId) {
            case FormSectionView.ID_CHOOSE_SECTION: {
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
                    formSectionPresenter.saveEventDate(getEventUid(), date);
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

        public void swapData(String eventId, String programId, String programStageId, List<FormSection> formSections) {
            this.eventId = eventId;
            this.programId = programId;
            this.programStageId = programStageId;
            this.formSections.clear();

            if (formSections != null) {
                this.formSections.addAll(formSections);
            }
            notifyDataSetChanged();
        }
    }
}