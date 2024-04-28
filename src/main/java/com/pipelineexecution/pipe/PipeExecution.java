package com.pipelineexecution.pipe;

import com.pipelineexecution.pipe.enums.PipeExecutionType;
import com.pipelineexecution.pipe.functional.PipeFunctional.PipeLambda;
import java.util.concurrent.StructuredTaskScope.ShutdownOnFailure;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class PipeExecution<O> {

    @Getter
    private final PipeExecutionType type;
    private final PipeLambda lambda;
    private PipeTask task;
    @Setter
    private PipeExecution<?> previousSyncPipe;

    public void fork(ShutdownOnFailure scope) {
        this.task = PipeTask.execute(scope, lambda, previousSyncPipe);
    }

    public O result() {
        return (O) task.result();
    }

    public boolean isDone() {
        return task.isDone();
    }
}
