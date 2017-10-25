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
package com.kairos.launcher.logging;

import android.util.ArrayMap;
import android.util.SparseArray;
import android.view.View;

import com.kairos.launcher.ButtonDropTarget;
import com.kairos.launcher.DeleteDropTarget;
import com.kairos.launcher.InfoDropTarget;
import com.kairos.launcher.ItemInfo;
import com.kairos.launcher.LauncherSettings;
import com.kairos.launcher.UninstallDropTarget;
import com.kairos.launcher.userevent.nano.LauncherLogProto;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Helper methods for logging.
 */
public class LoggerUtils {
    private static final ArrayMap<Class, SparseArray<String>> sNameCache = new ArrayMap<>();
    private static final String UNKNOWN = "UNKNOWN";

    public static String getFieldName(int value, Class c) {
        SparseArray<String> cache;
        synchronized (sNameCache) {
            cache = sNameCache.get(c);
            if (cache == null) {
                cache = new SparseArray<>();
                for (Field f : c.getDeclaredFields()) {
                    if (f.getType() == int.class && Modifier.isStatic(f.getModifiers())) {
                        try {
                            f.setAccessible(true);
                            cache.put(f.getInt(null), f.getName());
                        } catch (IllegalAccessException e) {
                            // Ignore
                        }
                    }
                }
                sNameCache.put(c, cache);
            }
        }
        String result = cache.get(value);
        return result != null ? result : UNKNOWN;
    }

    public static String getActionStr(LauncherLogProto.Action action) {
        switch (action.type) {
            case LauncherLogProto.Action.Type.TOUCH: return getFieldName(action.touch, LauncherLogProto.Action.Touch.class);
            case LauncherLogProto.Action.Type.COMMAND: return getFieldName(action.command, LauncherLogProto.Action.Command.class);
            default: return UNKNOWN;
        }
    }

    public static String getTargetStr(LauncherLogProto.Target t) {
        if (t == null){
            return "";
        }
        switch (t.type) {
            case LauncherLogProto.Target.Type.ITEM:
                return getItemStr(t);
            case LauncherLogProto.Target.Type.CONTROL:
                return getFieldName(t.controlType, LauncherLogProto.ControlType.class);
            case LauncherLogProto.Target.Type.CONTAINER:
                String str = getFieldName(t.containerType, LauncherLogProto.ContainerType.class);
                if (t.containerType == LauncherLogProto.ContainerType.WORKSPACE) {
                    str += " id=" + t.pageIndex;
                } else if (t.containerType == LauncherLogProto.ContainerType.FOLDER) {
                    str += " grid(" + t.gridX + "," + t.gridY+ ")";
                }
                return str;
            default:
                return "UNKNOWN TARGET TYPE";
        }
    }

    private static String getItemStr(LauncherLogProto.Target t) {
        String typeStr = getFieldName(t.itemType, LauncherLogProto.ItemType.class);
        if (t.packageNameHash != 0) {
            typeStr += ", packageHash=" + t.packageNameHash;
        }
        if (t.componentHash != 0) {
            typeStr += ", componentHash=" + t.componentHash;
        }
        if (t.intentHash != 0) {
            typeStr += ", intentHash=" + t.intentHash;
        }
        return typeStr + ", grid(" + t.gridX + "," + t.gridY + "), span(" + t.spanX + "," + t.spanY
                + "), pageIdx=" + t.pageIndex;
    }

    public static LauncherLogProto.Target newItemTarget(View v) {
        return (v.getTag() instanceof ItemInfo)
                ? newItemTarget((ItemInfo) v.getTag())
                : newTarget(LauncherLogProto.Target.Type.ITEM);
    }

    public static LauncherLogProto.Target newItemTarget(ItemInfo info) {
        LauncherLogProto.Target t = newTarget(LauncherLogProto.Target.Type.ITEM);
        switch (info.itemType) {
            case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                t.itemType = LauncherLogProto.ItemType.APP_ICON;
                break;
            case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                t.itemType = LauncherLogProto.ItemType.SHORTCUT;
                break;
            case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                t.itemType = LauncherLogProto.ItemType.FOLDER_ICON;
                break;
            case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                t.itemType = LauncherLogProto.ItemType.WIDGET;
                break;
            case LauncherSettings.Favorites.ITEM_TYPE_DEEP_SHORTCUT:
                t.itemType = LauncherLogProto.ItemType.DEEPSHORTCUT;
                break;
        }
        return t;
    }

    public static LauncherLogProto.Target newDropTarget(View v) {
        if (!(v instanceof ButtonDropTarget)) {
            return newTarget(LauncherLogProto.Target.Type.CONTAINER);
        }
        LauncherLogProto.Target t = newTarget(LauncherLogProto.Target.Type.CONTROL);
        if (v instanceof InfoDropTarget) {
            t.controlType = LauncherLogProto.ControlType.APPINFO_TARGET;
        } else if (v instanceof UninstallDropTarget) {
            t.controlType = LauncherLogProto.ControlType.UNINSTALL_TARGET;
        } else if (v instanceof DeleteDropTarget) {
            t.controlType = LauncherLogProto.ControlType.REMOVE_TARGET;
        }
        return t;
    }

    public static LauncherLogProto.Target newTarget(int targetType) {
        LauncherLogProto.Target t = new LauncherLogProto.Target();
        t.type = targetType;
        return t;
    }
    public static LauncherLogProto.Target newContainerTarget(int containerType) {
        LauncherLogProto.Target t = newTarget(LauncherLogProto.Target.Type.CONTAINER);
        t.containerType = containerType;
        return t;
    }

    public static LauncherLogProto.Action newAction(int type) {
        LauncherLogProto.Action a = new LauncherLogProto.Action();
        a.type = type;
        return a;
    }
    public static LauncherLogProto.Action newCommandAction(int command) {
        LauncherLogProto.Action a = newAction(LauncherLogProto.Action.Type.COMMAND);
        a.command = command;
        return a;
    }
    public static LauncherLogProto.Action newTouchAction(int touch) {
        LauncherLogProto.Action a = newAction(LauncherLogProto.Action.Type.TOUCH);
        a.touch = touch;
        return a;
    }

    public static LauncherLogProto.LauncherEvent newLauncherEvent(LauncherLogProto.Action action, LauncherLogProto.Target... srcTargets) {
        LauncherLogProto.LauncherEvent event = new LauncherLogProto.LauncherEvent();
        event.srcTarget = srcTargets;
        event.action = action;
        return event;
    }
}
