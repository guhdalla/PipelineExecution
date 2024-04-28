package com.pipelineexecution.pipe;

import com.pipelineexecution.pipe.functional.PipeFunctional.PipeCallable;
import com.pipelineexecution.pipe.functional.PipeFunctional.PipeConsumer;
import com.pipelineexecution.pipe.functional.PipeFunctional.PipeFunction;
import com.pipelineexecution.pipe.functional.PipeFunctional.PipeLambda;
import com.pipelineexecution.pipe.functional.PipeFunctional.PipeRunnable;
import java.util.concurrent.StructuredTaskScope.ShutdownOnFailure;
import java.util.concurrent.StructuredTaskScope.Subtask;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


import static java.util.concurrent.StructuredTaskScope.Subtask.State.SUCCESS;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class PipeTask {

    private Subtask<?> subtask;

    protected static PipeTask execute(ShutdownOnFailure scope, PipeLambda lambda, PipeExecution<?> previousSyncPipe) {


        Subtask<?> subtask = switch (lambda) {

            case PipeCallable<?> callable -> scope.fork(callable::run);

            case PipeRunnable runnable -> scope.fork(() -> {
                runnable.run();
                return null;
            });

            case PipeConsumer consumer -> scope.fork(() -> {

                consumer.run(previousSyncPipe.result());
                return null;
            });

            case PipeFunction function -> scope.fork(() -> function.run(previousSyncPipe.result()));

            default -> throw new IllegalArgumentException("Unsupported lambda type: " + lambda.getClass());
        };

        return new PipeTask(subtask);
    }

    protected Object result() {
        return subtask.get();
    }

    public boolean isDone() {
        return subtask.state() == SUCCESS;
    }
}
