package org.tud.minitimetable.extern.solver;

import java.io.InputStream;
import java.nio.file.Path;

public class ProcessLogger {

	public static interface Log {

	}

	public static interface NullLog extends Log {

	}

	public static interface StreamLog extends Log {
		void setStream(InputStream inputStream);
	}

	public static interface FileLog extends Log {
		Path getPath();

		Path generatePath(Path outputFolder, String dataFileName, String defaultfileExtension);
	}

	public static class DefaultStreamLog implements StreamLog {
		private InputStream _stream;

		public InputStream getStream() {
			return _stream;
		}

		@Override
		public void setStream(InputStream inputStream) {
			_stream = inputStream;
		}
	}

	public static class DefaultFileLog implements FileLog {
		private String _nameSchema;
		private boolean _hasExtension;
		private Path _generatedPath;

		public DefaultFileLog(String nameSchema) {
			this(nameSchema, false);
		}

		public DefaultFileLog(String nameSchema, boolean includesFileExtension) {
			_nameSchema = nameSchema;
			_hasExtension = includesFileExtension;
		}

		@Override
		public Path getPath() {
			return _generatedPath;
		}

		@Override
		public Path generatePath(Path outputFolder, String dataFileName, String defaultfileExtension) {
			var newFileName = String.format(_nameSchema, dataFileName);

			if (!_hasExtension)
				newFileName = newFileName + "." + defaultfileExtension;

			_generatedPath = outputFolder.resolve(newFileName);
			return _generatedPath;
		}
	}

}
