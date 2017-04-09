package com.eleith.calchoochoo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.eleith.calchoochoo.dagger.ChooChooComponent;
import com.eleith.calchoochoo.dagger.ChooChooModule;
import com.eleith.calchoochoo.data.ChooChooLoader;
import com.eleith.calchoochoo.data.PossibleTrip;
import com.eleith.calchoochoo.utils.BundleKeys;
import com.eleith.calchoochoo.utils.IntentKeys;
import com.eleith.calchoochoo.utils.RxBus;
import com.eleith.calchoochoo.utils.RxBusMessage.RxMessage;
import com.eleith.calchoochoo.utils.RxBusMessage.RxMessageKeys;
import com.eleith.calchoochoo.utils.RxBusMessage.RxMessagePossibleTrips;
import com.google.android.gms.common.api.GoogleApiClient;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.functions.Action1;

public class TripFilterActivity extends AppCompatActivity {
  private ChooChooComponent chooChooComponent;
  private Subscription subscription;
  private ArrayList<PossibleTrip> possibleTrips;
  private String stopSourceId;
  private String stopDestinationId;
  private Integer stopMethod;
  private Long stopDateTime;

  @Inject
  RxBus rxBus;
  @Inject
  GoogleApiClient googleApiClient;
  @Inject
  ChooChooRouterManager chooChooRouterManager;
  @Inject
  ChooChooLoader chooChooLoader;

  @BindView(R.id.activityFloatingActionButton)
  FloatingActionButton floatingActionButton;

  @OnClick(R.id.activityFloatingActionButton)
  void onFabClicked() {
    rxBus.send(new RxMessage(RxMessageKeys.FAB_CLICKED));
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    chooChooComponent = ChooChooApplication.from(this).getAppComponent().activityComponent(new ChooChooModule(this));
    chooChooComponent.inject(this);

    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_appbar_main_with_fab);
    ButterKnife.bind(this);

    floatingActionButton.setImageDrawable(getDrawable(R.drawable.ic_swap_vert_black_24dp));
    Intent intent = getIntent();
    if (intent != null) {
      Bundle bundle = intent.getExtras();
      if (bundle != null) {
        stopSourceId = bundle.getString(BundleKeys.STOP_SOURCE);
        stopDestinationId = bundle.getString(BundleKeys.STOP_DESTINATION);
        stopMethod = bundle.getInt(BundleKeys.STOP_METHOD);
        stopDateTime = bundle.getLong(BundleKeys.STOP_DATETIME, new LocalDateTime().toDateTime().getMillis());

        if (stopDestinationId != null && stopSourceId != null) {
          subscription = rxBus.observeEvents(RxMessagePossibleTrips.class).take(1).subscribe(handleRxMessages());
          chooChooLoader.loadPossibleTrips(stopSourceId, stopDestinationId, new LocalDateTime(stopDateTime));
        } else {
          chooChooRouterManager.loadTripFilterFragment(null, stopMethod, new LocalDateTime(stopDateTime), stopSourceId, stopDestinationId);
        }
      }
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    googleApiClient.connect();
  }

  @Override
  protected void onStop() {
    super.onStop();
    googleApiClient.disconnect();
    fabShow();
    subscription.unsubscribe();
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    googleApiClient.reconnect();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == IntentKeys.STOP_SEARCH_RESULT) {
      if (data != null) {
        Bundle bundle = data.getExtras();
        String stopId = bundle.getString(BundleKeys.STOP);
        Integer reason = bundle.getInt(BundleKeys.SEARCH_REASON);
        if (resultCode == RESULT_OK) {
          if (reason == 1) {
            stopDestinationId = stopId;
          } else {
            stopSourceId = stopId;
          }
          subscription = rxBus.observeEvents(RxMessagePossibleTrips.class).take(1).subscribe(handleRxMessages());
          chooChooLoader.loadPossibleTrips(stopSourceId, stopDestinationId, new LocalDateTime(stopDateTime));
        }
      }
    }
  }

  public ChooChooComponent getComponent() {
    return chooChooComponent;
  }

  private Action1<RxMessagePossibleTrips> handleRxMessages() {
    return new Action1<RxMessagePossibleTrips>() {
      @Override
      public void call(RxMessagePossibleTrips rxMessage) {
        possibleTrips = rxMessage.getMessage();
        chooChooRouterManager.loadTripFilterFragment(possibleTrips, stopMethod, new LocalDateTime(stopDateTime), stopSourceId, stopDestinationId);
      }
    };
  }

  public void fabHide() {
    floatingActionButton.setVisibility(View.GONE);
  }

  public void fabShow() {
    floatingActionButton.setVisibility(View.VISIBLE);
  }
}