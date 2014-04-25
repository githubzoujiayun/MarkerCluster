package com.amap.marker;

import com.amap.api.maps.model.MarkerOptions;

public class MarkerOptionsExtern {
	MarkerOptions option;
	Object object;

	public MarkerOptionsExtern(MarkerOptions option, Object object) {
		super();
		this.option = option;
		this.object = object;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public MarkerOptions getOption() {
		return option;
	}

}
