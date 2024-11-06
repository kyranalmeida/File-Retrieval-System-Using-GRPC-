package csc435.app;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.ServerBuilder;
import io.grpc.Server;

public class ServerProcessingEngine {
    private IndexStore store;
    private FileRetrievalEngineService rpcServer;
    private Server grpcServer;
    private Thread grpcServerThread;

    public ServerProcessingEngine(IndexStore store) {
        this.store = store;
    }

    // TO-DO create and start the gRPC Server
    public void initialize(int serverPort) {
        rpcServer = new FileRetrievalEngineService(store);
        grpcServerThread = new Thread(() -> {
            try {
                grpcServer = ServerBuilder.forPort(serverPort)
                    .addService(rpcServer)
                    .build();
                grpcServer.start();
                grpcServer.awaitTermination();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        grpcServerThread.start();
    }


    // TO-DO shutdown the gRPC Server
    public void shutdown() {
        if (grpcServer != null) {
            grpcServer.shutdown();
        }
        
        // TO-DO join the gRPC server thread
        if (grpcServerThread != null) {
            try {
                grpcServerThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }
    }
}