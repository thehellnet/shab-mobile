package org.thehellnet.shab.mobile.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.thehellnet.shab.mobile.R;
import org.thehellnet.shab.mobile.SHAB;
import org.thehellnet.shab.mobile.service.ShabService;

/**
 * Created by sardylan on 17/07/16.
 */
public abstract class ShabActivity extends AppCompatActivity {

    protected MenuItem menuEnabled;
    protected MenuItem menuMapToogle;
    protected MenuItem menuConfig;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menuEnabled = menu.findItem(R.id.menu_enabled);
        menuMapToogle = menu.findItem(R.id.menu_maptoogle);
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
            case R.id.menu_maptoogle:
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

    protected void toggleShabService() {
        Intent intent = new Intent(SHAB.getAppContext(), ShabService.class);
        if (!SHAB.isServiceRunning(ShabService.class)) {
            startService(intent);
        } else {
            stopService(intent);
        }
        updateMenu();
    }

    protected void updateMenu() {
        menuEnabled.setChecked(SHAB.isServiceRunning(ShabService.class));
        menuEnabled.setIcon(SHAB.isServiceRunning(ShabService.class)
                ? R.drawable.menu_enabled_on
                : R.drawable.menu_enabled_off);

        menuMapToogle.setIcon(this instanceof MainActivity
                ? R.drawable.menu_showmap
                : R.drawable.menu_showinfo);
        menuMapToogle.setTitle(this instanceof MainActivity
                ? R.string.menu_main_maptoogle_showmap
                : R.string.menu_main_maptoogle_showinfo);

        menuConfig.setEnabled(!SHAB.isServiceRunning(ShabService.class));
    }

    protected void toogleMap() {
        Class newActivityClazz = this instanceof MainActivity
                ? MapActivity.class
                : MainActivity.class;
        Intent intent = new Intent(getApplicationContext(), newActivityClazz);
        startActivity(intent);
        finish();
    }

    protected void showRawLog() {

    }

    protected void showConfig() {

    }
}
