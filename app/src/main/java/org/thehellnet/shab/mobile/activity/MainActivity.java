package org.thehellnet.shab.mobile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.thehellnet.shab.mobile.R;
import org.thehellnet.shab.mobile.SHAB;
import org.thehellnet.shab.mobile.service.ShabService;

public class MainActivity extends AppCompatActivity {

    private MenuItem menuEnabled;
    private MenuItem menuConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menuEnabled = menu.findItem(R.id.menu_enabled);
        menuConfig = menu.findItem(R.id.menu_config);
        updateMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_enabled:
                toggleShabService();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void updateMenu() {
        menuEnabled.setChecked(SHAB.isServiceRunning(ShabService.class));
        menuEnabled.setIcon(SHAB.isServiceRunning(ShabService.class)
                ? R.drawable.menu_enabled_on
                : R.drawable.menu_enabled_off);

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
}
