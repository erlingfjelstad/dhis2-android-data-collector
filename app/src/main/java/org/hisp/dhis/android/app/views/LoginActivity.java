package org.hisp.dhis.android.app.views;

import android.os.Bundle;

import org.hisp.dhis.android.app.BuildConfig;
import org.hisp.dhis.client.sdk.ui.bindings.views.DefaultLoginActivity;

public class LoginActivity extends DefaultLoginActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getServerUrl().setText(BuildConfig.SERVER_URL);
        getUsername().setText(BuildConfig.USERNAME);
        getPassword().setText(BuildConfig.PASSWORD);

    }
}
