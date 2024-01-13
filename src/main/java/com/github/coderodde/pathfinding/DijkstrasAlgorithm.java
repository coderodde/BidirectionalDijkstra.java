package com.github.coderodde.pathfinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 * This class implements the (unidirectional) Dijkstra's algorithm.
 * 
 * @param <N> the actual graph node type.
 * @param <W> the weight value type.
 */
public final class DijkstrasAlgorithm<N, W> {

    /**
     * Finds the shortest {@code source/target} path or throws an 
     * {@link IllegalStateException} if the target node is not reachable from 
     * the source node.
     * 
     * @param source           the source node.
     * @param target           the target node.
     * @param childrenExpander the children expander.
     * @param weightFunction   the graph weight function.
     * @param scoreComparator  the score comparator.
     * 
     * @return the shortest path, if any exist.
     */
    public List<N> findShortestPath(N source,
                                    N target, 
                                    NodeExpander<N> childrenExpander,
                                    WeightFunction<N, W> weightFunction,
                                    Comparator<W> scoreComparator) {
        
        Queue<HeapNodeWrapper<N, W>> open = new PriorityQueue<>();
        Map<N, W> distanceMap             = new HashMap<>();
        Map<N, N> parentMap               = new HashMap<>();
        Set<N> closed                     = new HashSet<>();
        
        open.add(new HeapNodeWrapper<>(
                weightFunction.getZero(), 
                source, 
                scoreComparator));
        
        distanceMap.put(source, weightFunction.getZero());
        parentMap.put(source, null);
        
        while (!open.isEmpty()) {
            N currentNode = open.remove().getNode();
            
            if (currentNode.equals(target)) {
                return tracebackSolution(target, parentMap);
            }
            
            closed.add(currentNode);
            
            for (N childNode : childrenExpander.expand(currentNode)) {
                if (closed.contains(childNode)) {
                    continue;
                }
                
                if (!distanceMap.containsKey(childNode)) {
                    W tentativeDistance = 
                            weightFunction.sum(
                                    distanceMap.get(currentNode),
                                    weightFunction.getWeight(currentNode, 
                                                             childNode));
                    
                    distanceMap.put(childNode, tentativeDistance);
                    parentMap.put(childNode, currentNode);
                    open.add(new HeapNodeWrapper<>(tentativeDistance, 
                                                   childNode,
                                                   scoreComparator));
                } else {
                    W tentativeDistance = 
                            weightFunction.sum(
                                    distanceMap.get(currentNode),
                                    weightFunction.getWeight(currentNode, 
                                                             childNode));

                    if (scoreComparator.compare(distanceMap.get(childNode), tentativeDistance) > 0) {
                        distanceMap.put(childNode, tentativeDistance);
                        parentMap.put(childNode, currentNode);
                        open.add(new HeapNodeWrapper<>(tentativeDistance,
                                                      childNode,
                                                      scoreComparator));
                    }
                }
            }
        }
        
        throw new IllegalStateException(
                "Target not reachable from the source.");
    }
    
    private static <N> List<N> tracebackSolution(N target, Map<N, N> parentMap) {
        List<N> path = new ArrayList<>();
        N node = target;
        
        while (node != null) {
            path.add(node);
            node = parentMap.get(node);
        }
        
        Collections.reverse(path);
        return path;
    }
}
