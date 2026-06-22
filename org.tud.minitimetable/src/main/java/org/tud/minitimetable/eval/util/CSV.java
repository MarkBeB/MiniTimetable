package org.tud.minitimetable.eval.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CSV implements Iterable<CSV.CSVRecord> {

	public static interface CSVRecord {
		int getRowIndex();

		String[] getRow();

		String getCell(String name);

		String getCell(int colIndex);

		void setCell(String name, Object value);

		void setCell(int colIndex, Object value);
	}

	public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
	public static final String DEFAULT_DELIMITER = ";";

	private String[] columnHeader = new String[0];
	private final Map<String, Integer> headerToIndex = new HashMap<>();

	private int numberOfColumns = -1;
	protected final DecimalFormat numberFormat;
	protected final List<String[]> data = new ArrayList<>();

	public CSV(DecimalFormat formatter) {
		if (formatter == null)
			formatter = getDefaultDecimalFormatter();

		this.numberFormat = Objects.requireNonNull(formatter, "formatter");
	}

	public void read(Path file, boolean hasHeader) throws IOException {
		this.read(file, DEFAULT_CHARSET, DEFAULT_DELIMITER, hasHeader);
	}

	public void read(Path file, Charset charset, String delimiter, boolean hasHeader) throws IOException {
		Objects.requireNonNull(file, "file");
		Objects.requireNonNull(charset, "charset");
		Objects.requireNonNull(delimiter, "delimiter");

		List<String[]> newRecords = null;
		try (var reader = Files.newBufferedReader(file, charset)) {
			newRecords = reader.lines().map(line -> line.split(delimiter)).collect(Collectors.toList());
		}

		if (newRecords.size() == 0)
			return;

		int longestRecord = newRecords.stream().mapToInt(a -> a.length).max().getAsInt();
		for (int index = 0; index < newRecords.size(); ++index) {
			String[] record = newRecords.get(index);
			if (record.length != longestRecord)
				throw new IOException(String.format( //
						"Invalid number of elements. Row %s only contains %s elements, expected are %s elements", //
						index, record.length, longestRecord//
				));
		}

		if (hasHeader) {
			String[] headerNames = newRecords.get(0);
			newRecords.remove(0);
			readHeader(headerNames);
		}

		for (int index = 0; index < newRecords.size(); ++index) {
			String[] record = newRecords.get(index);
			readEntry(index, record);
		}
	}

	public void write(Path file) throws IOException {
		this.write(file, DEFAULT_CHARSET, DEFAULT_DELIMITER);
	}

	public void write(Path file, Charset charset, String delimiter) throws IOException {
		Files.createDirectories(file.getParent());

		try (var writer = Files.newBufferedWriter(file, charset, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
				StandardOpenOption.TRUNCATE_EXISTING)) {

			if (this.columnHeader.length > 0) {
				String columnNames = String.join(delimiter, this.columnHeader);
				writer.append(columnNames);
				writer.newLine();
			}

			StringBuilder builder = new StringBuilder();
			for (String[] record : this.data) {
				if (builder.length() > 0)
					writer.newLine();

				builder.setLength(0);

				if (record == null) {
					for (var i = 0; i < this.numberOfColumns; ++i) {
						builder.append("").append(delimiter);
					}
				} else {
					for (String column : record) {
						if (column == null) {
							builder.append("");
						} else {
							builder.append(column);
						}
						builder.append(delimiter);
					}
				}

				if (builder.length() > 0) {
					builder.delete(builder.length() - delimiter.length(), builder.length());
					writer.append(builder.toString());
				}
			}
		}
	}

	public void clearAll() {
		clearColumnNames();
		clearData();
	}

	public void setNumberOfColumns(int expectedColumns) {
		if (data.size() > 0) {
			if (expectedColumns != this.numberOfColumns) {
				throw new IllegalArgumentException( //
						String.format("Invalid number of columns. Expected %s, actual %s", //
								this.numberOfColumns, expectedColumns //
						));
			}
		}

		this.numberOfColumns = expectedColumns;
	}

	public void setColumnNames(String[] columnNames) {
		if (data.size() > 0) {
			if (numberOfColumns != columnNames.length) {
				throw new IllegalArgumentException( //
						String.format("Invalid number of columns. Expected %s, actual %s", //
								this.numberOfColumns, columnNames.length //
						));
			}
		}

		this.columnHeader = Arrays.copyOf(columnNames, columnNames.length);
		this.numberOfColumns = this.columnHeader.length;
		updateHeaderIndex();
	}

	public int getColIndexWithName(String headerName) {
		Integer column = headerToIndex.get(headerName);
		if (column == null)
			throw new IllegalArgumentException("Column with name '" + headerName + "' not found");
		return column.intValue();
	}

	public String getNameWithColIndex(int col) {
		if (col < 0 || columnHeader.length <= col)
			throw new IllegalArgumentException(
					String.format("Column index %s out of bound (#columns: %s)", col, columnHeader.length));
		return columnHeader[col];
	}

	public String[] getColumnNames() {
		return Arrays.copyOf(this.columnHeader, this.columnHeader.length);
	}

	public void clearColumnNames() {
		this.columnHeader = new String[0];
		updateHeaderIndex();
	}

	public boolean hasColumnWithName(String name) {
		return headerToIndex.containsKey(name);
	}

	public String getCellValue(int rowIndex, int colIndex) {
		if (colIndex < 0 || this.numberOfColumns <= colIndex)
			throw new IllegalArgumentException();

		if (rowIndex < 0)
			throw new IllegalArgumentException();

		if (rowIndex > data.size())
			return null;

		String[] row = data.get(rowIndex);
		if (row == null)
			return null;

		return row[colIndex];
	}

	public void setCellValue(int rowIndex, int colIndex, Object value) {
		if (colIndex < 0 || this.numberOfColumns <= colIndex)
			throw new IllegalArgumentException();

		if (rowIndex < 0)
			throw new IllegalArgumentException();

		if (rowIndex >= data.size()) {
			for (var i = data.size(); i <= rowIndex; ++i)
				data.add(null);
		}

		String[] row = data.get(rowIndex);
		if (row == null) {
			row = buildEntry();
			data.set(rowIndex, row);
		}

		row[colIndex] = convertValueForStorage(value);
	}

	public String getCellValue(int rowIndex, String colName) {
		var colIndex = getColIndexWithName(colName);
		return getCellValue(rowIndex, colIndex);
	}

	protected String[] getRow(int rowIndex) {
		if (rowIndex < 0)
			throw new IllegalArgumentException();

		if (rowIndex > data.size()) {
			for (var i = data.size(); i <= rowIndex; ++i)
				data.add(null);
		}

		String[] row = data.get(rowIndex);
		if (row == null) {
			row = buildEntry();
			data.set(rowIndex, row);
		}

		return row;
	}

	public int addNewRow() {
		var row = buildEntry();
		var index = data.size();
		data.add(row);
		return index;
	}

	public void clearRow(int rowIndex) {
		if (rowIndex < 0)
			throw new IllegalArgumentException();

		if (data.size() < rowIndex)
			return;

		data.remove(rowIndex);
	}

	public void clearCellValue(int rowIndex, int colIndex) {
		if (colIndex < 0 || this.numberOfColumns <= colIndex)
			throw new IllegalArgumentException();

		if (rowIndex < 0)
			throw new IllegalArgumentException();

		if (data.size() < rowIndex)
			return;

		String[] row = data.get(rowIndex);
		if (row == null)
			return;

		row[colIndex] = null;

		boolean allNull = true;
		for (var n : row)
			allNull &= n == null;

		if (allNull)
			data.remove(rowIndex);
	}

	public void setCellValue(int rowIndex, String colName, Object value) {
		var colIndex = getColIndexWithName(colName);
		setCellValue(rowIndex, colIndex, value);
	}

	public void clearData() {
		data.clear();
	}

	public long getNumberOfRows() {
		return data.size();
	}

	public int getNumberOfColumns() {
		return this.numberOfColumns;
	}

	public long getNextRowIndex() {
		return getNumberOfRows() + 1;
	}

	public Stream<? extends CSVRecord> stream() {
		return StreamSupport.stream(
				Spliterators.spliterator(iterator(), data.size(), Spliterator.ORDERED & Spliterator.SIZED), false);
	}

	@Override
	public Iterator<CSVRecord> iterator() {
		return buildIterator();
	}

	protected Iterator<CSVRecord> buildIterator() {
		return new Iterator<>() {

//			private final Iterator<String[]> iterator = data.iterator();
			private int currentRowIndex = 0;
			private final int maxRow = CSV.this.data.size();

			@Override
			public CSVRecord next() {

				return new CSVRecord() {
					private int rowIndex = currentRowIndex++;

					@Override
					public int getRowIndex() {
						return rowIndex;
					}

					@Override
					public String[] getRow() {
						return CSV.this.getRow(rowIndex);
					}

					@Override
					public String getCell(String name) {
						return CSV.this.getCellValue(rowIndex, name);
					}

					@Override
					public String getCell(int colIndex) {
						return CSV.this.getCellValue(rowIndex, colIndex);
					}

					@Override
					public void setCell(String name, Object value) {
						CSV.this.setCellValue(rowIndex, name, value);
					}

					@Override
					public void setCell(int colIndex, Object value) {
						CSV.this.setCellValue(rowIndex, colIndex, value);
					}

				};
			}

			@Override
			public boolean hasNext() {
				return currentRowIndex < maxRow;
			}
		};
	}

	protected String convertValueForStorage(Object value) {
		if (value instanceof Byte || value instanceof Integer || value instanceof Long) {
			return value.toString();
		} else if (value instanceof Float || value instanceof Double) {
			return numberFormat.format(value);
		} else if (value instanceof BigDecimal decimal) {
			return numberFormat.format(decimal);
		} else {
			if (value == null) {
				return null;
			}
			return value.toString();
		}
	}

	protected String getRowValue(String[] row, int colIndex) {
		if (row == null)
			return null;
		return row[colIndex];
	}

	protected void setRowValue(String[] row, int colIndex, Object value) {
		row[colIndex] = convertValueForStorage(value);
	}

	protected void readHeader(String[] header) throws IOException {
		if (this.columnHeader.length == 0) {
			setColumnNames(header);

		} else if (!Arrays.deepEquals(this.columnHeader, header)) {
			throw new IOException(String.format( //
					"Column Header do not match. Expected: '%s', actual: '%s'", //
					String.join(", ", this.columnHeader), String.join(", ", header) //
			));
		}
	}

	protected String[] buildEntry() {
		if (numberOfColumns <= 0)
			throw new IllegalStateException();
		return new String[numberOfColumns];
	}

	protected void readEntry(int rowIndex, String[] record) throws IOException {
		if (record.length != this.numberOfColumns) {
			throw new IOException(String.format( //
					"Invalid number of elements. Row %s contains %s elements, expected are %s elements", //
					rowIndex, record.length, this.numberOfColumns//
			));
		}
		this.data.add(record);
	}

	protected DecimalFormat getDefaultDecimalFormatter() {
		DecimalFormatSymbols decimalFormatSymbol = new DecimalFormatSymbols();
		decimalFormatSymbol.setDecimalSeparator('.');
		return new DecimalFormat("#0.000000#", decimalFormatSymbol);
	}

	private void updateHeaderIndex() {
		headerToIndex.clear();
		for (int i = 0; i < this.numberOfColumns; ++i) {
			if (headerToIndex.containsKey(this.columnHeader[i])) {
				throw new IllegalArgumentException("Column name '" + this.columnHeader[i] + "' already set");
			}
			headerToIndex.put(this.columnHeader[i], Integer.valueOf(i));
		}
	}

}
