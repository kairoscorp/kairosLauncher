/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kairos.launcher.model;

import android.content.ComponentName;

import com.kairos.launcher.AllAppsList;
import com.kairos.launcher.ItemInfo;
import com.kairos.launcher.LauncherAppState;
import com.kairos.launcher.LauncherAppWidgetInfo;
import com.kairos.launcher.ShortcutInfo;
import com.kairos.launcher.compat.PackageInstallerCompat;
import com.kairos.launcher.LauncherModel;

import java.util.HashSet;

/**
 * Handles changes due to a sessions updates for a currently installing app.
 */
public class PackageInstallStateChangedTask extends ExtendedModelTask {

    private final PackageInstallerCompat.PackageInstallInfo mInstallInfo;

    public PackageInstallStateChangedTask(PackageInstallerCompat.PackageInstallInfo installInfo) {
        mInstallInfo = installInfo;
    }

    @Override
    public void execute(LauncherAppState app, BgDataModel dataModel, AllAppsList apps) {
        if (mInstallInfo.state == PackageInstallerCompat.STATUS_INSTALLED) {
            // Ignore install success events as they are handled by Package add events.
            return;
        }

        synchronized (dataModel) {
            final HashSet<ItemInfo> updates = new HashSet<>();
            for (ItemInfo info : dataModel.itemsIdMap) {
                if (info instanceof ShortcutInfo) {
                    ShortcutInfo si = (ShortcutInfo) info;
                    ComponentName cn = si.getTargetComponent();
                    if (si.isPromise() && (cn != null)
                            && mInstallInfo.packageName.equals(cn.getPackageName())) {
                        si.setInstallProgress(mInstallInfo.progress);

                        if (mInstallInfo.state == PackageInstallerCompat.STATUS_FAILED) {
                            // Mark this info as broken.
                            si.status &= ~ShortcutInfo.FLAG_INSTALL_SESSION_ACTIVE;
                        }
                        updates.add(si);
                    }
                }
            }

            for (LauncherAppWidgetInfo widget : dataModel.appWidgets) {
                if (widget.providerName.getPackageName().equals(mInstallInfo.packageName)) {
                    widget.installProgress = mInstallInfo.progress;
                    updates.add(widget);
                }
            }

            if (!updates.isEmpty()) {
                scheduleCallbackTask(new LauncherModel.CallbackTask() {
                    @Override
                    public void execute(LauncherModel.Callbacks callbacks) {
                        callbacks.bindRestoreItemsChange(updates);
                    }
                });
            }
        }
    }
}
