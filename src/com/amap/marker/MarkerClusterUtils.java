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
	private int height;// ��Ļ�߶�(px)
	private int width;// ��Ļ���(px)
	Activity activity;
	AMap aMap;
	/**
	 * ���е�marker
	 */
	ArrayList<MarkerOptionsExtern> markerOptionsList;
	/**
	 * ���пɼ���marker
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
	 * ��ȡ��Ұ�ڵ�marker ���ݾۺ��㷨�ϳ��Զ����marker ��ʾ��Ұ�ڵ�marker
	 * 
	 */
	public void resetMarks() {
		// ��ʼˢ�½���
		Projection projection = aMap.getProjection();
		markerOptionsListInView.clear();
		// ��ȡ�ڵ�ǰ��Ұ�ڵ�marker;���Ч��
		for (MarkerOptionsExtern mp : markerOptionsList) {
			Point p = projection.toScreenLocation(mp.getOption().getPosition());
			if (p.x < 0 || p.y < 0 || p.x > width || p.y > height) {
				// ����ӵ�������б���
			} else {
				markerOptionsListInView.add(mp.getOption());
			}
		}
		// �Զ���ľۺ���MarkerCluster
		ArrayList<MarkerCluster> clustersMarker = new ArrayList<MarkerCluster>();
		for (MarkerOptions mp : markerOptionsListInView) {

			if (clustersMarker.size() == 0) {
				clustersMarker.add(new MarkerCluster(activity, mp, projection,
						gridSize));// 100�����Լ��������
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
		// �������ͼ�����и�����
		aMap.clear();
		for (MarkerCluster markerCluster : clustersMarker) {
			markerCluster.setpositionAndIcon();// ���þۺϵ��λ�ú�icon
			Marker marker = aMap.addMarker(markerCluster.getOptions());// �������
			marker.setObject(markerCluster);
			/*if (!marker.getTitle().equals("�ۺϵ�")) {
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
