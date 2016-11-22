package org.hisp.dhis.android.app;

import android.content.Context;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

//import org.hisp.dhis.android.app.views.ActivityComponent;
import org.hisp.dhis.android.app.views.HomeActivity;
import org.hisp.dhis.client.sdk.ui.bindings.App;
import org.hisp.dhis.client.sdk.ui.bindings.commons.NavigationHandler;
import org.hisp.dhis.client.sdk.ui.bindings.views.DefaultLoginActivity;

import io.fabric.sdk.android.Fabric;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

public class SkeletonApp extends App {
    private AppComponent appComponent;
    private UserComponent userComponent;
    private FormComponent formComponent;
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

        NavigationHandler.loginActivity(DefaultLoginActivity.class);
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

    public FormComponent createFormComponent() {
        isNull(userComponent, "UserComponent must not be null");

        formComponent = userComponent.plus(new FormModule());
        return formComponent;
    }

    public FormComponent getFormComponent() {
        return formComponent;
    }

    public void releaseFormComponent() {
        formComponent = null;
    }
}
