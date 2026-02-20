package org.tud.minitimetable.extern.solver;

public class MiniZincException extends RuntimeException {
	private static final long serialVersionUID = 3305311052177462975L;

	public MiniZincException() {
		super();
	}

	public MiniZincException(String message) {
		super(message);
	}

	public MiniZincException(String message, Throwable cause) {
		super(message, cause);
	}

	public MiniZincException(Throwable cause) {
		super(cause);
	}
}