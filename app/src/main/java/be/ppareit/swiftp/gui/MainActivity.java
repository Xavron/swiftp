/*******************************************************************************
 * Copyright (c) 2012-2013 Pieter Pareit.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Contributors:
 * Pieter Pareit - initial API and implementation
 ******************************************************************************/

package be.ppareit.swiftp.gui;

import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import net.vrallev.android.cat.Cat;

import java.util.Arrays;

import be.ppareit.swiftp.App;
import be.ppareit.swiftp.BuildConfig;
import be.ppareit.swiftp.FsService;
import be.ppareit.swiftp.FsSettings;
import be.ppareit.swiftp.R;

/**
 * This is the main activity for swiftp, it enables the user to start the server service
 * and allows the users to change the settings.
 */
public class MainActivity extends AppCompatActivity {

    final static int PERMISSIONS_REQUEST_CODE = 12;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Cat.d("created");
        setTheme(FsSettings.getTheme());
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_layout);
        setSupportActionBar(findViewById(R.id.my_toolbar));
        onBackPressedListener();

        if (!haveReadWritePermissions()) {
            requestReadWritePermissions();
        }

        if (!haveNotiPermissions()) {
            requestNotiPermissions();
        }

        if (App.isFreeVersion() && App.isPaidVersionInstalled()) {
            Cat.d("Running demo while paid is installed");
            AlertDialog ad = new AlertDialog.Builder(this)
                    .setTitle(R.string.demo_while_paid_dialog_title)
                    .setMessage(R.string.demo_while_paid_dialog_message)
                    .setPositiveButton(getText(android.R.string.ok), (d, w) -> finish())
                    .create();
            ad.show();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_activity_fragment, new PreferenceFragment(), null)
                .commit();
    }

    private boolean haveReadWritePermissions() {
        if (VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED
                    && checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private void requestReadWritePermissions() {
        if (VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        // We can no longer request READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE,
        // This is not allowed. We use scoped storage in stead
        if (VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        String[] permissions = new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE};
        requestPermissions(permissions, PERMISSIONS_REQUEST_CODE);
    }

    private boolean haveNotiPermissions() {
        if (VERSION.SDK_INT >= 33) {
            return checkSelfPermission(POST_NOTIFICATIONS) == PERMISSION_GRANTED
                    && checkSelfPermission(POST_NOTIFICATIONS) == PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private void requestNotiPermissions() {
        if (VERSION.SDK_INT < 33) return;
        String[] permissions = new String[]{POST_NOTIFICATIONS};
        requestPermissions(permissions, PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != PERMISSIONS_REQUEST_CODE) {
            Cat.e("Unhandled request code");
            return;
        }
        Cat.d("permissions: " + Arrays.toString(permissions));
        Cat.d("grantResults: " + Arrays.toString(grantResults));
        if (grantResults.length > 0) {
            // Permissions not granted, close down
            for (int result : grantResults) {
                if (result != PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.unable_to_proceed_no_permissions,
                            Toast.LENGTH_LONG).show();
                    Cat.e("Unable to proceed, no permissions given.");
                    finish();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getSupportFragmentManager().popBackStack();
            return true;
        }
        new be.ppareit.swiftp.gui.Menu().init(item, this);
        return true;
    }

    private void onBackPressedListener() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    MainActivity.this.finish();
                } else {
                    getSupportFragmentManager().popBackStack();
                    if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                }
            }
        });
    }
}
