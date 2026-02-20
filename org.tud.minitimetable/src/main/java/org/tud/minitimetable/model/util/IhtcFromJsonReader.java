package org.tud.minitimetable.model.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.tud.minitimetable.model.IhtcModel;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

public class IhtcFromJsonReader {

	private final static String JSON_FILE_ENDING = "json";

	private final Path modelFolder;
	private Path modelFile;
	private IhtcModel model;

	public IhtcFromJsonReader(Path modelFolder) {
		this.modelFolder = Objects.requireNonNull(modelFolder);
	}

	public Path getModelPath() {
		return modelFile;
	}

	public IhtcModel getModel() {
		return model;
	}

	public IhtcModel readModel(String modelName) throws IOException {
		if (modelName.endsWith(JSON_FILE_ENDING)) {
			modelFile = modelFolder.resolve(modelName);
		} else {
			modelFile = modelFolder.resolve(modelName + JSON_FILE_ENDING);
		}

		var data = Files.readAllBytes(modelFile);
		JSONObject ob = (JSONObject) JSON.parse(data);
		model = null;// IhtcModel.fromJson(ob);
		return model;
	}

}
