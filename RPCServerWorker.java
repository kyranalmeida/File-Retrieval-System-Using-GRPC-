package csc435.app;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RPCServerWorker implements Runnable {
    private IndexStore store;
    private Server grpcServer;

    public RPCServerWorker(IndexStore store) {
        this.store = store;
    }

    @Override
    public void run() {
        FileRetrievalEngineService service = new FileRetrievalEngineService(store);
        grpcServer = ServerBuilder.forPort(50051)
            .addService(service)
            .build();

        try {
            grpcServer.start();
            grpcServer.awaitTermination();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    public void shutdown() {
        if (grpcServer != null) {
            try {
                grpcServer.shutdown();
                if (!grpcServer.awaitTermination(30, TimeUnit.SECONDS)) {
                    grpcServer.shutdownNow();
                }
            } catch (InterruptedException e) {
                grpcServer.shutdownNow();
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }
    }
}