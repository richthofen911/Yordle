package net.callofdroidy.yordle;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ActivityMain extends AppCompatActivity {
    private final static String TAG = "ActMain";

    private static int PORT = 8088;

    Handler handler;

    TextView tvGreetings;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //setSupportActionBar(toolbar);

        tvGreetings = (TextView) findViewById(R.id.tv_greetings);


    }

    @Override
    protected void onStop() {
        super.onStop();


    }

    public void onStartClick(View v) {
        Intent startIntent = new Intent(this, WebService.class);
        startService(startIntent);
    }

    public void onStopClick(View v) {
        Intent stopIntent = new Intent(this, WebService.class);
        stopService(stopIntent);
    }

    public void onFabClick(View v){
        DirectorySelector ds = new DirectorySelector(this, new DirectorySelector.DirectorySelectCallback() {
            @Override
            public void onDirSelected(String chosenDir) {
                Log.e(TAG, "onChosenDir: " + chosenDir);
                getSharedPreferences("serverConfig", 0).edit().putString("rootDir", chosenDir).apply();
            }
        });
        ds.chooseDirectory();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
