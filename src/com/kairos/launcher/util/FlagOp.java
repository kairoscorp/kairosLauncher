package com.kairos.launcher.util;

/**--------------------------------------------------------------
 * TEAM KAIROS :: DEV Kiko :: CLASS SYNOPSIS
 * Copyright (c) 2017 KAIROS
 *
 * Specifies the type of an operation
 ----------------------------------------------------------------*/

public abstract class FlagOp {

    public static FlagOp NO_OP = new FlagOp() {};

    private FlagOp() {}

    public int apply(int flags) {
        return flags;
    }

    public static FlagOp addFlag(final int flag) {
        return new FlagOp() {
            @Override
            public int apply(int flags) {
                return flags | flag;
            }
        };
    }

    public static FlagOp removeFlag(final int flag) {
        return new FlagOp() {
            @Override
            public int apply(int flags) {
                return flags & ~flag;
            }
        };
    }
}
