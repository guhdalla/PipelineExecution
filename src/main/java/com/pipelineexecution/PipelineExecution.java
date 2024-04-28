package com.pipelineexecution;

import com.pipelineexecution.pipe.PipeExecution;
import com.pipelineexecution.pipe.enums.PipeExecutionType;
import com.pipelineexecution.pipe.functional.PipeFunctional;
import com.pipelineexecution.pipe.functional.PipeFunctional.PipeCallable;
import com.pipelineexecution.pipe.functional.PipeFunctional.PipeConsumer;
import com.pipelineexecution.pipe.functional.PipeFunctional.PipeFunction;
import com.pipelineexecution.pipe.functional.PipeFunctional.PipeRunnable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope.ShutdownOnFailure;


import static com.pipelineexecution.pipe.enums.PipeExecutionType.ASYNC;
import static com.pipelineexecution.pipe.enums.PipeExecutionType.SYNC;


public class PipelineExecution<R> {

    private List<PipeExecution<?>> pipes;

    public PipelineExecution() {
        this.pipes = new ArrayList<>();
    }

    private PipelineExecution(List<PipeExecution<?>> pipes) {
        this.pipes = pipes;
    }

    public static PipelineExecution<Void> init() {
        return new PipelineExecution<>();
    }

    public PipelineExecution<R> execute() {


        try (var scope = new ShutdownOnFailure()) {

            PipeExecutionType lastType = null;
            PipeExecution<?> lastSyncPipe = null;

            for (PipeExecution<?> pipe : pipes) {

                if (lastSyncPipe != null) pipe.setPreviousSyncPipe(lastSyncPipe);
                PipeExecutionType type = pipe.getType();

                switch (type) {
                    case SYNC -> {

                        if (lastType == ASYNC) scope.join().throwIfFailed();

                        pipe.fork(scope);
                        scope.join().throwIfFailed();

                        lastSyncPipe = pipe;
                    }
                    case ASYNC -> pipe.fork(scope);
                    default -> throw new IllegalArgumentException("Unsupported pipe type: " + type);
                }

                lastType = type;
            }
            scope.join().throwIfFailed();
            return this;
        } catch (ExecutionException | InterruptedException e) {

            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public R result() {
        return (R) pipes.get(pipes.size() - 1).result();
    }

    public PipelineExecution<R> inOrder(PipeRunnable runnable) {
        pipes.add(new PipeExecution<>(SYNC, runnable));
        return this;
    }

    public <O> PipelineExecution<O> inOrderAndGetResult(PipeCallable<O> callable) {
        pipes.add(new PipeExecution<>(SYNC, callable));
        return new PipelineExecution<>(pipes);
    }

    public PipelineExecution<Void> consumeInOrder(PipeConsumer<R> consumer) {
        pipes.add(new PipeExecution<>(SYNC, consumer));
        return new PipelineExecution<>(pipes);
    }

    public <O> PipelineExecution<O> consumeInOrderAndGetResult(PipeFunction<R, O> function) {
        pipes.add(new PipeExecution<>(SYNC, function));
        return new PipelineExecution<>(pipes);
    }

    public PipelineExecution<R> inParallel(PipeRunnable runnable) {
        pipes.add(new PipeExecution<>(ASYNC, runnable));
        return this;
    }
}
