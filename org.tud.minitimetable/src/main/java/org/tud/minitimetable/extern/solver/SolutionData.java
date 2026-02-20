package org.tud.minitimetable.extern.solver;

import java.util.Objects;

import com.alibaba.fastjson2.JSONObject;

public class SolutionData {
	public final JSONObject json;

	public SolutionData(JSONObject json) {
		this.json = Objects.requireNonNull(json, "json");
	}

	public int getScore() {
		return json.getIntValue("score", 0);
	}
}