package org.tud.minitimetable.util;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

public interface Writer extends Closeable, Flushable {

	@FunctionalInterface
	public static interface WriteFunction<T> {
		void write(T value, boolean hasMore) throws IOException;
	}

	Writer setIndentation(int levels);

	int getIndentation();

	Writer increaseIndentation();

	Writer decreaseIndentation();

	Writer increaaseIndentation(int by);

	Writer decreaaseIndentation(int by);

	Writer indentation() throws IOException;

	Writer indentation(int levels) throws IOException;

	Writer write(String value) throws IOException;

	Writer write(String value, boolean doWrite) throws IOException;

	Writer write(int value) throws IOException;

	Writer write(float value) throws IOException;

	Writer write(boolean value) throws IOException;

	<T> Writer write(T[] values, WriteFunction<T> each) throws IOException;

	Writer write(int[] values, WriteFunction<Integer> each) throws IOException;

	Writer newLine() throws IOException;

}
