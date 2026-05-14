package org.tud.minitimetable.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Objects;

public abstract class Parser<T> {

	protected BufferedReader reader;
	protected String currentLine;

	public Parser(BufferedReader reader) {
		this.reader = Objects.requireNonNull(reader, "reader");
	}

	private void readFirstLine() throws IOException {
		readNextLine();
	}

	protected String readNextLine() throws IOException {
		currentLine = reader.readLine();
		return currentLine;
	}

	protected String getCurrentLine() {
		return currentLine;
	}

	protected boolean currentLineNotNull() {
		return currentLine != null;
	}

	public T parse() throws IOException {
		readFirstLine();
		boolean cont = true;
		while (cont && currentLineNotNull()) {
			cont = parseContent();
		}

		return getParseResult();
	}

	protected abstract boolean parseContent() throws IOException;

	public abstract T getParseResult();
}
