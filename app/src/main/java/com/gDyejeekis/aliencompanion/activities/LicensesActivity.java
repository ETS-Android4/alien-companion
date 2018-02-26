package com.gDyejeekis.aliencompanion.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.artitk.licensefragment.model.License;
import com.artitk.licensefragment.model.LicenseType;
import com.artitk.licensefragment.support.v4.ScrollViewLicenseFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;

import java.util.ArrayList;

/**
 * Created by George on 2/25/2018.
 */

public class LicensesActivity extends ToolbarActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        MyApplication.applyCurrentTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licenses);
        initToolbar();
        setupLicenses();
    }

    @Override
    public void finish() {
        super.finish();
        MyApplication.setPendingTransitions(this);
    }

    private void setupLicenses() {
        ScrollViewLicenseFragment fragment =
                (ScrollViewLicenseFragment) getSupportFragmentManager().findFragmentById(R.id.sv_license_fragment);

        ArrayList<License> customLicenses = new ArrayList<>();
        customLicenses.add(new License(this, "Subsampling Scale Image View", LicenseType.APACHE_LICENSE_20, "2018", "David Morrissey"));
        customLicenses.add(new License(this, "android-gif-drawable", LicenseType.MIT_LICENSE,      "2013",      "Karol Wrótniak"));
        customLicenses.add(new License(this, "RoundedImageView", LicenseType.APACHE_LICENSE_20,      "2017",      "Vincent Mi"));
        customLicenses.add(new License(this, "CircleView", LicenseType.APACHE_LICENSE_20,      "2014",      "Pavlos-Petros Tournaris"));
        customLicenses.add(new License(this, "ColorPicker", LicenseType.MIT_LICENSE,      "2016",      "Petrov Kristiyan"));
        customLicenses.add(new License(this, "ChangeLog Library", LicenseType.APACHE_LICENSE_20,      "2013-2015",      "Gabriele Mariotti"));
        customLicenses.add(new License(this, "betterpickers", LicenseType.APACHE_LICENSE_20,      "2013",      "Derek Brameyer, Code-Troopers"));
        customLicenses.add(new License(this, "Android flow layout", LicenseType.APACHE_LICENSE_20,      "2011",      "Artem Votincev (apmem.org)"));
        customLicenses.add(new License(this, "jsoup", LicenseType.MIT_LICENSE,      "2009-2017",      "Jonathan Hedley (jonathan@hedley.net)"));
        customLicenses.add(new License(this, "snacktory", LicenseType.APACHE_LICENSE_20,      "n/a",      "karussell"));
        customLicenses.add(new License(this, "SwipeBackLayout", LicenseType.APACHE_LICENSE_20,      "2013",      "Isaac Wang"));
        customLicenses.add(new License(this, "DragSortListView", LicenseType.APACHE_LICENSE_20,      "2012",      "Carl Bauer"));

        fragment.addCustomLicense(customLicenses);
    }
}
