//package org.itmo.jcstress;
//
//import org.itmo.Graph;
//import org.itmo.RandomGraphGenerator;
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
//import java.util.Random;
//import java.util.concurrent.Callable;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.stream.Collectors;
//
//@JCStressTest
//@Outcome(id = "true", expect = Expect.ACCEPTABLE, desc = "Each vertex visited exactly once")
//@Outcome(id = "false", expect = Expect.FORBIDDEN, desc = "Each vertex was visited more than one")
//@State
//public class GraphJCStressTestDataRace {
//    private static final int VERTICES = 1_000_000;
//    private static final int EDGES = 10_000_000;
//
//    private final Graph graph;
//    private final List<AtomicBoolean> visited;
//
//    public GraphJCStressTestDataRace() {
//        graph = new RandomGraphGenerator().generateGraph(new Random(42), VERTICES, EDGES);
//
//        visited = new ArrayList<>();
//        for (int i = 0; i < VERTICES; i++) {
//            visited.add(new AtomicBoolean(false));
//        }
//    }
//
//    @Actor
//    public void actor1() {
//        // Modified parallelBFS to collect order
//        List<Integer> currentLevel = new ArrayList<>();
//        currentLevel.add(0);
//        visited.get(0).set(true);
//
//        try {
//            while (!currentLevel.isEmpty()) {
//                List<List<Integer>> nextLevelSubLists = Collections.synchronizedList(new ArrayList<>());
//                List<Callable<Void>> tasks = new ArrayList<>();
//
//                for (int from = 0; from < currentLevel.size(); from += 10) {
//                    int to = Math.min(from + 10, currentLevel.size());
//                    List<Integer> batch = currentLevel.subList(from, to);
//
//                    tasks.add(() -> {
//                        List<Integer> nextLevelFromBatch = new ArrayList<>();
//                        for (int vertex : batch) {
//                            for (int child : graph.getAdjList().get(vertex)) {
//                                if (visited.get(child).compareAndSet(false, true)) {
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
//        System.out.println("АЛО " + visited.toString());
//        r.r1 = visited.stream().allMatch(AtomicBoolean::get);
//    }
//}
//
