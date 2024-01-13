package com.github.coderodde.pathfinding;

import java.util.ArrayList;
import java.util.Arrays;
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
 * This class implements a bidirectional Dijkstra's algorithm. 
 * 
 * @param <N> the actual graph node type.
 * @param <W> the value type of arc weights.
 */
public final class BidirectionalDijkstrasAlgorithm<N, W> {
    
    /**
     * Searches for a shortest {@code source/target} path. Throws an 
     * {@link IllegalStateException} if the target node is not reachable from 
     * the source node.
     * 
     * @param source           the source node.
     * @param target           the target node.
     * @param childrenExpander the node expander generating child nodes.
     * @param parentsExpander  the node expander generating parent nodes.
     * @param weightFunction   the weight function of the graph.
     * @param scoreComparator  the comparator for comparing weights/node 
     *                         g-scores.
     * 
     * @return the shortest path.
     */
    public List<N> findShortestPath(N source,
                                    N target,
                                    NodeExpander<N> childrenExpander,
                                    NodeExpander<N> parentsExpander,
                                    WeightFunction<N, W> weightFunction,
                                    Comparator<W> scoreComparator) {
        if (source.equals(target)) {
            // We need to handle this special case, since the actual algorithm
            // cannot deal with it.
            return Arrays.asList(target);
        }
        
        Queue<HeapNodeWrapper<N, W>> queueF = new PriorityQueue<>();
        Queue<HeapNodeWrapper<N, W>> queueB = new PriorityQueue<>();
        Map<N, W> distancesF = new HashMap<>();
        Map<N, W> distancesB = new HashMap<>();
        Map<N, N> parentsF = new HashMap<>();
        Map<N, N> parentsB = new HashMap<>();
        Set<N> settledF = new HashSet<>();
        Set<N> settledB = new HashSet<>();
        
        queueF.add(new HeapNodeWrapper<>(
                weightFunction.getZero(),
                source, 
                scoreComparator));
        
        queueB.add(new HeapNodeWrapper<>(
                weightFunction.getZero(), 
                target,
                scoreComparator));
        
        distancesF.put(source, weightFunction.getZero());
        distancesB.put(target, weightFunction.getZero());
        
        parentsF.put(source, null);
        parentsB.put(target, null);
        
        W mu = weightFunction.getInfinity();
        N touchNodeF = null;
        N touchNodeB = null;
        
        while (!queueF.isEmpty() && !queueB.isEmpty()) {
            N currentNodeF = queueF.remove().getNode();
            N currentNodeB = queueB.remove().getNode();
            
            settledF.add(currentNodeF);
            settledB.add(currentNodeB);
            
            for (N childNode : childrenExpander.expand(currentNodeF)) {
                if (settledF.contains(childNode)) {
                    continue;
                }
                
                if (!distancesF.containsKey(childNode) ||
                     scoreComparator.compare(
                           distancesF.get(childNode),        
                           weightFunction.sum(
                                distancesF.get(currentNodeF),
                                weightFunction.getWeight(currentNodeF, 
                                                         childNode))) > 0) {
                    
                    W tentativeDistance = 
                            weightFunction.sum(
                                    distancesF.get(currentNodeF), 
                                    weightFunction.getWeight(currentNodeF, 
                                                             childNode));
                    
                    distancesF.put(childNode, tentativeDistance);
                    parentsF.put(childNode, currentNodeF);
                    queueF.add(new HeapNodeWrapper<>(tentativeDistance,
                                                     childNode,
                                                     scoreComparator));
                } 
                
                if (settledB.contains(childNode)) {
                    W shortestPathUpperBound = 
                            weightFunction.sum(
                                    distancesF.get(currentNodeF),
                                    weightFunction.getWeight(currentNodeF, 
                                                             childNode),
                                    distancesB.get(childNode));
                    
                    if (scoreComparator.compare(mu, 
                                                shortestPathUpperBound) > 0) {
                        
                        mu = shortestPathUpperBound;
                        touchNodeF = currentNodeF;
                        touchNodeB = childNode;
                    }
                }
            }
            
            for (N parentNode : parentsExpander.expand(currentNodeB)) {
                if (settledB.contains(parentNode)) {
                    continue;
                }
                
                if (!distancesB.containsKey(parentNode) ||
                     scoreComparator.compare(
                           distancesB.get(parentNode),        
                           weightFunction.sum(
                                distancesB.get(currentNodeB),
                                weightFunction.getWeight(parentNode, 
                                                         currentNodeB))) > 0) {
                    
                    W tentativeDistance = 
                            weightFunction.sum(
                                    distancesB.get(currentNodeB), 
                                    weightFunction.getWeight(parentNode,
                                                             currentNodeB));
                    
                    distancesB.put(parentNode, tentativeDistance);
                    parentsB.put(parentNode, currentNodeB);
                    queueB.add(new HeapNodeWrapper<>(tentativeDistance,
                                                     parentNode,
                                                     scoreComparator));
                } 
                
                if (settledF.contains(parentNode)) {
                    W shortestPathUpperBound = 
                            weightFunction.sum(
                                    distancesF.get(parentNode),
                                    weightFunction.getWeight(parentNode,
                                                             currentNodeB),
                                    distancesB.get(currentNodeB));
                    
                    if (scoreComparator.compare(mu, 
                                                shortestPathUpperBound) > 0) {
                        
                        mu = shortestPathUpperBound;
                        touchNodeF = parentNode;
                        touchNodeB = currentNodeB;
                    }
                }   
            }
            
            if (distancesF.containsKey(currentNodeF) && 
                distancesB.containsKey(currentNodeB) &&
                scoreComparator.compare(
                        weightFunction.sum(
                                distancesF.get(currentNodeF),
                                distancesB.get(currentNodeB)), 
                        mu) > 0) {
                
                return tracebackPath(touchNodeF, 
                                     touchNodeB,
                                     parentsF,
                                     parentsB);
            }
        }
        
        throw new IllegalStateException(
                "The target node is not reachable from the source node.");
    }
    
    private static <N> List<N> tracebackPath(N touchNodeF,
                                             N touchNodeB,
                                             Map<N, N> parentsF,
                                             Map<N, N> parentsB) {
        List<N> path = new ArrayList<>();
        
        N node = touchNodeF;
        
        while (node != null) {
            path.add(node);
            node = parentsF.get(node);
        }
        
        Collections.reverse(path);
        node = touchNodeB;
        
        while (node != null) {
            path.add(node);
            node = parentsB.get(node);
        }
        
        return path;
    }
}
