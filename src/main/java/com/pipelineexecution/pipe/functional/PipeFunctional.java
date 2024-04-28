package com.pipelineexecution.pipe.functional;

public final class PipeFunctional {

    private PipeFunctional() {
    }

    public interface PipeLambda {
    }

    @FunctionalInterface
    public interface PipeRunnable extends PipeLambda, Runnable {
        void run();
    }

    @FunctionalInterface
    public interface PipeCallable<O> extends PipeLambda {
        O run();
    }

    @FunctionalInterface
    public interface PipeConsumer<I> extends PipeLambda {
        void run(I i);
    }

    @FunctionalInterface
    public interface PipeFunction<I, O> extends PipeLambda {
        O run(I i);
    }
}
