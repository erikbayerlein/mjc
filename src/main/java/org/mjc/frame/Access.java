package org.mjc.frame;

import org.mjc.irtree.Exp_;

public abstract class Access {
    public abstract String toString();
    public abstract Exp_ exp(Exp_ e);
}