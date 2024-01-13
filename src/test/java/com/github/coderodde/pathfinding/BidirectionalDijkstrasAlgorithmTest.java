package com.github.coderodde.pathfinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class BidirectionalDijkstrasAlgorithmTest {
    
    private static final int GRAPH_SIZE = 2_000;
    private static final int ARCS = 10_000;
    
    private static final DijkstrasAlgorithm<DirectedGraphNode, Integer> 
            pathfinder1 = new DijkstrasAlgorithm<>();
    
    private static final BidirectionalDijkstrasAlgorithm<DirectedGraphNode,
                                                         Integer> 
            pathfinder2 = new BidirectionalDijkstrasAlgorithm<>();
    
    private static final DirectedGraphNodeChildrenExpander childrenExpander = 
            new DirectedGraphNodeChildrenExpander();
    
    private static final DirectedGraphNodeParentsExpander parentsExpander = 
            new DirectedGraphNodeParentsExpander();
    
    private static final DirectedGraphWeightFunction weightFunction = 
            new DirectedGraphWeightFunction();
    
    @Test
    public void singleNodeGraphNoSelfLoop() {
        DirectedGraphNode node = new DirectedGraphNode();
        List<DirectedGraphNode> path = 
                pathfinder2.findShortestPath(
                        node, 
                        node,
                        childrenExpander, 
                        parentsExpander,
                        weightFunction,
                        Integer::compare);
        
        assertEquals(1, path.size());
        assertEquals(node, path.get(0));
    }
    
    @Test
    public void twoNodeGraph() {
        DirectedGraphNode source = new DirectedGraphNode();
        DirectedGraphNode target = new DirectedGraphNode();
        source.addChild(target, 2);
        
        List<DirectedGraphNode> path = 
                pathfinder2.findShortestPath(
                        source, 
                        target,
                        childrenExpander, 
                        parentsExpander,
                        weightFunction,
                        Integer::compare);
        
        assertEquals(2, path.size());
        assertEquals(source, path.get(0));
        assertEquals(target, path.get(1));
    }
    
    @Test
    public void threeNodeGraph() {
        DirectedGraphNode source = new DirectedGraphNode();
        DirectedGraphNode middle = new DirectedGraphNode();
        DirectedGraphNode target = new DirectedGraphNode();
        
        source.addChild(middle, -1);
        middle.addChild(target, -2);
        source.addChild(target, -4);
        target.addChild(source, -10);
        
        List<DirectedGraphNode> path = 
                pathfinder2.findShortestPath(
                        source, 
                        target,
                        childrenExpander, 
                        parentsExpander,
                        weightFunction,
                        Integer::compare);
        
        assertEquals(2, path.size());
        assertEquals(source, path.get(0));
        assertEquals(target, path.get(1));
    }
    
    @Test
    public void fiveNodeGraph() {
        DirectedGraphNode s = new DirectedGraphNode();
        DirectedGraphNode a = new DirectedGraphNode();
        DirectedGraphNode b = new DirectedGraphNode();
        DirectedGraphNode c = new DirectedGraphNode();
        DirectedGraphNode t = new DirectedGraphNode();
        
        // Three node path:
        s.addChild(a, 6);
        a.addChild(t, 4);
        
        // Four node path (the shortest):
        s.addChild(b, 3);
        b.addChild(c, 3);
        c.addChild(t, 3);
        
        List<DirectedGraphNode> path = 
                pathfinder2.findShortestPath(
                        s, 
                        t,
                        childrenExpander, 
                        parentsExpander,
                        weightFunction,
                        Integer::compare);
        
        assertEquals(4, path.size());
        assertEquals(s, path.get(0));
        assertEquals(b, path.get(1));
        assertEquals(c, path.get(2));
        assertEquals(t, path.get(3));
    }
    
    @Test
    public void bruteForceComparisonToDijkstra() {
        Random random = new Random(13L);
        
        DirectedGraphNodeChildrenExpander childrenExpander =
                new DirectedGraphNodeChildrenExpander();
        
        DirectedGraphNodeParentsExpander parentsExpander = 
                new DirectedGraphNodeParentsExpander();
        
        for (int i = 0; i < 10; i++) {
            GraphData graphData = getRandomGraph(GRAPH_SIZE, ARCS, random);
            DirectedGraphWeightFunction weightFunction = 
                    graphData.getWeightFunction();
            
            DirectedGraphNode source = graphData.getRandonNode(random);
            DirectedGraphNode target = graphData.getRandonNode(random);
            
            List<DirectedGraphNode> path1 = null;
            List<DirectedGraphNode> path2 = null;
            
            try {
                path1 = pathfinder1.findShortestPath(
                        source, 
                        target, 
                        childrenExpander, 
                        weightFunction,
                        Integer::compare);
            } catch (IllegalStateException ex) {
                
            }
            
            try {
                path2 = pathfinder2.findShortestPath(
                        source, 
                        target,
                        childrenExpander, 
                        parentsExpander,
                        weightFunction,
                        Integer::compare);
            } catch (IllegalStateException ex) {
                
            }
            
            assertTrue(Objects.equals(path1, path2));
        }
    }
    
    private static final class GraphData {
        private final List<DirectedGraphNode> graphNodes;
        private final DirectedGraphWeightFunction weightFunction;
        
        GraphData(List<DirectedGraphNode> graphNodes,
                  DirectedGraphWeightFunction weightFunction) {
            
            this.graphNodes = graphNodes;
            this.weightFunction = weightFunction;
        }
        
        List<DirectedGraphNode> getGraphNodes() {
            return graphNodes;
        }
        
        DirectedGraphWeightFunction getWeightFunction() {
            return weightFunction;
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
            int weight = random.nextInt(100);
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
    
    private final Map<DirectedGraphNode, Integer> outgoingArcs =
          new HashMap<>();
    
    private final Map<DirectedGraphNode, Integer> incomingArcs =
          new HashMap<>();
    
    DirectedGraphNode() {
        this.id = nodeIdCounter++;
    }
    
    int getId() {
        return id;
    }
    
    void addChild(DirectedGraphNode child, int weight) {
        outgoingArcs.put(child, weight);
        child.incomingArcs.put(this, weight);
    }
    
    List<DirectedGraphNode> getChildren() {
        return new ArrayList<>(outgoingArcs.keySet());
    }
    
    List<DirectedGraphNode> getParents() {
        return new ArrayList<>(incomingArcs.keySet());
    }
    
    Integer getWeightTo(DirectedGraphNode headNode) {
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
        implements WeightFunction<DirectedGraphNode, Integer> {

    @Override
    public Integer getWeight(DirectedGraphNode tail, DirectedGraphNode head) {
        return tail.getWeightTo(head);
    }

    @Override
    public Integer getZero() {
        return 0;
    }

    @Override
    public Integer getInfinity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Integer sum(Integer w1, Integer w2) {
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
