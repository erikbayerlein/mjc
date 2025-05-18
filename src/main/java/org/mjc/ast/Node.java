package org.mjc.ast;

import org.mjc.visitor.Visitor;

public abstract class Node {
	public abstract <T> T accept(Visitor<T> v);
}
