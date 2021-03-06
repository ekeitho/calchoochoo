package com.eleith.calchoochoo;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.eleith.calchoochoo.dagger.ChooChooWidgetConfigureComponent;
import com.eleith.calchoochoo.dagger.ChooChooWidgetConfigureModule;
import com.eleith.calchoochoo.data.ChooChooDatabase;
import com.eleith.calchoochoo.data.Stop;
import com.eleith.calchoochoo.utils.StopUtils;
import com.eleith.calchoochoo.utils.TripUtils;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;

public class ChooChooWidgetConfigure extends AppCompatActivity {
  private ChooChooWidgetConfigureComponent chooChooWidgetConfigureComponent;
  private int appWidgetId;
  private static final String PREFS_NAME = "com.eleith.calchoochoo.ChooChooWidgetProvider";
  private static final String PREF_PREFIX_KEY = "choochoo_widget_";
  private static final String PREF_PREFIX_STOP = PREF_PREFIX_KEY + "stop_";
  private static final String PREF_PREFIX_DIRECTION = PREF_PREFIX_KEY + "direction_";

  @BindView(R.id.search_results_empty_state)
  TextView searchResultsEmptyState;
  @BindView(R.id.search_results_recyclerview)
  RecyclerView searchResultsRecyclerView;

  @Inject
  ChooChooRouterManager chooChooRouterManager;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    setResult(RESULT_CANCELED);
    Intent intent = getIntent();
    Bundle extras = intent.getExtras();
    if (extras != null) {
      appWidgetId = extras.getInt(
          AppWidgetManager.EXTRA_APPWIDGET_ID,
          AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
      super.onCreate(savedInstanceState);
      finish();
    } else {
      chooChooWidgetConfigureComponent = ChooChooApplication.from(this).getAppComponent().activityComponent(new ChooChooWidgetConfigureModule(this));
      chooChooWidgetConfigureComponent.inject(this);

      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_linear_top_bottom);

      chooChooRouterManager.loadSearchWidgetConfigureFragment();
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  protected void onRestart() {
    super.onRestart();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
  }

  private void notifyWidget() {
    ChooChooWidgetProvider.updateOneWidget(this, appWidgetId, AppWidgetManager.getInstance(this));

    Intent resultValue = new Intent();
    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
    setResult(RESULT_OK, resultValue);
    finish();
  }

  public void chooseStopToConfigure(Stop stop) {
    saveWidgetConfiguration(stop.stop_id, TripUtils.DIRECTION_NORTH);
    notifyWidget();
  }

  private void saveWidgetConfiguration(String stopId, int direction) {
    SharedPreferences.Editor prefs = this.getSharedPreferences(PREFS_NAME, 0).edit();
    prefs.putString(PREF_PREFIX_STOP + appWidgetId, stopId);
    prefs.putInt(PREF_PREFIX_DIRECTION + appWidgetId, direction);
    prefs.apply();
  }

  static void updateWidgetConfiguration(Context context, int appWidgetId, String stopId, int direction) {
    SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
    prefs.putString(PREF_PREFIX_STOP + appWidgetId, stopId);
    prefs.putInt(PREF_PREFIX_DIRECTION + appWidgetId, direction);
    prefs.apply();
  }

  static Stop getStopFromPreferences(Context context, int appWidgetId) {
    SQLiteDatabase db = new ChooChooDatabase(context).getReadableDatabase();

    Cursor cursor = db.query("stops", null, "stop_code = '' AND platform_code = ''", null, null, null, null);
    ArrayList<Stop> stops = StopUtils.getStopsFromCursor(cursor);
    cursor.close();

    SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
    String stopId = prefs.getString(PREF_PREFIX_STOP + appWidgetId, null);
    return StopUtils.getStopById(stops, stopId);
  }

  static int getDirectionFromPreferences(Context context, int appWidgetId) {
    SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
    return prefs.getInt(PREF_PREFIX_DIRECTION + appWidgetId, TripUtils.DIRECTION_NORTH);
  }

  public ChooChooWidgetConfigureComponent getComponent() {
    return chooChooWidgetConfigureComponent;
  }
}
