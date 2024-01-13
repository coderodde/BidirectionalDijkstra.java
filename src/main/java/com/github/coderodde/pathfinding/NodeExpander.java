package com.github.coderodde.pathfinding;

import java.util.Collection;

/**
 * This interface defines the API for all the node expanders.
 * 
 * @param <N> the actual type of the nodes.
 */
public interface NodeExpander<N> {
     
    /**
     * Returns the expansion view of the input node.
     * 
     * @param node the node to expand.
     * @return the collection of "next" nodes to consider in search.
     */
    Collection<N> expand(N node);
}
