/*
 * Copyright 2018-present KunMinX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kunminx.puremusic;

import android.os.Bundle;
import android.view.View;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.kunminx.architecture.ui.page.BaseActivity;
import com.kunminx.architecture.ui.page.DataBindingConfig;
import com.kunminx.puremusic.ui.callback.SharedViewModel;
import com.kunminx.puremusic.ui.helper.DrawerCoordinateHelper;
import com.kunminx.puremusic.ui.state.MainActivityViewModel;

/**
 * Create by KunMinX at 19/10/16
 */

public class MainActivity extends BaseActivity {

    private MainActivityViewModel mState;
    private SharedViewModel mPageCallback;
    private boolean mIsListened = false;

    @Override
    protected void initViewModel() {
        mState = getActivityScopeViewModel(MainActivityViewModel.class);
        mPageCallback = getApplicationScopeViewModel(SharedViewModel.class);
    }

    @Override
    protected DataBindingConfig getDataBindingConfig() {
        return new DataBindingConfig(R.layout.activity_main, BR.vm, mState)
                .addBindingParam(BR.event, new EventHandler());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPageCallback.isToCloseActivityIfAllowed().observeInActivity(this, aBoolean -> {
            NavController nav = Navigation.findNavController(this, R.id.main_fragment_host);
            if (nav.getCurrentDestination() != null && nav.getCurrentDestination().getId() != R.id.mainFragment) {
                nav.navigateUp();
            } else if (mState.isDrawerOpened.get()) {
                mPageCallback.requestToOpenOrCloseDrawer(false);
            } else {
                super.onBackPressed();
            }
        });

        mPageCallback.isToOpenOrCloseDrawer().observeInActivity(this, aBoolean -> {
            mState.openDrawer.setValue(aBoolean);
        });

        DrawerCoordinateHelper.getInstance().isEnableSwipeDrawer().observeInActivity(this, aBoolean -> {
            mState.allowDrawerOpen.setValue(aBoolean);
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!mIsListened) {
            mPageCallback.requestToAddSlideListener(true);
            mIsListened = true;
        }
    }

    @Override
    public void onBackPressed() {
        mPageCallback.requestToCloseSlidePanelIfExpanded(true);
    }

    public class EventHandler extends DrawerLayout.SimpleDrawerListener {
        @Override
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
            mState.isDrawerOpened.set(true);
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            super.onDrawerClosed(drawerView);
            mState.isDrawerOpened.set(false);
            mState.openDrawer.setValue(false);
        }
    }
}
