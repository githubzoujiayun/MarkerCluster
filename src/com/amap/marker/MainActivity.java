package com.amap.marker;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.demo.markercluster.R;

@SuppressLint("HandlerLeak")
public class MainActivity extends Activity implements OnCameraChangeListener,
		OnMarkerClickListener {
	private AMap aMap;
	private MapView mapView;
	private ArrayList<MarkerOptionsExtern> markerOptionsList = new ArrayList<MarkerOptionsExtern>();// 所有的marker
	MarkerClusterUtils utils;
	Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			if (msg.what == 0) {
				resetMarks();// 更新markers
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);
		init();
		utils=new MarkerClusterUtils(this, aMap,markerOptionsList);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	/**
	 * 初始化AMap对象
	 */
	private void init() {
		if (aMap == null) {
			aMap = mapView.getMap();
			setUpMap();
		}
	}

	private void setUpMap() {
		aMap.setOnMarkerClickListener(this);
		aMap.setOnCameraChangeListener(this);
		addMarkers();// 聚合测试点添加
	}

	/**
	 * 模拟添加多个marker
	 */
	private void addMarkers() {
		for (int i = 0; i < 500; i++) {
			LatLng latLng = new LatLng(Math.random() * 6 + 39,
					Math.random() * 6 + 116);
			MarkerOptions icon = new MarkerOptions()
					.position(latLng)
					.title("Marker" + i)
					.icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
			markerOptionsList.add(new MarkerOptionsExtern(icon, null));
		}
	}

	@Override
	public void onCameraChange(CameraPosition arg0) {
		System.out.println("onCameraChange:"+arg0.zoom);
	}

	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		System.out.println("onCameraChange:"+arg0.zoom);
		handler.sendEmptyMessage(0);// 更新界面marker
	}
	
	/**
	 * 获取视野内的marker 根据聚合算法合成自定义的marker 显示视野内的marker
	 */
	public void resetMarks() {
		utils.resetMarks();
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		Toast.makeText(this, "点击marker" + marker.getTitle(), 3).show();
		return false;
	}
}
