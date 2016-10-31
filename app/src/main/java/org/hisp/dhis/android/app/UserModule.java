/*
 * Copyright (c) 2016, University of Oslo
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.app;

import android.app.Application;
import android.content.Context;

import org.hisp.dhis.client.sdk.core.D2;
import org.hisp.dhis.client.sdk.core.user.UserInteractor;
import org.hisp.dhis.client.sdk.ui.AppPreferences;
import org.hisp.dhis.client.sdk.ui.AppPreferencesImpl;
import org.hisp.dhis.client.sdk.ui.bindings.commons.SyncDateWrapper;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.HomePresenter;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.HomePresenterImpl;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.LauncherPresenter;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.LauncherPresenterImpl;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.LoginPresenter;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.LoginPresenterImpl;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.ProfilePresenter;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.ProfilePresenterImpl;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.SettingsPresenter;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.SettingsPresenterImpl;
import org.hisp.dhis.client.sdk.utils.Logger;

import javax.annotation.Nullable;

import dagger.Module;
import dagger.Provides;

import static org.hisp.dhis.client.sdk.utils.StringUtils.isEmpty;

@Module
public final class UserModule {
    private final D2 sdkInstance;

    public UserModule(Application application) {
        this.sdkInstance = D2.builder(application).build();
    }

    public UserModule(Application application, String serverUrl) {
        if (isEmpty(serverUrl)) {
            throw new IllegalArgumentException("serverUrl must not be null");
        }

        this.sdkInstance = D2.builder(application)
                .baseUrl(serverUrl)
                .build();
    }

    @Provides
    @PerUser
    public D2 sdkInstance() {
        return sdkInstance;
    }

    @Provides
    @PerUser
    @Nullable
    public UserInteractor userInteractor(D2 d2) {
        if (!isEmpty(d2.serverUrl())) {
            return d2.me();
        }

        return null;
    }

    @Provides
    @PerUser
    public LauncherPresenter launcherPresenter(@Nullable UserInteractor userInteractor) {
        return new LauncherPresenterImpl(userInteractor);
    }

    @Provides
    @PerUser
    public LoginPresenter loginPresenter(@Nullable UserInteractor userInteractor, Logger logger) {
        return new LoginPresenterImpl(userInteractor, null, logger);
    }

    @Provides
    @PerUser
    public SyncDateWrapper syncDateWrapper(@Nullable AppPreferences appPreferences) {
        return new SyncDateWrapper(appPreferences);
    }

    @Provides
    @PerUser
    public HomePresenter homePresenter(@Nullable UserInteractor userInteractor, Logger logger) {
        return new HomePresenterImpl(userInteractor, null, logger);
    }

    @Provides
    @PerUser
    public ProfilePresenter profilePresenter(@Nullable UserInteractor userInteractor, Logger logger) {
        return new ProfilePresenterImpl(userInteractor, null, null, null, logger);
    }

    @Provides
    @PerUser
    public SettingsPresenter settingsPresenter() {
        return new SettingsPresenterImpl(null, null);
    }
}