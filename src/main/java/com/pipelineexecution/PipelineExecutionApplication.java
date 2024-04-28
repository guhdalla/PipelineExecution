package com.pipelineexecution;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PipelineExecutionApplication {

	public static void main(String[] args) {
		SpringApplication.run(PipelineExecutionApplication.class, args);

		exemple();
	}

	private static void exemple() {

		Integer result = PipelineExecution.init()
				.inParallel(() -> countTo(3))
				.inParallel(() -> countTo(3))
				.inOrderAndGetResult(() -> sum(1, 2))
				.consumeInOrderAndGetResult(o -> sub(o, 2))
				.consumeInOrderAndGetResult(PipelineExecutionApplication::print)
				.execute()
				.result();

		System.out.println("Result: " + result);
	}

	private static Integer sum(Integer a, Integer b) {
		System.out.println(STR."Sum: \{a} + \{b}");
		sleep(1);
		return a + b;
	}

	private static Integer sub(Integer a, Integer b) {
		System.out.println(STR."Sub: \{a} - \{b}");
		sleep(1);
		return a - b;
	}

	private static Integer print(Integer message) {
		System.out.println(STR."Print: \{message}");
		sleep(1);
		return message;
	}

	private static void countTo(Integer number) {
		for (int i = 1; i < number + 1; i++) {
			System.out.println(STR."Count: \{i}");
			sleep(1);
		}
	}

	private static void sleep(Integer seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
	}
}
