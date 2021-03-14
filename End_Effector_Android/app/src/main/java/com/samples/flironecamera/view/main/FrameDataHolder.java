/*******************************************************************
 * @title FLIR THERMAL SDK
 * @file FrameDataHolder.java
 * @Author FLIR Systems AB
 *
 * @brief Container class that holds references to Bitmap images
 *
 * Copyright 2019:    FLIR Systems
 ********************************************************************/

package com.samples.flironecamera.view.main;

import android.graphics.Bitmap;

public class FrameDataHolder {
    public final Bitmap dcBitmap;

    FrameDataHolder(Bitmap dcBitmap){
        this.dcBitmap = dcBitmap;
    }
}
