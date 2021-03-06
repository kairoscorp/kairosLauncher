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

package com.kairos.launcher.compat;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
/**--------------------------------------------------------------
 * TEAM KAIROS :: DEV Daniel :: CLASS SYNOPSIS
 * Copyright (c) 2017 KAIROS
 * Manages apps for different users (Nougat 7.1)
 ----------------------------------------------------------------*/
@TargetApi(Build.VERSION_CODES.N_MR1)
public class UserManagerCompatVNMr1 extends UserManagerCompatVN {

    UserManagerCompatVNMr1(Context context) {
        super(context);
    }

    @Override
    public boolean isDemoUser() {
        return mUserManager.isDemoUser();
    }
}
