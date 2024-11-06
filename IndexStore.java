package csc435.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicLong;

// Data structure that stores a document number and the number of time a word/term appears in the document
class DocFreqPair {
    public long documentNumber;
    public long wordFrequency;

    public DocFreqPair(long documentNumber, long wordFrequency) {
        this.documentNumber = documentNumber;
        this.wordFrequency = wordFrequency;
    }
}

public class IndexStore {
    private HashMap<Long, String> documentMap;
    private HashMap<String, ArrayList<DocFreqPair>> termInvertedIndex;
    private final ReentrantLock documentMapLock;
    private final ReentrantLock termInvertedIndexLock;
    private AtomicLong documentCounter;
    
    public IndexStore() {
        documentMap = new HashMap<>();
        termInvertedIndex = new HashMap<>();
        documentMapLock = new ReentrantLock();
        termInvertedIndexLock = new ReentrantLock();
        documentCounter = new AtomicLong(0);
    }

    public long putDocument(String documentPath) {
        long documentNumber = 0;
        documentMapLock.lock();
        try {
            documentNumber = documentCounter.incrementAndGet();
            documentMap.put(documentNumber, documentPath);
        } finally {
            documentMapLock.unlock();
        }
        return documentNumber;
    }


    public String getDocument(long documentNumber) {
        String documentPath = "";
        documentMapLock.lock();
        try {
            documentPath = documentMap.getOrDefault(documentNumber, "");
        } finally {
            documentMapLock.unlock();
        }
        return documentPath;
    }


    public void updateIndex(long documentNumber, HashMap<String, Long> wordFrequencies) {
        termInvertedIndexLock.lock();
        try {
            for (HashMap.Entry<String, Long> entry : wordFrequencies.entrySet()) {
                String term = entry.getKey();
                Long frequency = entry.getValue();
                
                if (!termInvertedIndex.containsKey(term)) {
                    termInvertedIndex.put(term, new ArrayList<>());
                }
                
                ArrayList<DocFreqPair> docFreqPairs = termInvertedIndex.get(term);
                boolean found = false;
                for (DocFreqPair pair : docFreqPairs) {
                    if (pair.documentNumber == documentNumber) {
                        pair.wordFrequency = frequency;
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    docFreqPairs.add(new DocFreqPair(documentNumber, frequency));
                }
            }
        } finally {
            termInvertedIndexLock.unlock();
        }
    }

    public ArrayList<DocFreqPair> lookupIndex(String term) {
        ArrayList<DocFreqPair> results = new ArrayList<>();
        termInvertedIndexLock.lock();
        try {
            if (termInvertedIndex.containsKey(term)) {
                results = new ArrayList<>(termInvertedIndex.get(term));
            }
        } finally {
            termInvertedIndexLock.unlock();
        }
        return results;
    }
}
