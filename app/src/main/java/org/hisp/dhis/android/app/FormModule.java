package org.hisp.dhis.android.app;

import android.support.annotation.Nullable;

import org.hisp.dhis.android.app.model.RxRulesEngine;
import org.hisp.dhis.android.app.presenters.DataEntryPresenter;
import org.hisp.dhis.android.app.presenters.DataEntryPresenterImpl;
import org.hisp.dhis.android.app.presenters.FormSectionPresenter;
import org.hisp.dhis.android.app.presenters.FormSectionPresenterImpl;
import org.hisp.dhis.android.app.views.dashboard.TeiDashboardPresenter;
import org.hisp.dhis.android.app.views.dashboard.TeiDashboardPresenterImpl;
import org.hisp.dhis.android.app.views.dashboard.navigation.TeiNavigationPresenter;
import org.hisp.dhis.android.app.views.dashboard.navigation.TeiNavigationPresenterImpl;
import org.hisp.dhis.android.app.views.dashboard.navigation.event.TeiProgramStagePresenter;
import org.hisp.dhis.android.app.views.dashboard.navigation.event.TeiProgramStagePresenterImpl;
import org.hisp.dhis.android.app.views.dashboard.navigation.profile.TeiProfilePresenter;
import org.hisp.dhis.android.app.views.dashboard.navigation.profile.TeiProfilePresenterImpl;
import org.hisp.dhis.android.app.views.dashboard.navigation.widget.TeiWidgetPresenter;
import org.hisp.dhis.android.app.views.dashboard.navigation.widget.TeiWidgetPresenterImpl;
import org.hisp.dhis.client.sdk.core.enrollment.EnrollmentInteractor;
import org.hisp.dhis.client.sdk.core.event.EventInteractor;
import org.hisp.dhis.client.sdk.core.option.OptionSetInteractor;
import org.hisp.dhis.client.sdk.core.program.ProgramInteractor;
import org.hisp.dhis.client.sdk.core.trackedentity.TrackedEntityAttributeValueInteractor;
import org.hisp.dhis.client.sdk.core.trackedentity.TrackedEntityDataValueInteractor;
import org.hisp.dhis.client.sdk.core.trackedentity.TrackedEntityInstanceInteractor;
import org.hisp.dhis.client.sdk.core.user.UserInteractor;
import org.hisp.dhis.client.sdk.utils.Logger;

import dagger.Module;
import dagger.Provides;

@Module
public class FormModule {

    public FormModule() {
        // explicit empty constructor
    }

    @Provides
    @PerActivity
    public RxRulesEngine providesRuleEngine(
            @Nullable UserInteractor currentUserInteractor,
            @Nullable ProgramInteractor programInteractor,
            @Nullable EventInteractor eventInteractor,
            @Nullable EnrollmentInteractor enrollmentInteractor, Logger logger) {
        return new RxRulesEngine(
                currentUserInteractor,
                programInteractor,
                eventInteractor, enrollmentInteractor, logger);
    }

    @Provides
    @PerActivity
    public FormSectionPresenter providesFormSectionPresenter(
            @Nullable ProgramInteractor programInteractor,
            @Nullable EventInteractor eventInteractor,
            @Nullable EnrollmentInteractor enrollmentInteractor,
            RxRulesEngine rxRulesEngine, LocationProvider locationProvider, Logger logger) {
        return new FormSectionPresenterImpl(programInteractor, eventInteractor,
                enrollmentInteractor, rxRulesEngine, locationProvider, logger);
    }

    @Provides
    public DataEntryPresenter providesDataEntryPresenter(
            @Nullable UserInteractor currentUserInteractor,
            @Nullable ProgramInteractor programInteractor,
            @Nullable OptionSetInteractor optionSetInteractor,
            @Nullable EventInteractor eventInteractor,
            @Nullable EnrollmentInteractor enrollmentInteractor,
            @Nullable TrackedEntityInstanceInteractor trackedEntityInstanceInteractor,
            @Nullable TrackedEntityDataValueInteractor dataValueInteractor,
            @Nullable TrackedEntityAttributeValueInteractor trackedEntityAttributeValueInteractor,
            RxRulesEngine rxRulesEngine, Logger logger) {
        return new DataEntryPresenterImpl(currentUserInteractor,
                programInteractor, optionSetInteractor,
                eventInteractor, enrollmentInteractor,
                trackedEntityInstanceInteractor,
                dataValueInteractor, trackedEntityAttributeValueInteractor, rxRulesEngine, logger);
    }

    //TODO: Change naming
    @Provides
    public TeiDashboardPresenter providesTeiDashboardPresenter(
            @Nullable FormSectionPresenter formSectionPresenter) {
        return new TeiDashboardPresenterImpl();
    }

    @Provides
    public TeiNavigationPresenter providesTeiNavigationPresenter(@Nullable EnrollmentInteractor enrollmentInteractor,
                                                                 @Nullable TrackedEntityInstanceInteractor trackedEntityInstanceInteractor,
                                                                 @Nullable TrackedEntityAttributeValueInteractor trackedEntityAttributeValueInteractor,
                                                                 @Nullable ProgramInteractor programInteractor,
                                                                 @Nullable Logger logger) {
        return new TeiNavigationPresenterImpl(enrollmentInteractor, trackedEntityInstanceInteractor,
                trackedEntityAttributeValueInteractor, programInteractor, logger);
    }

    @Provides
    public TeiProgramStagePresenter providesTeiProgramStagePresenter(
            @Nullable TeiDashboardPresenter teiDashboardPresenter,
            @Nullable ProgramInteractor programInteractor,
            @Nullable EventInteractor eventInteractor) {
        return new TeiProgramStagePresenterImpl(teiDashboardPresenter, programInteractor, eventInteractor);
    }

    @Provides
    public TeiProfilePresenter providesTeiProfilePresenter(
            @Nullable EnrollmentInteractor enrollmentInteractor) {
        return new TeiProfilePresenterImpl(enrollmentInteractor);
    }

    @Provides
    public TeiWidgetPresenter providesTeiWidgetPresenter() {
        return new TeiWidgetPresenterImpl();
    }

}
