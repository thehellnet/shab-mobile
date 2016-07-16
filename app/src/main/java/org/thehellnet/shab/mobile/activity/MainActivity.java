package org.thehellnet.shab.mobile.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.thehellnet.shab.mobile.R;
import org.thehellnet.shab.mobile.SHAB;
import org.thehellnet.shab.mobile.protocol.ShabSocket;

public class MainActivity extends AppCompatActivity {

    private MenuItem menuEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        menuEnabled = menu.findItem(R.id.menu_enabled);
        menuEnabled.setChecked(SHAB.isServiceRunning(ShabSocket.class));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_mapview) {
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
