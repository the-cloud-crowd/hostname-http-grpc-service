package x.hostname.grpc.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.protobuf.Empty;

import io.grpc.stub.StreamObserver;
import x.hostname.grpc.proto.ConnectRequest;
import x.hostname.grpc.proto.CounterServiceGrpc.CounterServiceImplBase;
import x.hostname.grpc.proto.CounterUpdate;

public class CounterServiceImpl extends CounterServiceImplBase {

    private static final Logger logger = LogManager.getLogger(CounterServiceImpl.class);

    private final AtomicLong counter;
    private final Map<String, StreamObserver<CounterUpdate>> observers;

    public CounterServiceImpl() {
        this.counter = new AtomicLong(0);
        this.observers = new ConcurrentHashMap<>();
    }

    @Override
    public void connect(ConnectRequest request, StreamObserver<CounterUpdate> responseObserver) {
        logger.info("connect: {}", request.getSessionId());
        observers.put(request.getSessionId(), responseObserver);
    }

    @Override
    public void disconnect(ConnectRequest request, StreamObserver<CounterUpdate> responseObserver) {
        logger.info("disconnect: {}", request.getSessionId());
        StreamObserver<CounterUpdate> observer = observers.remove(request.getSessionId());
        observer.onCompleted();

        CounterUpdate response = CounterUpdate.newBuilder().setCount(counter.get()).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void countUp(Empty request, StreamObserver<CounterUpdate> responseObserver) {

        long count = counter.incrementAndGet();
        logger.info("countUp: {}", count);

        CounterUpdate response = CounterUpdate.newBuilder().setCount(count).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        onUpdate(response);
    }

    @Override
    public void countDown(Empty request, StreamObserver<CounterUpdate> responseObserver) {

        long count = counter.decrementAndGet();
        logger.info("countDown: {}", count);

        CounterUpdate response = CounterUpdate.newBuilder().setCount(count).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        onUpdate(response);
    }

    private synchronized void onUpdate(final CounterUpdate update) {
        observers.values().parallelStream().forEach(o -> o.onNext(update));
    }

}
