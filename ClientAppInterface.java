package csc435.app;

import java.lang.System;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientAppInterface {
    private ClientProcessingEngine engine;

    public ClientAppInterface(ClientProcessingEngine engine) {
        this.engine = engine;

        System.out.println("Client interface initialized");
        System.out.println("Available commands:");
        System.out.println("  connect <serverIP> <serverPort>");
        System.out.println("  index <folderPath>");
        System.out.println("  search <term1> [term2] ...");
        System.out.println("  quit");
    }

    public void readCommands() {
        // TO-DO implement the read commands method
        Scanner sc = new Scanner(System.in);
        String command;
        
        while (true) {
            System.out.print("> ");
            
            // read from command line
            command = sc.nextLine();

            // if the command is quit, terminate the program       
            if (command.compareTo("quit") == 0) {
                break;
            }

            // if the command begins with connect, connect to the given server
            if (command.length() >= 7 && command.substring(0, 7).compareTo("connect") == 0) {
                String[] parts = command.split("\\s+");
                if (parts.length != 3) {
                    System.out.println("Usage: connect <serverIP> <serverPort>");
                    continue;
                }
                engine.connect(parts[1], parts[2]);
                System.out.println("Connected to server " + parts[1] + ":" + parts[2]);
                continue;
            }
            
            // if the command begins with index, index the files from the specified directory
            if (command.length() >= 5 && command.substring(0, 5).compareTo("index") == 0) {
                String[] parts = command.split("\\s+");
                if (parts.length != 2) {
                    System.out.println("Usage: index <folderPath>");
                    continue;
                }
                IndexResult result = engine.indexFiles(parts[1]);
                System.out.println("Indexing completed in " + result.executionTime + " seconds");
                System.out.println("Total bytes processed: " + result.totalBytesRead);
                continue;
            }

            // if the command begins with search, search for files that matches the query
            if (command.length() >= 6 && command.substring(0, 6).compareTo("search") == 0) {
                String[] parts = command.split("\\s+");
                if (parts.length < 2) {
                    System.out.println("Usage: search <term1> [term2] ...");
                    continue;
                }
                ArrayList<String> terms = new ArrayList<>(Arrays.asList(parts).subList(1, parts.length));
                SearchResult result = engine.searchFiles(terms);
                System.out.println("Search completed in " + result.excutionTime + " seconds");
                System.out.println("Top matching documents:");
                for (DocPathFreqPair pair : result.documentFrequencies) {
                    System.out.println(pair.documentPath + ": " + pair.wordFrequency + " occurrences");
                }
                continue;
            }

            System.out.println("unrecognized command!");
        }

        sc.close();
    }
}
