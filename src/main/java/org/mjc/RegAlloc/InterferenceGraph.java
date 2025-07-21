package org.mjc.RegAlloc;

import org.mjc.Graph.Node;
import org.mjc.Graph.Graph;
import org.mjc.temp.Temp;
import org.mjc.RegAlloc.MoveList;

abstract public class InterferenceGraph extends Graph {
    abstract public Node tnode(Temp temp);
    abstract public Temp gtemp(Node node);
    abstract public MoveList moves();
    public int spillCost(Node node) {return 1;}
}