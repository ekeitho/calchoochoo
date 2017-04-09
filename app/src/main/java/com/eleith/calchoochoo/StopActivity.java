package com.eleith.calchoochoo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.eleith.calchoochoo.dagger.ChooChooComponent;
import com.eleith.calchoochoo.dagger.ChooChooModule;
import com.eleith.calchoochoo.data.ChooChooLoader;
import com.eleith.calchoochoo.data.PossibleTrain;
import com.eleith.calchoochoo.data.Stop;
import com.eleith.calchoochoo.utils.BundleKeys;
import com.eleith.calchoochoo.utils.RxBus;
import com.eleith.calchoochoo.utils.RxBusMessage.RxMessage;
import com.eleith.calchoochoo.utils.RxBusMessage.RxMessageKeys;
import com.eleith.calchoochoo.utils.RxBusMessage.RxMessageNextTrains;
import com.eleith.calchoochoo.utils.RxBusMessage.RxMessageStop;
import com.eleith.calchoochoo.utils.TripUtils;
import com.google.android.gms.common.api.GoogleApiClient;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.functions.Action1;

public class StopActivity extends AppCompatActivity {
  private ChooChooComponent chooChooComponent;
  private Subscription subscription;
  private ArrayList<PossibleTrain> possibleTrains;
  private Stop stop;
  private int direction;

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

    subscription = rxBus.observeEvents(RxMessage.class).subscribe(new HandleRxMessages());
    floatingActionButton.setImageDrawable(getDrawable(R.drawable.ic_link_black_24dp));
    Intent intent = getIntent();
    if (intent != null) {
      Bundle bundle = intent.getExtras();
      if (bundle != null) {
        String stopId = bundle.getString(BundleKeys.STOP);
        direction = bundle.getInt(BundleKeys.DIRECTION);
        chooChooLoader.loadPossibleTrains(stopId, new LocalDateTime());
        chooChooLoader.loadStopByParentId(stopId);
      }
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    googleApiClient.connect();
    if (subscription.isUnsubscribed()) {
      subscription = rxBus.observeEvents(RxMessage.class).subscribe(new HandleRxMessages());
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    googleApiClient.disconnect();
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
    subscription.unsubscribe();
  }

  public ChooChooComponent getComponent() {
    return chooChooComponent;
  }

  private void loadFragment() {
    if (possibleTrains != null && stop != null) {
      ArrayList<PossibleTrain> filteredTrains = new ArrayList<>();
      for (PossibleTrain possibleTrain : possibleTrains) {
        if (possibleTrain.getTripDirectionId() == direction) {
          filteredTrains.add(possibleTrain);
        }
      }
      chooChooRouterManager.loadStopsFragments(stop, filteredTrains, direction);
    }
  }

  private class HandleRxMessages implements Action1<RxMessage> {
    @Override
    public void call(RxMessage rxMessage) {
      if (rxMessage.isMessageValidFor(RxMessageKeys.LOADED_NEXT_TRAINS)) {
        possibleTrains = ((RxMessageNextTrains) rxMessage).getMessage();
        loadFragment();
      } else if (rxMessage.isMessageValidFor(RxMessageKeys.LOADED_STOP)) {
        stop = ((RxMessageStop) rxMessage).getMessage();
        loadFragment();
      } else if (rxMessage.isMessageValidFor(RxMessageKeys.SWITCH_SOURCE_DESTINATION_SELECTED)) {
        direction = direction == TripUtils.DIRECTION_NORTH ? TripUtils.DIRECTION_SOUTH: TripUtils.DIRECTION_NORTH;
        loadFragment();
      }
    }
  }
}