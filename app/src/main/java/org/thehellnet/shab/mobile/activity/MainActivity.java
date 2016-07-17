package org.thehellnet.shab.mobile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import org.thehellnet.shab.mobile.R;
import org.thehellnet.shab.mobile.SHAB;
import org.thehellnet.shab.mobile.activity.fragment.Fragments;
import org.thehellnet.shab.mobile.activity.fragment.ShabFragment;
import org.thehellnet.shab.mobile.service.ShabService;

public class MainActivity extends ShabActivity {

    private MenuItem menuEnabled;
    private MenuItem menuMapToogle;
    private MenuItem menuConfig;

    private FragmentManager fragmentManager;
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
        replaceFragment(Fragments.MAIN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menuEnabled = menu.findItem(R.id.menu_enabled);
        menuMapToogle = menu.findItem(R.id.menu_maptoggle);
        menuConfig = menu.findItem(R.id.menu_config);
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
            case R.id.menu_config:
                showConfig();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void replaceFragment(Fragments newFragment) {
        fragment = newFragment;
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ShabFragment.getNewFragment(newFragment))
                .commit();
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
            case MAP:
                menuMapToogle.setIcon(R.drawable.menu_maptoogle_showinfo);
                menuMapToogle.setTitle(R.string.menu_main_maptoogle_showinfo);
                break;
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
        switch (fragment) {
            case MAIN:
                replaceFragment(Fragments.MAP);
                updateMenu();
                break;
            case MAP:
                replaceFragment(Fragments.MAIN);
                updateMenu();
                break;
        }
    }

    private void showRawLog() {

    }

    private void showConfig() {

    }


}
