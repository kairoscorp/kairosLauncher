package com.kairos.launcher.model;

import com.kairos.launcher.ItemInfo;
import com.kairos.launcher.LauncherAppWidgetInfo;
import com.kairos.launcher.ShortcutInfo;
import com.kairos.launcher.compat.PackageInstallerCompat;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Tests for {@link PackageInstallStateChangedTask}
 */
public class PackageInstallStateChangedTaskTest extends BaseModelUpdateTaskTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        initializeData("package_install_state_change_task_data");
    }

    private PackageInstallStateChangedTask newTask(String pkg, int progress) {
        PackageInstallerCompat.PackageInstallInfo installInfo = new PackageInstallerCompat.PackageInstallInfo(pkg);
        installInfo.progress = progress;
        installInfo.state = PackageInstallerCompat.STATUS_INSTALLING;
        return new PackageInstallStateChangedTask(installInfo);
    }

    public void testSessionUpdate_ignore_installed() throws Exception {
        executeTaskForTest(newTask("app1", 30));

        // No shortcuts were updated
        verifyProgressUpdate(0);
    }

    public void testSessionUpdate_shortcuts_updated() throws Exception {
        executeTaskForTest(newTask("app3", 30));

        verifyProgressUpdate(30, 5L, 6L, 7L);
    }

    public void testSessionUpdate_widgets_updated() throws Exception {
        executeTaskForTest(newTask("app4", 30));

        verifyProgressUpdate(30, 8L, 9L);
    }

    private void verifyProgressUpdate(int progress, Long... idsUpdated) {
        HashSet<Long> updates = new HashSet<>(Arrays.asList(idsUpdated));
        for (ItemInfo info : bgDataModel.itemsIdMap) {
            if (info instanceof ShortcutInfo) {
                assertEquals(updates.contains(info.id) ? progress: 0,
                        ((ShortcutInfo) info).getInstallProgress());
            } else {
                assertEquals(updates.contains(info.id) ? progress: -1,
                        ((LauncherAppWidgetInfo) info).installProgress);
            }
        }
    }
}
