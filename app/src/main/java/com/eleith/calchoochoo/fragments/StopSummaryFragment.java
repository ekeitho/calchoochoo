package com.eleith.calchoochoo.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eleith.calchoochoo.R;
import com.eleith.calchoochoo.StopActivity;
import com.eleith.calchoochoo.data.Stop;
import com.eleith.calchoochoo.utils.BundleKeys;
import com.eleith.calchoochoo.utils.ColorUtils;
import com.eleith.calchoochoo.utils.DataStringUtils;
import com.eleith.calchoochoo.utils.RxBus;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.parceler.Parcels;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StopSummaryFragment extends Fragment {
  private Stop stop;
  private StopActivity stopActivity;

  @BindView(R.id.stop_summary_name)
  TextView stopName;
  @BindView(R.id.stop_summary_zone)
  TextView stopZone;
  @BindView(R.id.stop_summary_datetime)
  TextView stopDateTime;

  @Inject
  RxBus rxBus;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    stopActivity = (StopActivity) getActivity();
    stopActivity.getComponent().inject(this);
    unWrapBundle(savedInstanceState != null ? savedInstanceState : getArguments());
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_stop_summary, container, false);
    ButterKnife.bind(this, view);

    unWrapBundle(savedInstanceState);
    return view;
  }

  @Override
  public void onStart() {
    super.onStart();
    stopName.setText(DataStringUtils.removeCaltrain(stop.stop_name));
    stopZone.setText(String.format(Locale.getDefault(), "%d", stop.zone_id + 1));
    stopDateTime.setText(DateTimeFormat.forPattern("E, MMM d").print(new LocalDateTime()));
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    outState.putParcelable(BundleKeys.STOP, Parcels.wrap(stop));
    super.onSaveInstanceState(outState);
  }

  private void unWrapBundle(Bundle savedInstanceState) {
    if (savedInstanceState != null) {
      stop = Parcels.unwrap(savedInstanceState.getParcelable(BundleKeys.STOP));
    }
  }

  @OnClick(R.id.stop_summary_link)
  public void goToLink() {
    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
    CustomTabsIntent customTabsIntent = builder.build();
    builder.setToolbarColor(ColorUtils.getThemeColor(getActivity(), R.attr.colorPrimary));
    customTabsIntent.launchUrl(getActivity(), Uri.parse(stop.stop_url));
  }
}
