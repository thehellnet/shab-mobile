package org.thehellnet.shab.mobile.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.thehellnet.shab.mobile.Permissions;
import org.thehellnet.shab.mobile.R;
import org.thehellnet.shab.mobile.SHAB;
import org.thehellnet.shab.mobile.activity.fragment.Fragments;
import org.thehellnet.shab.mobile.activity.fragment.ShabFragment;
import org.thehellnet.shab.mobile.config.I;
import org.thehellnet.shab.mobile.service.ShabService;

public class MainActivity extends ShabActivity {

    private static final int REQUESTCODE_SETTINGS = 1;
    private static final int REQUESTCODE_PERMISSIONS = 2;

    private MenuItem menuEnabled;
    private MenuItem menuMapToogle;
    private MenuItem menuConfig;

    private FragmentManager fragmentManager;
    private ShabFragment currentFragment;
    private Fragments fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        replaceFragment(Fragments.MAIN, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menuEnabled = menu.findItem(R.id.menu_enabled);
        menuMapToogle = menu.findItem(R.id.menu_maptoggle);
        menuConfig = menu.findItem(R.id.menu_settings);
        updateMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_enabled:
                toggleShabService();
                return true;
            case R.id.menu_maptoggle:
                toogleMap();
                return true;
            case R.id.menu_showrawlog:
                showRawLog();
                return true;
            case R.id.menu_settings:
                showSettings();
                return true;
            case R.id.menu_permissions:
                requestPermissions();
                return true;
            case R.id.menu_about:
                showAbout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUESTCODE_SETTINGS:
                if (resultCode == RESULT_OK) {
                    showToast(R.string.toast_settings_saved);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (currentFragment.getBackFragment() != null) {
            replaceFragment(currentFragment.getBackFragment());
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != REQUESTCODE_PERMISSIONS) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                showToast(R.string.toast_permissions_denied);
                break;
            }
        }
        showToast(R.string.toast_permissions_granted);
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, Permissions.PERMISSIONS, REQUESTCODE_PERMISSIONS);
    }

    private void replaceFragment(Fragments newFragment) {
        replaceFragment(newFragment, true);
    }

    private void replaceFragment(Fragments newFragment, boolean updateMenuItems) {
        fragment = newFragment;
        currentFragment = ShabFragment.getNewFragment(newFragment);
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, currentFragment)
                .commit();

        if (updateMenuItems) {
            updateMenu();
        }
    }

    private void updateMenu() {
        menuEnabled.setChecked(SHAB.isServiceRunning(ShabService.class));
        menuEnabled.setIcon(SHAB.isServiceRunning(ShabService.class)
                ? R.drawable.menu_enabled_on
                : R.drawable.menu_enabled_off);

        switch (fragment) {
            case MAIN:
                menuMapToogle.setIcon(R.drawable.menu_maptoogle_showmap);
                menuMapToogle.setTitle(R.string.menu_main_maptoogle_showmap);
                break;

            default:
                menuMapToogle.setIcon(R.drawable.menu_maptoogle_showinfo);
                menuMapToogle.setTitle(R.string.menu_main_maptoogle_showinfo);
        }

        menuConfig.setEnabled(!SHAB.isServiceRunning(ShabService.class));
    }

    private void toggleShabService() {
        Intent intent = new Intent(SHAB.getAppContext(), ShabService.class);
        if (!SHAB.isServiceRunning(ShabService.class)) {
            startService(intent);
        } else {
            stopService(intent);
        }
        updateMenu();
    }

    private void toogleMap() {
        replaceFragment(fragment == Fragments.MAIN ? Fragments.MAP : Fragments.MAIN);
    }

    private void showRawLog() {
        replaceFragment(Fragments.RAWLOG);
    }

    private void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, REQUESTCODE_SETTINGS);
    }

    private void showAbout() {
        replaceFragment(Fragments.ABOUT);
    }

    private void showToast(int resId) {
        showToast(getString(resId));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
