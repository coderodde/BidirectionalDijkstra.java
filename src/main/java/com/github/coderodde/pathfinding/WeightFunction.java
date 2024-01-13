package com.github.coderodde.pathfinding;

/**
 * This interface defines the API for graph weight functions.
 * 
 * @param <N> the actual graph node type.
 * @param <W> the type of the weight values.
 */
public interface WeightFunction<N, W> {
    
    /**
     * Returns the weight of the arc {@code (tail, head)}.
     * 
     * @param tail the starting node of the arc.
     * @param head the ending node of the arc.
     * @return the weight of the input arc.
     */
    W getWeight(N tail, N head);
    
    /**
     * Returns the value of type {@code W} representing zero.
     * 
     * @return the zero value.
     */
    W getZero();
    
    /**
     * Returns the largest representable weight.
     * 
     * @return the largest weight. 
     */
    W getInfinity();
    
    /**
     * Returns the sum of {@code w1} and {@code w2}.
     * 
     * @param w1 the first weight value.
     * @param w2 the second weight value.
     * 
     * @return the sum of the two input weights.
     */
    W sum(W w1, W w2);
    
    /**
     * Returns the sum of three input weights. We need this method primarily for 
     * bidirectional Dijkstra's algorithm.
     * 
     * @param w1 the first weight.
     * @param w2 the second weight.
     * @param w3 the third weight.
     * @return the sum of the three input weights.
     */
    default W sum(W w1, W w2, W w3) {
        return sum(w1, sum(w2, w3));
    }
}
