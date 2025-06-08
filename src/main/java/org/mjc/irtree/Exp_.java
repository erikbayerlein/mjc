package org.mjc.irtree;


public abstract class Exp_ {
	public abstract ExpList kids();

	public abstract Exp_ build(ExpList children);
}

