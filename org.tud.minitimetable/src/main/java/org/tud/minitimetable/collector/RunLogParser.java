package org.tud.minitimetable.collector;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Pattern;

import org.tud.minitimetable.collector.RunLogParser.RunData;
import org.tud.minitimetable.util.Parser;

public class RunLogParser extends Parser<RunData> {

	public static record RunData(String modelVersion, String modelName, String dataName) {
	}

	private RunData result;

	public RunLogParser(BufferedReader reader) {
		super(reader);
	}

	@Override
	protected boolean parseContent() throws IOException {
		Pattern modelFilePattern = Pattern.compile("^Model File: .*[\\/\\\\](?<version>.*)[\\/\\\\](?<name>.*)\\.mzn$");
		Pattern dataFilePattern = Pattern.compile("^Data File: .*[\\/\\\\](?<name>.*)\\.json$");

		String modelVersion = null;
		String modelFile = null;
		String dataName = null;

		while (getCurrentLine() != null) {
			if (modelFile == null) {
				var matcher = modelFilePattern.matcher(getCurrentLine());
				if (matcher.find()) {
					// we only care for the part after model - this makes the path very specific!
					modelVersion = matcher.group("version");
					modelFile = matcher.group("name");
				}
			} else if (dataName == null) {
				var matcher = dataFilePattern.matcher(getCurrentLine());
				if (matcher.find()) {
					dataName = matcher.group("name");
				}
			} else {
				break;
			}

			readNextLine();
		}

		if (modelVersion != null && modelFile != null && dataName != null) {
			result = new RunData(modelVersion, modelFile, dataName);
		}

		return false;
	}

	@Override
	public RunData getParseResult() {
		return result;
	}

}