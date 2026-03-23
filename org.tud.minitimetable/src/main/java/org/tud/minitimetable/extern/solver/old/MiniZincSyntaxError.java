package org.tud.minitimetable.extern.solver.old;

public class MiniZincSyntaxError extends MiniZincException {
	private static final long serialVersionUID = 5033381712357127028L;
	private String errorLocation;

	public MiniZincSyntaxError(String message, String errorLocation) {
		super(message + "\n" + errorLocation);
		this.errorLocation = errorLocation;
	}
}