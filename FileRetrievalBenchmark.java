package csc435.app;

import java.util.ArrayList;
import java.util.Arrays;

class BenchmarkWorker implements Runnable {
    // TO-DO declare a ClientProcessingEngine
    private ClientProcessingEngine engine;
    private String serverIP;
    private String serverPort;
    private String datasetPath;

    public BenchmarkWorker(String serverIP, String serverPort, String datasetPath) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.datasetPath = datasetPath;
    }

    @Override
    public void run() {
        // TO-DO create a ClientProcessinEngine
        engine = new ClientProcessingEngine();
        // TO-DO connect the ClientProcessingEngine to the server
        engine.connect(serverIP, serverPort);
        // TO-DO index the dataset
        IndexResult result = engine.indexFiles(datasetPath);
        System.out.println("Indexing completed in " + result.executionTime + " seconds");
        System.out.println("Total bytes processed: " + result.totalBytesRead);
    }

    public void search() {
        // TO-DO perform search operations on the ClientProcessingEngine
        // TO-DO print the results and performance
        String[] searchTerms = {"computer", "algorithm", "network", "database"};
        ArrayList<String> terms = new ArrayList<>(Arrays.asList(searchTerms));
        
        SearchResult result = engine.searchFiles(terms);
        System.out.println("Search completed in " + result.excutionTime + " seconds");
        System.out.println("Top matching documents:");
        for (DocPathFreqPair pair : result.documentFrequencies) {
            System.out.println(pair.documentPath + ": " + pair.wordFrequency + " occurrences");
        }
    }

    public void disconnect() {
        // TO-DO disconnect the ClientProcessingEngine from the server
        if (engine != null && engine.channel != null) {
            engine.channel.shutdown();
        }
    }
}

public class FileRetrievalBenchmark {
    public static void main(String[] args)
    {
        String serverIP;
        String serverPort;
        int numberOfClients;
        ArrayList<String> clientsDatasetPath;

        // TO-DO extract the arguments from args
        serverIP = args[0];
        serverPort = args[1];
        numberOfClients = Integer.parseInt(args[2]);
        clientsDatasetPath = new ArrayList<>();
        for (int i = 0; i < numberOfClients; i++) {
            clientsDatasetPath.add(args[i + 3]);
        }

        // TO-DO measure the execution start time
        long startTime = System.currentTimeMillis();

        // TO-DO create and start benchmark worker threads equal to the number of clients
        ArrayList<Thread> threads = new ArrayList<>();
        ArrayList<BenchmarkWorker> workers = new ArrayList<>();
        
        for (int i = 0; i < numberOfClients; i++) {
            BenchmarkWorker worker = new BenchmarkWorker(serverIP, serverPort, clientsDatasetPath.get(i));
            workers.add(worker);
            Thread thread = new Thread(worker);
            threads.add(thread);
            thread.start();
        }

        // TO-DO join the benchmark worker threads
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // TO-DO measure the execution stop time and print the performance
        long endTime = System.currentTimeMillis();
        System.out.println("Indexing completed for all clients in " + (endTime - startTime) + " ms");

        // TO-DO run search queries on the first client (benchmark worker thread number 1)
        if (!workers.isEmpty()) {
            workers.get(0).search();
        }

        // TO-DO disconnect all clients (all benchmakr worker threads)
        for (BenchmarkWorker worker : workers) {
            worker.disconnect();
        }
    }
}