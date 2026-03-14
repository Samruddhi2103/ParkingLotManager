package graphs;

import java.util.*;

public class ParkingGraph {
    private int totalSlots;
    private int totalNodes;
    private List<List<Edge>> adjacencyList;
    
    private int entranceNode;
    private int exitNode;
    
    public ParkingGraph(int totalSlots) {
        this.totalSlots = totalSlots;
        this.totalNodes = totalSlots + 2;
        this.entranceNode = totalSlots + 1;  // Node 31
        this.exitNode = totalSlots + 2;      // Node 32
        
        adjacencyList = new ArrayList<>();
        for (int i = 0; i <= totalNodes; i++) {
            adjacencyList.add(new ArrayList<>());
        }
        
        buildParkingGraph();
    }
    
    private void buildParkingGraph() {
        int slotsPerFloor = 10;
        
        // === 1. ENTRANCE connects to Slot 1 ===
        addEdge(entranceNode, 1, 2);
        
        // === 2. Connect slots HORIZONTALLY on each floor ===
        for (int floor = 0; floor < 3; floor++) {
            int startSlot = floor * slotsPerFloor + 1;
            int endSlot = startSlot + slotsPerFloor - 1;
            
            for (int slot = startSlot; slot < endSlot; slot++) {
                addEdge(slot, slot + 1, 5);  // 5 meters between adjacent slots
            }
        }
        
        // === 3. Connect floors VERTICALLY ===
        addEdge(10, 11, 10);  // Floor 0 → Floor 1
        addEdge(20, 21, 10);  // Floor 1 → Floor 2
        
        // === 4. EXIT ACCESS FROM EACH FLOOR ===
        // Each floor's LAST SLOT connects to EXIT
        addEdge(10, exitNode, 15);  // Floor 0 exit (Slot 10 → EXIT)
        addEdge(20, exitNode, 15);  // Floor 1 exit (Slot 20 → EXIT)
        addEdge(30, exitNode, 15);  // Floor 2 exit (Slot 30 → EXIT)
    }
    
    private void addEdge(int from, int to, int weight) {
        adjacencyList.get(from).add(new Edge(to, weight));
        adjacencyList.get(to).add(new Edge(from, weight));
    }
    
    // === DIJKSTRA'S ALGORITHM ===
    
    public PathResult findShortestPath(int start, int end) {
        int[] distance = new int[totalNodes + 1];
        Arrays.fill(distance, Integer.MAX_VALUE);
        distance[start] = 0;
        
        boolean[] visited = new boolean[totalNodes + 1];
        int[] parent = new int[totalNodes + 1];
        Arrays.fill(parent, -1);
        
        PriorityQueue<Node> pq = new PriorityQueue<>((a, b) -> a.distance - b.distance);
        pq.offer(new Node(start, 0));
        
        while (!pq.isEmpty()) {
            Node current = pq.poll();
            
            if (current.id == end) break;
            
            if (visited[current.id]) continue;
            visited[current.id] = true;
            
            for (Edge edge : adjacencyList.get(current.id)) {
                int neighbor = edge.destination;
                int newDistance = distance[current.id] + edge.weight;
                
                if (newDistance < distance[neighbor]) {
                    distance[neighbor] = newDistance;
                    parent[neighbor] = current.id;
                    pq.offer(new Node(neighbor, newDistance));
                }
            }
        }
        
        List<Integer> path = reconstructPath(parent, start, end);
        
        return new PathResult(path, distance[end]);
    }
    
    private List<Integer> reconstructPath(int[] parent, int start, int end) {
        List<Integer> path = new ArrayList<>();
        int current = end;
        
        while (current != -1) {
            path.add(0, current);
            current = parent[current];
        }
        
        return path;
    }
    
    public PathResult findEmergencyExitPath(int start, int end) {
        return findShortestPath(start, end);
    }
    
    // === FORMAT OUTPUT ===
    
    public String formatPath(PathResult result) {
        if (result.path.isEmpty() || result.distance == Integer.MAX_VALUE) {
            return "ERROR: No path found!";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== SHORTEST PATH TO EXIT ===\n");
        sb.append("Total Distance: ").append(result.distance).append(" meters\n\n");
        sb.append("Route:\n");
        
        for (int i = 0; i < result.path.size(); i++) {
            int node = result.path.get(i);
            
            if (node == entranceNode) {
                sb.append("ENTRANCE");
            } else if (node == exitNode) {
                sb.append("EXIT GATE");
            } else {
                sb.append("Slot ").append(node);
            }
            
            if (i < result.path.size() - 1) {
                sb.append(" → ");
            }
        }
        
        return sb.toString();
    }
    
    public String formatEmergencyPath(PathResult result) {
        if (result.path.isEmpty() || result.distance == Integer.MAX_VALUE) {
            return "ERROR: No emergency path found!";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("🚨 EMERGENCY EXIT ROUTE 🚨\n");
        sb.append("Priority Path - Distance: ").append(result.distance).append(" meters\n\n");
        sb.append("Route:\n");
        
        for (int i = 0; i < result.path.size(); i++) {
            int node = result.path.get(i);
            
            if (node == entranceNode) {
                sb.append("🚗 ENTRANCE");
            } else if (node == exitNode) {
                sb.append("🚪 EXIT GATE");
            } else {
                sb.append("Slot ").append(node);
            }
            
            if (i < result.path.size() - 1) {
                sb.append(" → ");
            }
        }
        
        return sb.toString();
    }
    
    public int getExitNode() {
        return exitNode;
    }
    
    public int getEntranceNode() {
        return entranceNode;
    }
    
    // === INNER CLASSES ===
    
    public static class Edge {
        int destination;
        int weight;
        
        public Edge(int destination, int weight) {
            this.destination = destination;
            this.weight = weight;
        }
    }
    
    private static class Node {
        int id;
        int distance;
        
        public Node(int id, int distance) {
            this.id = id;
            this.distance = distance;
        }
    }
    
    public static class PathResult {
        public List<Integer> path;
        public int distance;
        
        public PathResult(List<Integer> path, int distance) {
            this.path = path;
            this.distance = distance;
        }
    }
}
