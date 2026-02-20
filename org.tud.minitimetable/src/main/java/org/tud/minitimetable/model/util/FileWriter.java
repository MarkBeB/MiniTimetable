package org.tud.minitimetable.model.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Objects;

import org.tud.minitimetable.model.util.Writer.WriteFunction;

public class FileWriter implements Writer {

	private static final String NEW_LINE = "\n";
	private static final String TAB = "\t";

	private final BufferedWriter _writer;
	private int _indentation;

	public FileWriter(BufferedWriter writer) {
		this._writer = Objects.requireNonNull(writer);
	}

	@Override
	public Writer write(String value) throws IOException {
		_writer.write(value);
		return this;
	}

	@Override
	public Writer write(String value, boolean doWrite) throws IOException {
		if (doWrite) {
			_writer.write(value);
		}
		return this;
	}

	@Override
	public Writer write(int value) throws IOException {
		return write(Integer.toString(value));
	}

	@Override
	public Writer write(float value) throws IOException {
		return write(Float.toString(value));
	}

	@Override
	public Writer write(boolean value) throws IOException {
		return write(Boolean.toString(value));
	}

	@Override
	public void close() throws IOException {
		_writer.close();
	}

	@Override
	public void flush() throws IOException {
		_writer.flush();
	}

	@Override
	public Writer write(int[] values, WriteFunction<Integer> each) throws IOException {
		for (var i = 0; i < values.length; ++i) {
			each.write(values[i], i + 1 < values.length);
		}
		return this;
	}

	@Override
	public <T> Writer write(T[] values, WriteFunction<T> each) throws IOException {
		for (var i = 0; i < values.length; ++i) {
			each.write(values[i], i + 1 < values.length);
		}
		return this;
	}

	@Override
	public Writer newLine() throws IOException {
		_writer.write(NEW_LINE);
		return this;
	}

	@Override
	public Writer setIndentation(int levels) {
		if (levels < 0)
			throw new IllegalArgumentException("Value needs to be equal or greater than 0");
		_indentation = levels;
		return this;
	}

	@Override
	public int getIndentation() {
		return _indentation;
	}

	@Override
	public Writer increaseIndentation() {
		return this.increaaseIndentation(1);
	}

	@Override
	public Writer decreaseIndentation() {
		return this.decreaaseIndentation(1);
	}

	@Override
	public Writer increaaseIndentation(int by) {
		if (by < 0)
			throw new IllegalArgumentException("Value needs to be greater than 0");
		_indentation += by;
		return this;
	}

	@Override
	public Writer decreaaseIndentation(int by) {
		if (by < 0)
			throw new IllegalArgumentException("Value needs to be greater than 0");
		_indentation -= by;
		if (_indentation < 0) {
			_indentation = 0;
		}
		return this;
	}

	@Override
	public Writer indentation() throws IOException {
		return this.indentation(_indentation);
	}

	@Override
	public Writer indentation(int levels) throws IOException {
		if (levels <= 0)
			return this;

		for (var i = 0; i < levels; ++i) {
			this.write(TAB);
		}

		return this;
	}

}
