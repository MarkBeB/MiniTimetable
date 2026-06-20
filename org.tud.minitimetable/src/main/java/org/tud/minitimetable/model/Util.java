package org.tud.minitimetable.model;

public final class Util {
	private Util() {

	}

	public static int[][] transformIntTo2D(int[] array, int rows, int cols) {
		if (array.length != rows * cols)
			throw new IllegalArgumentException(String.format(
					"Array length [%d] does not match number of rows and columns %d", array.length, rows * cols));

		int[][] result = new int[rows][cols];

		int head = 0;
		for (var i = 0; i < rows; ++i)
			for (var j = 0; j < cols; ++j)
				result[i][j] = array[head++];

		return result;
	}
}
