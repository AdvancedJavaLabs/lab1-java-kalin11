package org.itmo;

import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class BFSTest {

    @Test
    public void bfsTest() throws IOException {
        int[] sizes = new int[]{10, 100, 1000, 10_000, 10_000, 50_000, 100_000, 1_000_000, 2_000_000};
        int[] connections = new int[]{50, 500, 5000, 50_000, 100_000, 1_000_000, 1_000_000, 10_000_000, 10_000_000};
        Random r = new Random(42);
        try (FileWriter fw = new FileWriter("tmp/results.txt")) {
            for (int i = 0; i < sizes.length; i++) {
                System.out.println("--------------------------");
                System.out.println("Generating graph of size " + sizes[i] + " ...wait");
                Graph g = new RandomGraphGenerator().generateGraph(r, sizes[i], connections[i]);
                System.out.println("Generation completed!\nStarting bfs");
                long serialTime = executeSerialBfsAndGetTime(g);
                long parallelTime = executeParallelBfsAndGetTime(g, 8);
                fw
                        .append("Times for ").append(String.valueOf(sizes[i])).append(" vertices and ").append(String.valueOf(connections[i])).append(" connections: ")
                        .append("\nSerial: ").append(String.valueOf(serialTime))
                        .append("\nParallel: ").append(String.valueOf(parallelTime))
                        .append("\n--------\n");
            }
            fw.flush();
        }
    }

    @Test
    public void differentThreadsCountTest() throws IOException {
        int[] threadsCount = new int[Runtime.getRuntime().availableProcessors()];
        for (int i = 0; i < threadsCount.length; i++) {
            threadsCount[i] = i + 1;
        }
        int verticesCount = 1_000_000;
        int connectionsCount = 10_000_000;

        int repeatTimes = 5;

        Random r = new Random(42);

        try (FileWriter fw = new FileWriter("tmp/threads_results.txt")) {
            for (int i = 0; i < threadsCount.length; i++) {
                System.out.println("--------------------------");
                System.out.println("Generating graph of size " + verticesCount + " ...wait");
                System.out.println("Threads count: " + threadsCount[i]);
                Graph g = new RandomGraphGenerator().generateGraph(r, verticesCount, connectionsCount);
                System.out.println("Generation completed!\nStarting bfs");

                long sumSerialTime = 0;
                long sumParallelTime = 0;

                for (int j = 0; j < repeatTimes; j++) {
                    sumSerialTime += executeSerialBfsAndGetTime(g);
                    sumParallelTime += executeParallelBfsAndGetTime(g, threadsCount[i]);
                }

                fw
                        .append("Times for ").append(String.valueOf(verticesCount)).append(" vertices and ").append(String.valueOf(connectionsCount)).append(" connections: ")
                        .append("\nThreads count: ").append(String.valueOf(threadsCount[i]))
                        .append("\nSerial: ").append(String.valueOf((long) (sumSerialTime / repeatTimes)))
                        .append("\nParallel: ").append(String.valueOf((long) (sumParallelTime / repeatTimes)))
                        .append("\n--------\n");
            }
            fw.flush();
        }

    }

    private long executeSerialBfsAndGetTime(Graph g) {
        long startTime = System.currentTimeMillis();
        g.bfs(0);
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private long executeParallelBfsAndGetTime(Graph g, int threadsCount) {
        long startTime = System.currentTimeMillis();
        g.parallelBFS(0, threadsCount);
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

}
