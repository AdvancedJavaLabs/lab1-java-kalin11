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
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.stream.Collectors;

@JCStressTest
@Outcome(id = "true", expect = Expect.ACCEPTABLE, desc = "Each vertex visited exactly once")
@Outcome(id = "false", expect = Expect.FORBIDDEN, desc = "Each vertex was visited more than one")
@State
public class GraphJCStressTestDataRace {
    private static final int VERTICES = 20;

    private final Graph graph;
    private final AtomicIntegerArray visited = new AtomicIntegerArray(VERTICES);
    private Queue<Integer> currentLevel = new ConcurrentLinkedQueue<>();
    private Queue<Integer> nextLevel = new ConcurrentLinkedQueue<>();

    public GraphJCStressTestDataRace() {
        graph = new Graph(VERTICES);
        for (int i = 1; i < VERTICES; i++) {
            graph.addEdge(0, i);
        }

        currentLevel.add(0);
        visited.compareAndSet(0, 0, 1);
    }

    @Actor
    public void actor1() {
        Integer vertex;
        while ((vertex = currentLevel.poll()) != null) {
            graph.markVertexAsVisited(vertex, visited, nextLevel);
        }
    }

    @Actor
    public void actor2() {
        Integer vertex;
        while ((vertex = currentLevel.poll()) != null) {
            graph.markVertexAsVisited(vertex, visited, nextLevel);
        }
    }

    @Actor
    public void actor3() {
        Integer vertex;
        while ((vertex = currentLevel.poll()) != null) {
            graph.markVertexAsVisited(vertex, visited, nextLevel);
        }
    }

    @Actor
    public void actor4() {
        Integer vertex;
        while ((vertex = currentLevel.poll()) != null) {
            graph.markVertexAsVisited(vertex, visited, nextLevel);
        }
    }

    @Actor
    public void actor5() {
        Integer vertex;
        while ((vertex = currentLevel.poll()) != null) {
            graph.markVertexAsVisited(vertex, visited, nextLevel);
        }
    }

    @Arbiter
    public void arbiter(L_Result r) {
        // currentLevel исчерпан — делаем nextLevel новым уровнем
        currentLevel = nextLevel;

        // считаем количество вершин, которые были посещены
        int visitedCount = 0;
        for (int i = 0; i < VERTICES; i++) {
            if (visited.get(i) == 1) visitedCount++;
        }

        r.r1 = visitedCount == VERTICES; // должно быть 10, если гонок нет
    }
}

