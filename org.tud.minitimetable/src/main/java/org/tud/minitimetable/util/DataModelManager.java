package org.tud.minitimetable.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.tud.minitimetable.model.IhtcModel;
import org.tud.minitimetable.model.util.InputModelReader;
import org.tud.minitimetable.model.util.OutputModelWriter;

public class DataModelManager {

	private final Path _generalInputFolder;
	private final Path _generalOutputFolder;

	private Path _inputModelPath;
	private Path _outputModelPath;

	public DataModelManager(Path generalInputFolder, Path generalOutputFolder) {
		_generalInputFolder = Objects.requireNonNull(generalInputFolder, "generalInputFolder");
		_generalOutputFolder = Objects.requireNonNull(generalOutputFolder, "generalOutputFolder");
	}

	public IhtcModel loadDataModel(String fileName) throws IOException {
		return loadModel(Path.of(fileName));
	}

	public IhtcModel loadModel(Path filePath) throws IOException {
		if (filePath.isAbsolute())
			throw new IllegalArgumentException();

		_inputModelPath = null;
		_outputModelPath = null;

		String fileName = filePath.getFileName().toString();
		if (fileName.lastIndexOf('.') < 0) {
			filePath = filePath.resolveSibling(fileName + ".json");
		}

		var fullInputPath = _generalInputFolder.resolve(filePath);
		if (!Files.exists(fullInputPath))
			throw new FileNotFoundException("File '" + fullInputPath + "' not found.");

		_inputModelPath = fullInputPath.normalize();

		InputModelReader reader = new InputModelReader();
		var model = reader.read(_inputModelPath);

		return model;
	}

	public Path writeDataModelAsDZN(IhtcModel model) throws IOException {
		var fullOutputPath = _generalOutputFolder.resolve(_generalInputFolder.relativize(_inputModelPath));
		_outputModelPath = PathUtils.changeFileExtension(fullOutputPath, ".dzn");

		if (!Files.isDirectory(_outputModelPath.getParent())) {
			Files.createDirectories(_outputModelPath.getParent());
		}

		OutputModelWriter writer = new OutputModelWriter();
		writer.write(model, _outputModelPath);

		return _outputModelPath;
	}

	public Path getPathOfDataModel() {
		return _inputModelPath;
	}

	public Path getPathOfWrite() {
		return _outputModelPath;
	}

}
