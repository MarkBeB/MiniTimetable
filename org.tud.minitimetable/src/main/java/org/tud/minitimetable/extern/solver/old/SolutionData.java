package org.tud.minitimetable.extern.solver.old;

import java.util.Objects;

import com.alibaba.fastjson2.JSONObject;

public class SolutionData {
	public final JSONObject _json;

	public SolutionData(JSONObject json) {
		_json = Objects.requireNonNull(json, "json");
	}

	public int getScore() {
		return _json.getIntValue("score", 0);
	}
}