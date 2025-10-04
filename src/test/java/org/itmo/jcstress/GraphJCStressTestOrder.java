//package org.itmo.jcstress;
//
//import org.itmo.Graph;
//import org.openjdk.jcstress.annotations.Actor;
//import org.openjdk.jcstress.annotations.Arbiter;
//import org.openjdk.jcstress.annotations.Expect;
//import org.openjdk.jcstress.annotations.JCStressTest;
//import org.openjdk.jcstress.annotations.Outcome;
//import org.openjdk.jcstress.annotations.State;
//import org.openjdk.jcstress.infra.results.L_Result;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.concurrent.Callable;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.stream.Collectors;
//
//@JCStressTest
//@Outcome(id = "true", expect = Expect.ACCEPTABLE, desc = "Correct BFS traversal with no duplicates or missing vertices")
//@Outcome(id = "false", expect = Expect.FORBIDDEN, desc = "Incorrect BFS traversal due to race conditions")
//@State
//public class GraphJCStressTestOrder {
//    private final Graph graph;
//    private final List<AtomicBoolean> visited;
//    private final List<Integer> parallelOrder;
//    private final List<Integer> sequentialOrder;
//
//    public GraphJCStressTestOrder() {
//        // Create a simple graph: 0 -> 1, 2; 1 -> 3; 2 -> 3
//        graph = new Graph(4);
//        graph.addEdge(0, 1);
//        graph.addEdge(0, 2);
//        graph.addEdge(1, 3);
//        graph.addEdge(2, 3);
//
//        visited = new ArrayList<>();
//        for (int i = 0; i < 4; i++) {
//            visited.add(new AtomicBoolean(false));
//        }
//        parallelOrder = Collections.synchronizedList(new ArrayList<>());
//        sequentialOrder = new ArrayList<>();
//        computeSequentialOrder();
//    }
//
//    private void computeSequentialOrder() {
//        boolean[] seqVisited = new boolean[4];
//        LinkedList<Integer> queue = new LinkedList<>();
//        seqVisited[0] = true;
//        queue.add(0);
//        sequentialOrder.add(0);
//
//        while (!queue.isEmpty()) {
//            int vertex = queue.poll();
//            for (int n : graph.getAdjList().get(vertex)) {
//                if (!seqVisited[n]) {
//                    seqVisited[n] = true;
//                    queue.add(n);
//                    sequentialOrder.add(n);
//                }
//            }
//        }
//    }
//
//    @Actor
//    public void actor1() {
//        // Modified parallelBFS to collect order
//        List<Integer> currentLevel = new ArrayList<>();
//        currentLevel.add(0);
//        visited.get(0).set(true);
//        parallelOrder.add(0);
//
//        try {
//            while (!currentLevel.isEmpty()) {
//                List<List<Integer>> nextLevelSubLists = Collections.synchronizedList(new ArrayList<>());
//                List<Callable<Void>> tasks = new ArrayList<>();
//
//                for (int from = 0; from < currentLevel.size(); from += 10_000) {
//                    int to = Math.min(from + 10_000, currentLevel.size());
//                    List<Integer> batch = currentLevel.subList(from, to);
//
//                    tasks.add(() -> {
//                        List<Integer> nextLevelFromBatch = new ArrayList<>();
//                        for (int vertex : batch) {
//                            for (int child : graph.getAdjList().get(vertex)) {
//                                if (visited.get(child).compareAndSet(false, true)) {
//                                    parallelOrder.add(child);
//                                    nextLevelFromBatch.add(child);
//                                }
//                            }
//                        }
//                        nextLevelSubLists.add(nextLevelFromBatch);
//                        return null;
//                    });
//                }
//
//                graph.getExecutor().invokeAll(tasks);
//
//                currentLevel = nextLevelSubLists.stream()
//                        .flatMap(List::stream)
//                        .collect(Collectors.toList());
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } finally {
//            graph.getExecutor().shutdown();
//        }
//    }
//
//    @Arbiter
//    public void arbiter(L_Result r) {
//        // Check if the parallel BFS order matches the sequential BFS order
//        // Allow for different valid BFS orders (e.g., [0,1,2,3] or [0,2,1,3])
//        List<Integer> expected = new ArrayList<>(sequentialOrder);
//        List<Integer> alternate = Arrays.asList(0, 2, 1, 3); // Another valid BFS order
//        r.r1 = parallelOrder.equals(expected) || parallelOrder.equals(alternate);
//    }
//}
