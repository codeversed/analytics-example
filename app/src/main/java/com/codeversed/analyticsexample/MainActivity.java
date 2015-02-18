package com.codeversed.analyticsexample;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.codeversed.analyticsexample.TimeElapseUtils.TimeTrack;
import com.google.android.gms.analytics.HitBuilders;

import java.util.Random;
import java.util.concurrent.TimeUnit;


public class MainActivity extends Activity {

    TimeElapseUtils timeElapseUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Analytics Tracking
        ((MyApp) getApplicationContext())
                .sendBothAnalyticsScreen(getString(R.string.screen_Home));

        timeElapseUtils = TimeElapseUtils.getInstance(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void trackEvent(View view) {

        TextView textView = (TextView)findViewById(R.id.editText);
        String dynamicText = textView.getText().toString();

        if (dynamicText.isEmpty()) dynamicText = "Blank";

        // Analytics Tracking
        ((MyApp) getApplicationContext()).sendBothAnalyticsEvent(
                new String[]{ getString(R.string.screen_Home), null },
                new HitBuilders.EventBuilder()
                        .setCategory(getString(R.string.category_interacting))
                        .setAction(getString(R.string.action_button_click))
                        .setLabel(dynamicText)
                        .setValue(1));
    }

    public void trackTime(View view) {

        final Random random = new Random();

        String[] strings = {
                getString(R.string.action_fake_process),
                getString(R.string.action_fake_process_one),
                getString(R.string.action_fake_process_two),
                getString(R.string.action_fake_process_three)
        };

        final String randomString = strings[random.nextInt(strings.length)];


        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {


                // Analytics Time Tracking Start
                timeElapseUtils.trackStart(randomString,
                        new TimeTrack(TimeUnit.MILLISECONDS,
                                getString(R.string.category_processing),
                                randomString,
                                getString(R.string.label_fake_process)));

            }

            @Override
            protected String doInBackground(Void... unused) {

                try {

                    // nextInt is normally exclusive of the top value,
                    // so add 1 to make it inclusive
                    int randomNum = random.nextInt((3000 - 1000) + 1) + 1000;

                    // Fake process total time
                    Thread.sleep(randomNum);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return null;

            }

            @Override
            protected void onPostExecute(String result) {

                // Analytics Time Tracking Stop
                timeElapseUtils.trackStop(randomString);

            }

        }.execute();
    }
}