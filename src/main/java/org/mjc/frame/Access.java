package org.mjc.frame;

import org.mjc.irtree.ExpAbstract;

public abstract class Access {
    public abstract String toString();
    public abstract ExpAbstract exp(ExpAbstract e);
}