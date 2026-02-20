package org.tud.minitimetable.model.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.tud.minitimetable.model.IhtcModel;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

public class InputModelReader {

	private static final String FILE_EXTENSION_JSON = ".json";

	public IhtcModel read(Path inputFile) throws IOException {
		if (inputFile.getFileName().toString().toLowerCase().endsWith(FILE_EXTENSION_JSON)) {
			Json2IhtcModelParser modelReader = new Json2IhtcModelParser();

			var data = Files.readAllBytes(inputFile);
			var jObj = (JSONObject) JSON.parse(data);
			IhtcModel model = modelReader.read(jObj);

			return model;
		}

		throw new IllegalArgumentException("No loader for filetype available: '" + inputFile + "'");
	}

}