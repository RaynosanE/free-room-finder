/**
 * Free Room Finder (FRF)
 * Tired of rooms on campus always being in use? Fear no more the FRF is here.
 *
 * Copyright (C) 2013 Joseph Heron, Jonathan Gillett, and Daniel Smullen
 * All rights reserved.
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.uoit.freeroomfinder;

import java.util.List;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import com.uoit.freeroomfinder.preferences.OnPreferenceDialogClosedListener;
import com.uoit.freeroomfinder.preferences.PreferenceDialog;

/**
 * SettingsActivity A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets, settings are split by
 * category, with category headers shown to the left of the list of settings.
 * 
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html"> Android Design:
 * Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings API Guide</a> for more
 * information on developing a Settings UI.
 * 
 * @author Joseph Heron
 * @author Jonathan Gilett
 * @author Daniel Smullen
 */
public class SettingsActivity extends PreferenceActivity
{
    /**
     * Determines whether to always show the simplified settings UI, where settings are presented in
     * a single list. When false, settings are shown as a master/detail two-pane view on tablets.
     * When true, a single pane is shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;

    /*
     * (non-Javadoc)
     * 
     * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Default implementation.
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * setupActionBar Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            // Show the up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case android.R.id.home:
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPostCreate(android.os.Bundle)
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        // Default implementation. Incorporates the SimplePreferencesScreen we provide.
        super.onPostCreate(savedInstanceState);
        setupSimplePreferencesScreen();
    }

    /**
     * setupSimplePreferencesScreen Shows the simplified settings UI if the device configuration if
     * the device configuration dictates that a simplified, single-pane UI should be shown.
     */
    @SuppressWarnings("deprecation")
    private void setupSimplePreferencesScreen()
    {
        if (!isSimplePreferences(this))
        {
            return;
        }
        
        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.
        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);

        PreferenceDialog deleteAccount = (PreferenceDialog) this.findPreference("delete_account");
        
        DatabaseInterface dbi = new DatabaseInterface(getBaseContext());

        // Disable delete account if user does not have an account
        if (dbi.getUser() == null)
        {
            deleteAccount.setEnabled(false);
            deleteAccount.setSelectable(false);
        }
        else
        {
            deleteAccount.setEnabled(true);
            deleteAccount.setSelectable(true);
        }

        // Contains the logic for when the preferences dialog is closed.
        deleteAccount.setOnPreferenceDialogClosedListener(new OnPreferenceDialogClosedListener()
        {
            /*
             * (non-Javadoc)
             * 
             * @see com.uoit.freeroomfinder.preferences.OnPreferenceDialogClosedListener#
             * onPreferenceDialogClosed(boolean)
             */
            @Override
            public void onPreferenceDialogClosed(boolean positiveResult)
            {
                if (positiveResult)
                {
                    DatabaseInterface dbi = new DatabaseInterface(getBaseContext());
                    dbi.deleteAll();
                }
            }
        });

        // Add 'search' preferences, and a corresponding header.
        PreferenceCategory fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_search);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_search);

        // Contains the logic for setting the 24 hour clock preferences.
        findPreference("army_clock").setOnPreferenceChangeListener(new OnPreferenceChangeListener()
        {
            /*
             * (non-Javadoc)
             * 
             * @see
             * android.preference.Preference.OnPreferenceChangeListener#onPreferenceChange(android
             * .preference.Preference, java.lang.Object)
             */
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                DateTimeUtility.setArmyClock(Boolean.valueOf(newValue.toString()));
                return true;
            }

        });

        // Add 'feedback' preferences, and a corresponding header.
        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_feedback);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_feedback);

        findPreference("source_code").setOnPreferenceClickListener(new OnPreferenceClickListener()
        {
            /*
             * (non-Javadoc)
             * 
             * @see
             * android.preference.Preference.OnPreferenceClickListener#onPreferenceClick(android
             * .preference.Preference)
             */
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(SettingsActivity.this.getString(R.string.source_code_site)));
                SettingsActivity.this.startActivity(i);
                return true;
            }
        });

        /* Set an onclick listener for contact developers */
        findPreference("contact").setOnPreferenceClickListener(new OnPreferenceClickListener()
        {
            /*
             * (non-Javadoc)
             * 
             * @see
             * android.preference.Preference.OnPreferenceClickListener#onPreferenceClick(android
             * .preference.Preference)
             */
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                /**
                 * Create the Intent
                 */
                final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

                /**
                 * Fill it with Data
                 */
                emailIntent.setType("plain/text");
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, SettingsActivity.this
                        .getResources().getStringArray(R.array.dev_emails));
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Free Room Finder");
                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
                
                /**
                 * Send it off to the Activity-Chooser
                 */
                SettingsActivity.this.startActivity(Intent.createChooser(emailIntent,
                        "Email the developers..."));
                return true;
            }
        });
    }

    /** {@inheritDoc} */
    /*
     * (non-Javadoc)
     * 
     * @see android.preference.PreferenceActivity#onIsMultiPane()
     */
    @Override
    public boolean onIsMultiPane()
    {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * isXLargeTablet Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     * 
     * @param context
     *            The context for the activity.
     * 
     * @return Returns whether the device is extra large or not. True if the device is large.
     */
    private static boolean isXLargeTablet(Context context)
    {
        return (context.getResources().getConfiguration().screenLayout 
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * isSimplePreferences Determines whether the simplified settings UI should be shown. This is true if this is forced
     * via {@link #ALWAYS_SIMPLE_PREFS}, or the device doesn't have newer APIs like
     * {@link PreferenceFragment}, or the device doesn't have an extra-large screen. In these cases,
     * a single-pane "simplified" settings UI should be shown.
     * 
     * @param context The context for the activity.
     * 
     * @return Returns true if the simplified settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context)
    {
        return ALWAYS_SIMPLE_PREFS || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    /** {@inheritDoc} */
    /* (non-Javadoc)
     * @see android.preference.PreferenceActivity#onBuildHeaders(java.util.List)
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target)
    {
        if (!isSimplePreferences(this))
        {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }
}
