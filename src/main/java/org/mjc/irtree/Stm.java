package org.mjc.irtree;

public abstract class Stm {
	public abstract ExpList children();

	public abstract Stm build(ExpList children);
}
