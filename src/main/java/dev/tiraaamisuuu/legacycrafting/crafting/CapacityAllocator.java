package dev.tiraaamisuuu.legacycrafting.crafting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Package-private, game-independent capacity matcher used by IngredientAllocator. */
final class CapacityAllocator {
    Result allocateMaximum(int[] sourceCounts, boolean[][] matches) {
        if (matches.length == 0 || matches[0].length == 0) {
            return Result.EMPTY;
        }
        int ingredientCount = matches[0].length;
        int totalItems = Arrays.stream(sourceCounts).sum();
        int low = 0;
        int high = totalItems / ingredientCount + 1;
        while (high - low > 1) {
            int middle = (low + high) / 2;
            if (this.allocate(sourceCounts, matches, middle).craftable()) {
                low = middle;
            } else {
                high = middle;
            }
        }
        return this.allocate(sourceCounts, matches, low);
    }

    Result allocate(int[] sourceCounts, boolean[][] matches, int craftCount) {
        if (craftCount <= 0 || matches.length == 0 || matches[0].length == 0) {
            return Result.EMPTY;
        }
        int ingredientCount = matches[0].length;
        FlowNetwork network = this.createNetwork(sourceCounts, matches, craftCount, ingredientCount);
        if (network.maxFlow() != ingredientCount * craftCount) {
            return Result.EMPTY;
        }
        int[][] assignments = new int[sourceCounts.length][ingredientCount];
        for (int source = 0; source < sourceCounts.length; source++) {
            for (int ingredient = 0; ingredient < ingredientCount; ingredient++) {
                Edge edge = network.assignmentEdges[source][ingredient];
                if (edge != null) {
                    assignments[source][ingredient] = edge.originalCapacity - edge.capacity;
                }
            }
        }
        return new Result(craftCount, assignments);
    }

    private FlowNetwork createNetwork(int[] sourceCounts, boolean[][] matches, int craftCount, int ingredientCount) {
        int firstSource = 1;
        int firstIngredient = firstSource + sourceCounts.length;
        int sink = firstIngredient + ingredientCount;
        FlowNetwork network = new FlowNetwork(sink + 1, 0, sink, sourceCounts.length, ingredientCount);
        for (int source = 0; source < sourceCounts.length; source++) {
            network.addEdge(0, firstSource + source, sourceCounts[source]);
            for (int ingredient = 0; ingredient < ingredientCount; ingredient++) {
                if (matches[source][ingredient]) {
                    network.assignmentEdges[source][ingredient] =
                        network.addEdge(firstSource + source, firstIngredient + ingredient, craftCount);
                }
            }
        }
        for (int ingredient = 0; ingredient < ingredientCount; ingredient++) {
            network.addEdge(firstIngredient + ingredient, sink, craftCount);
        }
        return network;
    }

    record Result(int craftCount, int[][] assignments) {
        static final Result EMPTY = new Result(0, new int[0][0]);

        boolean craftable() {
            return this.craftCount > 0;
        }
    }

    private static final class FlowNetwork {
        private final List<List<Edge>> graph;
        private final int source;
        private final int sink;
        private final Edge[][] assignmentEdges;

        private FlowNetwork(int nodeCount, int source, int sink, int sourceCount, int ingredientCount) {
            this.graph = new ArrayList<>(nodeCount);
            for (int index = 0; index < nodeCount; index++) {
                this.graph.add(new ArrayList<>());
            }
            this.source = source;
            this.sink = sink;
            this.assignmentEdges = new Edge[sourceCount][ingredientCount];
        }

        private Edge addEdge(int from, int to, int capacity) {
            Edge forward = new Edge(to, this.graph.get(to).size(), capacity);
            Edge reverse = new Edge(from, this.graph.get(from).size(), 0);
            this.graph.get(from).add(forward);
            this.graph.get(to).add(reverse);
            return forward;
        }

        private int maxFlow() {
            int flow = 0;
            int[] levels = new int[this.graph.size()];
            while (this.buildLevels(levels)) {
                int[] nextEdge = new int[this.graph.size()];
                int pushed;
                while ((pushed = this.push(this.source, Integer.MAX_VALUE, levels, nextEdge)) > 0) {
                    flow += pushed;
                }
            }
            return flow;
        }

        private boolean buildLevels(int[] levels) {
            Arrays.fill(levels, -1);
            int[] queue = new int[this.graph.size()];
            int head = 0;
            int tail = 0;
            levels[this.source] = 0;
            queue[tail++] = this.source;
            while (head < tail) {
                int node = queue[head++];
                for (Edge edge : this.graph.get(node)) {
                    if (edge.capacity > 0 && levels[edge.to] < 0) {
                        levels[edge.to] = levels[node] + 1;
                        queue[tail++] = edge.to;
                    }
                }
            }
            return levels[this.sink] >= 0;
        }

        private int push(int node, int available, int[] levels, int[] nextEdge) {
            if (node == this.sink) {
                return available;
            }
            List<Edge> edges = this.graph.get(node);
            for (; nextEdge[node] < edges.size(); nextEdge[node]++) {
                Edge edge = edges.get(nextEdge[node]);
                if (edge.capacity <= 0 || levels[edge.to] != levels[node] + 1) {
                    continue;
                }
                int pushed = this.push(edge.to, Math.min(available, edge.capacity), levels, nextEdge);
                if (pushed > 0) {
                    edge.capacity -= pushed;
                    this.graph.get(edge.to).get(edge.reverseIndex).capacity += pushed;
                    return pushed;
                }
            }
            return 0;
        }
    }

    private static final class Edge {
        private final int to;
        private final int reverseIndex;
        private final int originalCapacity;
        private int capacity;

        private Edge(int to, int reverseIndex, int capacity) {
            this.to = to;
            this.reverseIndex = reverseIndex;
            this.originalCapacity = capacity;
            this.capacity = capacity;
        }
    }
}

