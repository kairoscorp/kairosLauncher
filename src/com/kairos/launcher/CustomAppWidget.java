package com.kairos.launcher;

/**--------------------------------------------------------------
 * TEAM KAIROS :: DEV CARLOS :: CLASS SYNOPSIS
 * Copyright (c) 2017 KAIROS
 *
 * Insterface to create a custom widget
 ----------------------------------------------------------------*/

public interface CustomAppWidget {
    public String getLabel();
    public int getPreviewImage();
    public int getIcon();
    public int getWidgetLayout();

    public int getSpanX();
    public int getSpanY();
    public int getMinSpanX();
    public int getMinSpanY();
    public int getResizeMode();
}
