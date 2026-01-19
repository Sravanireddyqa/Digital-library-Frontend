package com.simats.digitallibrary;

import com.journeyapps.barcodescanner.CaptureActivity;

/**
 * Portrait-locked capture activity for QR scanning
 * Prevents camera from rotating
 */
public class PortraitCaptureActivity extends CaptureActivity {
    // This class is intentionally empty
    // It's used to lock camera to portrait orientation via AndroidManifest
}
