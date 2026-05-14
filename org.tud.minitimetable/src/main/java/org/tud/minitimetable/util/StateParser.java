package org.tud.minitimetable.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class StateParser {

	public static interface Handler {

		public boolean parse(StateParser parser) throws Exception;

	}

	public static abstract class ConditionalHandler implements Handler {

		@Override
		public boolean parse(StateParser parser) throws Exception {
			if (isMatch(parser)) {
				doParse(parser);
				return true;
			}

			return false;
		}

		abstract protected boolean isMatch(StateParser parser);

		abstract protected void doParse(StateParser parser);

	}

	public static class ConditionalHandler2 extends ConditionalHandler {

		private final Function<StateParser, Boolean> matcher;
		private final Consumer<StateParser> parser;

		public ConditionalHandler2(Function<StateParser, Boolean> matcher, Consumer<StateParser> parser) {
			this.matcher = Objects.requireNonNull(matcher);
			this.parser = Objects.requireNonNull(parser);
		}

		@Override
		protected boolean isMatch(StateParser parser) {
			return this.matcher.apply(parser);
		}

		@Override
		protected void doParse(StateParser parser) {
			this.parser.accept(parser);
		}

	}

	private List<Handler> activeHandler = new ArrayList<>();

	private BufferedReader reader;
	private String currentLine;

	public StateParser(BufferedReader reader) {
		this.reader = Objects.requireNonNull(reader);
	}

	public String readNextLine() throws IOException {
		currentLine = reader.readLine();
		return currentLine;
	}

	public String getCurrentLine() {
		return currentLine;
	}

	public void addHandler(Handler handler) {
		this.activeHandler.add(Objects.requireNonNull(handler));
	}

	public void removeHandler(Handler handler) {
		this.activeHandler.remove(handler);
	}

	public void removeAllHandler() {
		this.activeHandler.clear();
	}

	public void parse() throws Exception {
		readNextLine();
		while (getCurrentLine() != null) {
			boolean isParsed = false;
			var defensiveCopy = activeHandler.stream().toList();
			for (var state : defensiveCopy) {
				isParsed = state.parse(this);
				if (isParsed)
					break;
			}

			if (!isParsed)
				readNextLine();
		}
	}

}
