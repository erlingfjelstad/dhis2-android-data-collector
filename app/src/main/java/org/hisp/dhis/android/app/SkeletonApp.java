package org.hisp.dhis.android.app;

import android.content.Context;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import org.hisp.dhis.android.app.views.HomeActivity;
import org.hisp.dhis.android.app.views.LoginActivity;
import org.hisp.dhis.android.app.views.drawerform.form.FormComponent;
import org.hisp.dhis.android.app.views.drawerform.singleevent.SingleEventDashboardComponent;
import org.hisp.dhis.android.app.views.drawerform.singleevent.SingleEventDashboardModule;
import org.hisp.dhis.android.app.views.drawerform.trackedentityinstance.TeiDashboardComponent;
import org.hisp.dhis.android.app.views.drawerform.trackedentityinstance.TeiDashboardModule;
import org.hisp.dhis.android.app.views.enrollment.EnrollmentComponent;
import org.hisp.dhis.android.app.views.enrollment.EnrollmentModule;
import org.hisp.dhis.client.sdk.ui.bindings.App;
import org.hisp.dhis.client.sdk.ui.bindings.commons.NavigationHandler;

import io.fabric.sdk.android.Fabric;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

//import org.hisp.dhis.android.app.views.ActivityComponent;

public class SkeletonApp extends App {
    private AppComponent appComponent;
    private UserComponent userComponent;
    private TeiDashboardComponent teiDashboardComponent;
    private EnrollmentComponent enrollmentComponent;
    private SingleEventDashboardComponent singleEventDashboardComponent;
    //private ActivityComponent activityComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        // enabling crashlytics only for release builds
        Crashlytics crashlytics = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder()
                        .disabled(BuildConfig.DEBUG)
                        .build())
                .build();
        Fabric.with(this, crashlytics);

        NavigationHandler.loginActivity(LoginActivity.class);
        NavigationHandler.homeActivity(HomeActivity.class);

        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
        userComponent = appComponent.plus(new UserModule(this));

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public AppComponent getAppComponent() {
        return appComponent;
    }

    @Override
    public UserComponent getUserComponent() {
        return userComponent;
    }

    @Override
    public UserComponent createUserComponent(String serverUrl) {
        return (userComponent = appComponent.plus(new UserModule(this, serverUrl)));
    }

    public TeiDashboardComponent createTeiDashboardComponent() {
        isNull(userComponent, "UserComponent must not be null");

        teiDashboardComponent = userComponent.plus(new TeiDashboardModule());
        return teiDashboardComponent;
    }

    public TeiDashboardComponent getTeiDashboardComponent() {
        return teiDashboardComponent;
    }

    public void releaseTeiDashboardComponent() {
        teiDashboardComponent = null;
    }

    public EnrollmentComponent createEnrollmentComponent() {
        enrollmentComponent = userComponent.plus(new EnrollmentModule());
        return enrollmentComponent;
    }

    public EnrollmentComponent getEnrollmentComponent() {
        return enrollmentComponent;
    }

    public void releaseEnrollmentComponent() {
        enrollmentComponent = null;
    }

    public SingleEventDashboardComponent createSingleEventDashboardComponent() {
        singleEventDashboardComponent = userComponent.plus(new SingleEventDashboardModule());
        return singleEventDashboardComponent;
    }

    public SingleEventDashboardComponent getSingleEventDashboardComponent() {
        return singleEventDashboardComponent;
    }

    public void releaseSingleEventDashboardComponent() {
        singleEventDashboardComponent = null;
    }

    public FormComponent getFormComponent() {
        if (teiDashboardComponent != null) {
            return teiDashboardComponent;
        } else {
            return singleEventDashboardComponent;
        }
    }
}
