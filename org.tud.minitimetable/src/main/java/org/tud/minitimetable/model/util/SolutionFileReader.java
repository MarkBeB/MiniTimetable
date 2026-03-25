package org.tud.minitimetable.model.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;

public class SolutionFileReader {

	public static record Solution(int number, int score, long time, JSONObject json) {
	}

	public List<Solution> parseSolutionFile(Path file) throws IOException {
		List<Solution> solutions = new LinkedList<>();

		try (var bufferedReader = Files.newBufferedReader(file)) {
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				var json = (JSONObject) JSON.parse(line);
				String parseType = json.getString("type");
				if ("solution".equals(parseType)) {
					JSONObject solutionOutput = json.getJSONObject("output");
					String content = solutionOutput.containsKey("json") ? solutionOutput.getString("json") : null;
					if (content != null) {
						var result = JSON.parseObject(content);
						solutions.add(new Solution(solutions.size() + 1, result.getInteger("score"),
								json.getLongValue("time"), result));
					}
				}
			}
		}

		return solutions;
	}

	public String constructNameForSolution(Solution solution) {
		return String.format("solution#%d_%d.json", solution.number, solution.score);
	}

	public void writeToFile(Solution solution, Path file) throws IOException {
		Files.createDirectories(file.getParent());
		try (var out = Files.newOutputStream(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
				StandardOpenOption.TRUNCATE_EXISTING)) {
			JSON.writeTo(out, solution.json, JSONWriter.Feature.PrettyFormat);
		}
	}

}
