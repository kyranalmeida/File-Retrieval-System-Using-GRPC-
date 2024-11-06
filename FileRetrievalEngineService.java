package csc435.app;

import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FileRetrievalEngineService extends FileRetrievalEngineGrpc.FileRetrievalEngineImplBase {
    private IndexStore store;
    
    public FileRetrievalEngineService(IndexStore store) {
        this.store = store;
    }

    @Override
    public void computeIndex(IndexReq request, StreamObserver<IndexRep> responseObserver) {
        responseObserver.onNext(doIndex(request));
        responseObserver.onCompleted();
    }

    @Override
    public void computeSearch(SearchReq request, StreamObserver<SearchRep> responseObserver) {
        responseObserver.onNext(doSearch(request));
        responseObserver.onCompleted();
    }

    private IndexRep doIndex(IndexReq request) {
        long documentNumber = store.putDocument(request.getDocumentPath());
        HashMap<String, Long> wordFreqs = new HashMap<>(request.getWordFrequenciesMap());
        store.updateIndex(documentNumber, wordFreqs);

        // TO-DO send an OK message as the reply
        return IndexRep.newBuilder()
            .setAck("OK")
            .build();
    }


    private SearchRep doSearch(SearchReq request) {
        
        Map<String, Long> searchResults = new HashMap<>();
        
        for (String term : request.getTermsList()) {
            ArrayList<DocFreqPair> results = store.lookupIndex(term);
            for (DocFreqPair pair : results) {
                String documentPath = store.getDocument(pair.documentNumber);
                searchResults.put(documentPath, pair.wordFrequency);
            }
        }

        // TO-DO send the results as the reply message
        return SearchRep.newBuilder()
            .putAllSearchResults(searchResults)
            .build();
    }
}

