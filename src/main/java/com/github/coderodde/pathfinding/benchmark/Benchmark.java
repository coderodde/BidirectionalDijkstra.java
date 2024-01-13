package com.github.coderodde.pathfinding.benchmark;

import com.github.coderodde.pathfinding.BidirectionalDijkstrasAlgorithm;
import com.github.coderodde.pathfinding.DijkstrasAlgorithm;
import com.github.coderodde.pathfinding.NodeExpander;
import com.github.coderodde.pathfinding.WeightFunction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

final class Benchmark {
 
    private static final int NUMBER_OF_NODES = 100_000;
    private static final int NUMBER_OF_ARCS = 1_000_000;
    
    public static void main(String[] args) {
        long seed = parseSeed(args);
        System.out.println("Seed = " + seed);
        Random random = new Random(seed);
        
        long startTime = System.currentTimeMillis();
        GraphData graphData = getRandomGraph(NUMBER_OF_NODES, 
                                             NUMBER_OF_ARCS, 
                                             random);
        
        System.out.printf("Built the graph in %d milliseconds.\n", 
                          System.currentTimeMillis() - startTime);
        
        DirectedGraphNode source = graphData.getRandonNode(random);
        DirectedGraphNode target = graphData.getRandonNode(random);
        
        System.out.printf("Source node: %s\n", source);
        System.out.printf("Target node: %s\n", target);
        
        DijkstrasAlgorithm<DirectedGraphNode, Float> pathfinderDijkstra =
                new DijkstrasAlgorithm<>();
        
        BidirectionalDijkstrasAlgorithm<DirectedGraphNode, Float> 
                pathfinderBidirectionalDijkstra = 
                new BidirectionalDijkstrasAlgorithm<>();
        
        NodeExpander<DirectedGraphNode> childNodeExpander = 
                new DirectedGraphNodeChildrenExpander();
        
        NodeExpander<DirectedGraphNode> parentNodeExpander = 
                new DirectedGraphNodeParentsExpander();
        
        DirectedGraphWeightFunction weightFunction = 
                new DirectedGraphWeightFunction();
        
        startTime = System.currentTimeMillis();
        
        List<DirectedGraphNode> pathDijkstra = 
                pathfinderDijkstra.findShortestPath(
                        source, 
                        target,
                        childNodeExpander, 
                        weightFunction,
                        Float::compare);
        
        System.out.printf("Dijkstra's algorithm in %d milliseconds.\n",
                          System.currentTimeMillis() - startTime);
        
        startTime = System.currentTimeMillis();
        
        List<DirectedGraphNode> pathBidirectionalDijkstra = 
                pathfinderBidirectionalDijkstra.findShortestPath(
                        source, 
                        target, 
                        childNodeExpander, 
                        parentNodeExpander,
                        weightFunction, 
                        Float::compare);
        
        System.out.printf(
                "Bidirectional Dijkstra's algorithm in %d milliseconds.\n",
                System.currentTimeMillis() - startTime);
        
        boolean pathsAreEqual = pathDijkstra.equals(pathBidirectionalDijkstra);
        
        if (pathsAreEqual) {
            System.out.println("Paths agree:");
            
            for (DirectedGraphNode node : pathDijkstra) {
                System.out.println(node);
            }
            
            System.out.printf(
                    "Path cost: %.3f\n", 
                    computePathCost(pathDijkstra, weightFunction));
            
        } else {
            System.out.println("Paths diagree!");
            System.out.println("Dijkstra's algorithm's path:");
            
            for (DirectedGraphNode node : pathDijkstra) {
                System.out.println(node);
            }
            
            System.out.printf("Dijkstra's path cost: %.3f\n", 
                              computePathCost(pathDijkstra, weightFunction));
            
            System.out.println("Bidirectional Dijkstra's algorithm's path:");
            
            for (DirectedGraphNode node : pathBidirectionalDijkstra) {
                System.out.println(node);
            }
            
            System.out.printf("Bidirectional Dijkstra's path cost: %.3f\n", 
                              computePathCost(pathBidirectionalDijkstra, 
                                              weightFunction));
        }
    }
    
    private static long parseSeed(String[] args) {
        if (args.length == 0) {
            return System.currentTimeMillis();
        }
        
        try {
            return Long.parseLong(args[0]);
        } catch (NumberFormatException ex) {
            System.err.printf("WARNING: Could not parse %s as a long value.",
                              args[0]);
            
            return System.currentTimeMillis();
        }
    }
    
