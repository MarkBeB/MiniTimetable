package org.tud.minitimetable.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.tud.minitimetable.model.IhtcModel;

public class DataModelManager {

	private final Path _generalInputFolder;
	private final Path _generalOutputFolder;

	private Path _inputModelPath;
	private Path _outputModelPath;
	private IhtcModel _model;

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
		_model = reader.read(_inputModelPath);

		return _model;
	}

	public Path writeDataModelAsDZN(IhtcModel model) throws IOException {
		var fullOutputPath = _generalOutputFolder.resolve(_generalInputFolder.relativize(_inputModelPath));
		_outputModelPath = changeFileExtension(fullOutputPath, ".dzn");

		if (!Files.isDirectory(_outputModelPath.getParent())) {
			Files.createDirectories(_outputModelPath.getParent());
		}

		OutputModelWriter writer = new OutputModelWriter();
		writer.write(model, _outputModelPath);

		return _outputModelPath;
	}

	public Path writeDataModelAsDZN() throws IOException {
		return writeDataModelAsDZN(_model);
	}

	public Path getPathOfDataModel() {
		return _inputModelPath;
	}

	public Path getPathOfOutput() {
		return _outputModelPath;
	}

	public IhtcModel getDataModel() {
		return _model;
	}

	private static Path changeFileExtension(Path filePath, String newExtension) {
		String fileName = filePath.getFileName().toString();
		int delimiterPosition = fileName.lastIndexOf('.');

		int fileNameLength = delimiterPosition < 0 ? fileName.length() : delimiterPosition;
		String newFileName = fileName.substring(0, fileNameLength)
				+ (newExtension.startsWith(".") ? newExtension : "." + newExtension);

		return filePath.resolveSibling(newFileName);
	}

}
