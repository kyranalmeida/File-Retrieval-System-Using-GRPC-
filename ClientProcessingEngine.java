package csc435.app;

import csc435.app.FileRetrievalEngineGrpc.FileRetrievalEngineBlockingStub;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.regex.Pattern;
import java.util.Map;

class IndexResult {
    public double executionTime;
    public long totalBytesRead;

    public IndexResult(double executionTime, long totalBytesRead) {
        this.executionTime = executionTime;
        this.totalBytesRead = totalBytesRead;
    }
}

class DocPathFreqPair {
    public String documentPath;
    public long wordFrequency;

    public DocPathFreqPair(String documentPath, long wordFrequency) {
        this.documentPath = documentPath;
        this.wordFrequency = wordFrequency;
    }
}

class SearchResult {
    public double excutionTime;
    public ArrayList<DocPathFreqPair> documentFrequencies;

    public SearchResult(double executionTime, ArrayList<DocPathFreqPair> documentFrequencies) {
        this.excutionTime = executionTime;
        this.documentFrequencies = documentFrequencies;
    }
}

public class ClientProcessingEngine {
    // TO-DO keep track of the connection
    ManagedChannel channel;
    FileRetrievalEngineBlockingStub stub;

    public ClientProcessingEngine() { }

    public IndexResult indexFiles(String folderPath) {
        IndexResult result = new IndexResult(0.0, 0);

        // TO-DO get the start time
        long startTime = System.currentTimeMillis();

        try {
            // TO-DO crawl the folder path and extract all file paths
            ArrayList<Path> filePaths = Files.walk(Paths.get(folderPath))
                .filter(Files::isRegularFile)
                .collect(Collectors.toCollection(ArrayList::new));

            long totalBytes = 0;
            Pattern wordPattern = Pattern.compile("[a-zA-Z0-9]{3,}");

            for (Path filePath : filePaths) {
                // TO-DO for each file extract all alphanumeric terms that are larger than 2 characters
                // and count their frequencies
                String content = Files.readString(filePath);
                totalBytes += content.length();

                HashMap<String, Long> wordFreqs = new HashMap<>();
                wordPattern.matcher(content)
                    .results()
                    .map(match -> match.group().toLowerCase())
                    .forEach(word -> wordFreqs.merge(word, 1L, Long::sum));

                // TO-DO increment the total number of bytes read
                result.totalBytesRead += totalBytes;

                // TO-DO for each file perform a remote procedure call to the server by calling the gRPC client stub
                IndexReq request = IndexReq.newBuilder()
                    .setDocumentPath(filePath.toString())
                    .putAllWordFrequencies(wordFreqs)
                    .build();

                IndexRep response = stub.computeIndex(request);
                if (!"OK".equals(response.getAck())) {
                    System.err.println("Failed to index file: " + filePath);
                }
            }

            // TO-DO get the stop time and calculate the execution time
            long endTime = System.currentTimeMillis();
            result.executionTime = (endTime - startTime) / 1000.0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        // TO-DO return the execution time and the total number of bytes read
        return result;
    }
    
    public SearchResult searchFiles(ArrayList<String> terms) {
        SearchResult result = new SearchResult(0.0, new ArrayList<DocPathFreqPair>());

        // TO-DO get the start time
        long startTime = System.currentTimeMillis();

        // TO-DO perform a remote procedure call to the server by calling the gRPC client stub
        SearchReq request = SearchReq.newBuilder()
            .addAllTerms(terms)
            .build();

        SearchRep response = stub.computeSearch(request);

        // TO-DO get the stop time and calculate the execution time
        long endTime = System.currentTimeMillis();
        result.excutionTime = (endTime - startTime) / 1000.0;

        // TO-DO return the execution time and the top 10 documents and frequencies
        Map<String, Long> searchResults = response.getSearchResultsMap();
        searchResults.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .forEach(entry -> 
                result.documentFrequencies.add(new DocPathFreqPair(entry.getKey(), entry.getValue()))
            );

        return result;
    }

    public void connect(String serverIP, String serverPort) {
        // TO-DO create communication channel with the gRPC Server
        channel = Grpc.newChannelBuilder(serverIP + ":" + serverPort, InsecureChannelCredentials.create())
            .build();

        // TO-DO create gRPC client stub
        stub = FileRetrievalEngineGrpc.newBlockingStub(channel);
    }
}