    private static float computePathCost(
            List<DirectedGraphNode> path,
            DirectedGraphWeightFunction weightFunction) {
        float cost = 0.0f;
        
        for (int i = 0; i < path.size() - 1; i++) {
            DirectedGraphNode tail = path.get(i);
            DirectedGraphNode head = path.get(i + 1);
            float arcWeight = weightFunction.getWeight(tail, head);
            cost += arcWeight;
        }
        
        return cost;
    }
    
    private static final class GraphData {
        private final List<DirectedGraphNode> graphNodes;
        private final DirectedGraphWeightFunction weightFunction;
        
        GraphData(List<DirectedGraphNode> graphNodes,
                  DirectedGraphWeightFunction weightFunction) {
            
            this.graphNodes = graphNodes;
            this.weightFunction = weightFunction;
        }
        
        DirectedGraphNode getRandonNode(Random random) {
            return choose(graphNodes, random);
        }
    }
    
    private static final GraphData 
        getRandomGraph(int nodes, int edges, Random random) {
        
        List<DirectedGraphNode> graph = new ArrayList<>(nodes);
        Set<Arc> arcs = new HashSet<>(edges);
        
        for (int i = 0; i < nodes; i++) {
            graph.add(new DirectedGraphNode());
        }
        
        while (arcs.size() < edges) {
            DirectedGraphNode tail = choose(graph, random);
            DirectedGraphNode head = choose(graph, random);
            Arc arc = new Arc(tail, head);
            arcs.add(arc);
        }
        
        DirectedGraphWeightFunction weightFunction = 
                new DirectedGraphWeightFunction();
        
        for (Arc arc : arcs) {
            DirectedGraphNode tail = arc.getTail();
            DirectedGraphNode head = arc.getHead();
            float weight = 100.0f * random.nextFloat();
            tail.addChild(head, weight);
        }
        
        return new GraphData(graph, weightFunction);
    }
        
    private static <T> T choose(List<T> list, Random random) {
        return list.get(random.nextInt(list.size()));
    }
        
    private static final class Arc {
        private final DirectedGraphNode tail;
        private final DirectedGraphNode head;
        
        Arc(DirectedGraphNode tail, DirectedGraphNode head) {
            this.tail = tail;
            this.head = head;
        }
        
        DirectedGraphNode getTail() {
            return tail;
        }
        
        DirectedGraphNode getHead() {
            return head;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(tail, head);
        }
        
        @Override
        public boolean equals(Object o) {
            Arc arc = (Arc) o;
            return tail.equals(arc.tail) && 
                   head.equals(arc.head);
        }
    }
}

final class DirectedGraphNode {
    
    private static int nodeIdCounter = 0;
    private final int id;
    
    private final Map<DirectedGraphNode, Float> outgoingArcs =
          new HashMap<>();
    
    private final Map<DirectedGraphNode, Float> incomingArcs =
          new HashMap<>();
    
    DirectedGraphNode() {
        this.id = nodeIdCounter++;
    }
    
    void addChild(DirectedGraphNode child, Float weight) {
        outgoingArcs.put(child, weight);
        child.incomingArcs.put(this, weight);
    }
    
    List<DirectedGraphNode> getChildren() {
        return new ArrayList<>(outgoingArcs.keySet());
    }
    
    List<DirectedGraphNode> getParents() {
        return new ArrayList<>(incomingArcs.keySet());
    }
    
    Float getWeightTo(DirectedGraphNode headNode) {
        return outgoingArcs.get(headNode);
    }
    
    @Override
    public String toString() {
        return String.format("[DirectedGraphNode id = %d]", id);
    }
    
    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        DirectedGraphNode other = (DirectedGraphNode) obj;
        return this.id == other.id;
    }
}

class DirectedGraphWeightFunction
        implements WeightFunction<DirectedGraphNode, Float> {

    @Override
    public Float getWeight(DirectedGraphNode tail, DirectedGraphNode head) {
        return tail.getWeightTo(head);
    }

    @Override
    public Float getZero() {
        return 0.0f;
    }

    @Override
    public Float getInfinity() {
        return Float.POSITIVE_INFINITY;
    }

    @Override
    public Float sum(Float w1, Float w2) {
        return w1 + w2;
    }
}

class DirectedGraphNodeChildrenExpander 
        implements NodeExpander<DirectedGraphNode> {

    @Override
    public List<DirectedGraphNode> expand(DirectedGraphNode node) {
        return node.getChildren();
    }
}

class DirectedGraphNodeParentsExpander
        implements NodeExpander<DirectedGraphNode> {

    @Override
    public List<DirectedGraphNode> expand(DirectedGraphNode node) {
        return node.getParents();
    }
}
