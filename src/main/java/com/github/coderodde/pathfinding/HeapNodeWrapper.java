package com.github.coderodde.pathfinding;

import java.util.Comparator;

final class HeapNodeWrapper<N, W> implements Comparable<HeapNodeWrapper<N, W>> {

    private final W score;
    private final N node;
    private final Comparator<W> scoreComparator;
    
    HeapNodeWrapper(W score,
                    N node,
                    Comparator<W> scoreComparator) {
        this.score = score;
        this.node = node;
        this.scoreComparator = scoreComparator;
    }
    
    N getNode() {
        return node;
    }
    
    @Override
    public int compareTo(HeapNodeWrapper<N, W> o) {
        return scoreComparator.compare(this.score, o.score);
    }
}
