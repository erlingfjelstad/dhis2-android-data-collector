package org.hisp.dhis.android.app.views.drawerform.singleevent;

import android.support.annotation.Nullable;

import org.hisp.dhis.android.app.LocationProvider;
import org.hisp.dhis.android.app.PerActivity;
import org.hisp.dhis.android.app.model.RxRulesEngine;
import org.hisp.dhis.android.app.views.drawerform.NavigationLockController;
import org.hisp.dhis.android.app.views.drawerform.NavigationLockControllerImpl;
import org.hisp.dhis.android.app.views.drawerform.RightDrawerController;
import org.hisp.dhis.android.app.views.drawerform.RightDrawerControllerImpl;
import org.hisp.dhis.android.app.views.drawerform.form.FormPresenter;
import org.hisp.dhis.android.app.views.drawerform.form.FormPresenterImpl;
import org.hisp.dhis.android.app.views.drawerform.form.dataentry.DataEntryPresenter;
import org.hisp.dhis.android.app.views.drawerform.form.dataentry.DataEntryPresenterImpl;
import org.hisp.dhis.android.app.views.drawerform.singleevent.drawer.WidgetDrawerPresenter;
import org.hisp.dhis.android.app.views.drawerform.singleevent.drawer.WidgetDrawerPresenterImpl;
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
public class SingleEventDashboardModule {

    public SingleEventDashboardModule() {
        // explicit empty constructor
    }

    @Provides
    @PerActivity
    public RightDrawerController providesRightDrawerController(
            @Nullable SingleEventDashboardPresenter singleEventDashboardPresenter) {
        return new RightDrawerControllerImpl(singleEventDashboardPresenter);
    }

    @Provides
    @PerActivity
    public NavigationLockController providesNavigationLockController() {
        return new NavigationLockControllerImpl(null);
    }

    @Provides
    @PerActivity
    public RxRulesEngine providesRuleEngine(
            @Nullable UserInteractor currentUserInteractor,
            @Nullable ProgramInteractor programInteractor,
            @Nullable EventInteractor eventInteractor,
            Logger logger) {
        return new RxRulesEngine(
                currentUserInteractor,
                programInteractor,
                eventInteractor, null, logger);
    }

    @Provides
    @PerActivity
    public FormPresenter providesFormPresenter(
            @Nullable ProgramInteractor programInteractor,
            @Nullable EventInteractor eventInteractor,
            @Nullable EnrollmentInteractor enrollmentInteractor,
            RxRulesEngine rxRulesEngine, LocationProvider locationProvider, Logger logger) {
        return new FormPresenterImpl(programInteractor, eventInteractor,
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

    @Provides
    @PerActivity
    public SingleEventDashboardPresenter providesSingleEventDashboardPresenter() {
        return new SingleEventDashboardPresenterImpl();
    }

    @Provides
    @PerActivity
    public WidgetDrawerPresenter providesWidgetDrawerPresenter(@Nullable SingleEventDashboardPresenter singleEventDashboardPresenter,
                                                               @Nullable EventInteractor eventInteractor,
                                                               @Nullable ProgramInteractor programInteractor,
                                                               @Nullable Logger logger) {
        return new WidgetDrawerPresenterImpl(singleEventDashboardPresenter,
                eventInteractor,
                programInteractor,
                logger);
    }
}
