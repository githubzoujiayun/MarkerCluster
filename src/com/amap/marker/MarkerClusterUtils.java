package com.amap.marker;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Point;
import android.os.Handler;
import android.util.DisplayMetrics;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.LatLngBounds.Builder;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

public class MarkerClusterUtils {
	private int gridSize = 30;
	private int height;// 屏幕高度(px)
	private int width;// 屏幕宽度(px)
	Activity activity;
	AMap aMap;
	/**
	 * 所有的marker
	 */
	ArrayList<MarkerOptionsExtern> markerOptionsList;
	/**
	 * 所有可见的marker
	 */
	ArrayList<MarkerOptions> markerOptionsListInView = new ArrayList<MarkerOptions>();
	Handler handler;

	public MarkerClusterUtils(Activity activity, AMap aMap,
			ArrayList<MarkerOptionsExtern> markerOptionsList) {
		super();
		this.activity = activity;
		this.aMap = aMap;
		handler = new Handler();
		this.markerOptionsList = markerOptionsList;
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		height = dm.heightPixels;
	}

	/**
	 * 获取视野内的marker 根据聚合算法合成自定义的marker 显示视野内的marker
	 * 
	 */
	public void resetMarks() {
		// 开始刷新界面
		Projection projection = aMap.getProjection();
		markerOptionsListInView.clear();
		// 获取在当前视野内的marker;提高效率
		for (MarkerOptionsExtern mp : markerOptionsList) {
			Point p = projection.toScreenLocation(mp.getOption().getPosition());
			if (p.x < 0 || p.y < 0 || p.x > width || p.y > height) {
				// 不添加到计算的列表中
			} else {
				markerOptionsListInView.add(mp.getOption());
			}
		}
		// 自定义的聚合类MarkerCluster
		ArrayList<MarkerCluster> clustersMarker = new ArrayList<MarkerCluster>();
		for (MarkerOptions mp : markerOptionsListInView) {

			if (clustersMarker.size() == 0) {
				clustersMarker.add(new MarkerCluster(activity, mp, projection,
						gridSize));// 100根据自己需求调整
			} else {
				boolean isIn = false;
				for (MarkerCluster cluster : clustersMarker) {
					if (cluster.getBounds().contains(mp.getPosition())) {
						cluster.addMarker(mp);
						isIn = true;
						break;
					}
				}
				if (!isIn) {
					clustersMarker.add(new MarkerCluster(activity, mp,
							projection, gridSize));
				}
			}
		}
		// 先清除地图上所有覆盖物
		aMap.clear();
		for (MarkerCluster markerCluster : clustersMarker) {
			markerCluster.setpositionAndIcon();// 设置聚合点的位置和icon
			Marker marker = aMap.addMarker(markerCluster.getOptions());// 重新添加
			marker.setObject(markerCluster);
			/*if (!marker.getTitle().equals("聚合点")) {
				marker.showInfoWindow();
			}*/
		}
		// moveMutltipleCarsCenter();
	}

	public void moveMutltipleCarsCenter() {
		Builder builder = LatLngBounds.builder();
		for (MarkerOptionsExtern option : markerOptionsList) {
			builder.include(option.getOption().getPosition());
		}
		LatLngBounds bound = null;
		try {
			bound = builder.build();
			if (bound == null) {
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bound, 5));
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				LatLngBounds bounds = aMap.getProjection().getVisibleRegion().latLngBounds;
				if (checkMultipleMarkersBound(bounds)) {
					LatLng tarLatLng = aMap.getCameraPosition().target;
					float tarZoom = aMap.getCameraPosition().zoom - 1;
					aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
							tarLatLng, tarZoom));
				}
			}
		}, 200);
	}

	private boolean checkMultipleMarkersBound(LatLngBounds bounds) {
		for (MarkerOptionsExtern option : markerOptionsList) {
			if (!bounds.contains(option.getOption().getPosition())) {
				return true;
			}
		}
		return false;

	}
}
