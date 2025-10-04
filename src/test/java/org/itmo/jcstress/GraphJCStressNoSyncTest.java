package org.itmo.jcstress;

import org.itmo.Graph;
import org.itmo.RandomGraphGenerator;
import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Arbiter;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.L_Result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@JCStressTest
@Outcome(id = "true", expect = Expect.FORBIDDEN, desc = "Lucky")
@Outcome(id = "false", expect = Expect.ACCEPTABLE, desc = "There's data race")
@State
public class GraphJCStressNoSyncTest {
    private static final int VERTICES = 4;
    private static final int EDGES = 8;

    private final Graph graph;
    private final List<Boolean> visited;
    private final List<Integer> visitedCount;

    public GraphJCStressNoSyncTest() {
        graph = new RandomGraphGenerator().generateGraph(new Random(42), VERTICES, EDGES);

        visited = new ArrayList<>();
        visitedCount = new ArrayList<>();
        for (int i = 0; i < VERTICES; i++) {
            visited.add(false);
        }
        for (int i = 0; i < VERTICES; i++) {
            visitedCount.add(0);
        }
    }

    @Actor
    public void actor1() {
        // Modified parallelBFS to collect order
        List<Integer> currentLevel = new ArrayList<>();
        currentLevel.add(0);
        visited.set(0, true);
        visitedCount.set(0, 1);

        try {
            while (!currentLevel.isEmpty()) {
                List<List<Integer>> nextLevelSubLists = Collections.synchronizedList(new ArrayList<>());
                List<Callable<Void>> tasks = new ArrayList<>();

                for (int from = 0; from < currentLevel.size(); from += 2) {
                    int to = Math.min(from + 2, currentLevel.size());
                    List<Integer> batch = currentLevel.subList(from, to);

                    tasks.add(() -> {
                        List<Integer> nextLevelFromBatch = new ArrayList<>();
                        for (int vertex : batch) {
                            List<Integer> children = graph.getAdjList().get(vertex);
                            if (children != null) {
                                for (int child : graph.getAdjList().get(vertex)) {
                                    visited.set(vertex, true);
                                    nextLevelFromBatch.add(child);
                                    visitedCount.set(0, visitedCount.get(0) + 1);
                                }
                            }
                        }
                        nextLevelSubLists.add(nextLevelFromBatch);
                        return null;
                    });
                }

                graph.getExecutor().invokeAll(tasks);

                currentLevel = nextLevelSubLists.stream()
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            graph.getExecutor().shutdown();
        }
    }

    @Arbiter
    public void arbiter(L_Result r) {
        System.out.println("АЛО " + visited.toString());
        r.r1 = visitedCount.stream().noneMatch(v -> v == 1);
    }
}


