package org.tud.minitimetable;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@Deprecated
public class MiniZincJsonOutputParser implements Closeable {
	private InputStream input;

	public MiniZincJsonOutputParser(InputStream mzOutput) {
		input = Objects.requireNonNull(mzOutput);
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}
}