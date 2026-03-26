package org.tud.minitimetable.extern.solver;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public final class CodeLogger {

	public static interface Logger {
		void log(String msg);

		void logEmptyLine();

		void flush();
	}

	public static final class NullCodeLogger implements Logger {
		@Override
		public void log(String msg) {

		}

		@Override
		public void logEmptyLine() {

		}

		@Override
		public void flush() {

		}
	}

	public static final class ConsolCodeLogger implements Logger {
		@Override
		public void log(String msg) {
			System.out.println(msg);
		}

		@Override
		public void logEmptyLine() {
			System.out.println();
		}

		@Override
		public void flush() {

		}
	}

	public static class FileCodeLogger implements Logger, Closeable {

		private BufferedWriter writer;
		private Path logFile;

		public FileCodeLogger(Path logFile) {
			this.logFile = Objects.requireNonNull(logFile);
		}

		private void initializeWriter() throws IOException {
			if (!Files.exists(logFile.getParent()))
				Files.createDirectories(logFile.getParent());

			writer = Files.newBufferedWriter(logFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
					StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
		}

		@Override
		public void log(String msg) {
			try {
				if (writer == null)
					initializeWriter();
				writer.write(msg);
				writer.newLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void logEmptyLine() {
			try {
				if (writer == null)
					initializeWriter();
				writer.newLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void flush() {
			try {
				if (writer != null)
					writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void close() throws IOException {
			flush();
			if (writer != null)
				writer.close();
		}

	}

	public static final class MixedCodeLogger extends FileCodeLogger {

		public MixedCodeLogger(Path logFile) {
			super(logFile);
		}

		@Override
		public void log(String msg) {
			System.out.println(msg);
			super.log(msg);
		}

		@Override
		public void logEmptyLine() {
			System.out.println();
			super.logEmptyLine();
		}

	}

}