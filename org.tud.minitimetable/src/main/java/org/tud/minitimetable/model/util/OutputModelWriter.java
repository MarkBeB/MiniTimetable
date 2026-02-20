package org.tud.minitimetable.model.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.tud.minitimetable.model.IhtcModel;

public class OutputModelWriter {

	public void write(IhtcModel model, Path outputFile) throws IOException {
		Ihtc2DznModelWriter modelWriter = new Ihtc2DznModelWriter();

		try (var buffer = Files.newBufferedWriter(outputFile, //
				StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE); //
				var writer = new FileWriter(buffer)) {
			modelWriter.write(model, writer);
		}
	}

}
