package com.infrastructure.benchmark;

import com.domain.repositories.CampaignReader;
import com.infrastructure.io.reader.BufferedCSVReader;
import com.infrastructure.io.reader.MultiThreadedCSVReader;
import com.infrastructure.io.reader.StreamCSVReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Detailed benchmark for CSV readers.
 * Measures execution time and memory usage across multiple runs and computes averages.
 *
 * Metrics collected:
 * - Execution time (milliseconds)
 * - Memory usage before and after (MB)
 * - Peak memory usage during execution
 * - Average and standard deviation across runs
 */
public class ConsoleBenchmark {
	
	private static final String FILENAME = "ad_data.csv";
	private static final int WARMUP_RUNS = 2;
	private static final int MEASUREMENT_RUNS = 5;
	
	/**
	 * Container for benchmark results
	 */
	static class BenchmarkResult {
		String name;
		List<Long> executionTimes = new ArrayList<>();
		List<Long> memoryUsages = new ArrayList<>();
		List<Long> peakMemories = new ArrayList<>();
		
		BenchmarkResult(String name) {
			this.name = name;
		}
		
		void addRun(long executionTime, long memoryUsage, long peakMemory) {
			executionTimes.add(executionTime);
			memoryUsages.add(memoryUsage);
			peakMemories.add(peakMemory);
		}
		
		double getAverageExecutionTime() {
			return executionTimes.stream().mapToLong(Long::longValue).average().orElse(0);
		}
		
		double getAverageMemoryUsage() {
			return memoryUsages.stream().mapToLong(Long::longValue).average().orElse(0);
		}
		
		double getAveragePeakMemory() {
			return peakMemories.stream().mapToLong(Long::longValue).average().orElse(0);
		}
		
		double getStdDevExecutionTime() {
			double avg = getAverageExecutionTime();
			double variance = executionTimes.stream()
				.mapToDouble(t -> Math.pow(t - avg, 2))
				.average()
				.orElse(0);
			return Math.sqrt(variance);
		}
		
		double getStdDevMemoryUsage() {
			double avg = getAverageMemoryUsage();
			double variance = memoryUsages.stream()
				.mapToDouble(m -> Math.pow(m - avg, 2))
				.average()
				.orElse(0);
			return Math.sqrt(variance);
		}
		
		double getMinExecutionTime() {
			return executionTimes.stream().mapToLong(Long::longValue).min().orElse(0);
		}
		
		double getMaxExecutionTime() {
			return executionTimes.stream().mapToLong(Long::longValue).max().orElse(0);
		}
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println("=".repeat(80));
		System.out.println("CSV Reader Performance Benchmark");
		System.out.println("=".repeat(80));
		System.out.println("File: " + FILENAME);
		System.out.println("Warmup runs: " + WARMUP_RUNS);
		System.out.println("Measurement runs: " + MEASUREMENT_RUNS);
		System.out.println("=".repeat(80));
		System.out.println();
		
		List<BenchmarkResult> results = new ArrayList<>();
		
		// Benchmark each reader
		results.add(benchmarkReader("BufferedCSVReader", new BufferedCSVReader()));
		results.add(benchmarkReader("StreamCSVReader", new StreamCSVReader()));
		results.add(benchmarkReader("MultiThreadedCSVReader (4 threads)", new MultiThreadedCSVReader(4)));
		results.add(benchmarkReader("MultiThreadedCSVReader (8 threads)", new MultiThreadedCSVReader(8)));
		results.add(benchmarkReader("MultiThreadedCSVReader (16 threads)", new MultiThreadedCSVReader(16)));
		
		// Print results
		printResults(results);
		
		// Print comparison table
		printComparisonTable(results);
	}
	
	/**
	 * Run benchmark for a single reader
	 */
	private static BenchmarkResult benchmarkReader(String name, CampaignReader reader) throws IOException {
		System.out.println("Benchmarking " + name + "...");
		BenchmarkResult result = new BenchmarkResult(name);
		
		// Warmup runs
		System.out.print("  Warmup: ");
		for (int i = 0; i < WARMUP_RUNS; i++) {
			runBenchmarkIteration(reader);
			System.out.print(".");
		}
		System.out.println(" done");
		
		// Measurement runs
		System.out.print("  Measuring: ");
		for (int i = 0; i < MEASUREMENT_RUNS; i++) {
			// Force garbage collection before measurement
			System.gc();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			
			// Get initial memory state
			Runtime runtime = Runtime.getRuntime();
			long memBefore = runtime.totalMemory() - runtime.freeMemory();
			long peakMemory = memBefore;
			
			// Time the execution
			long startTime = System.nanoTime();
			Map<String, ?> data = reader.readStats(FILENAME);
			long endTime = System.nanoTime();
			
			// Get final memory state
			runtime.gc();
			long memAfter = runtime.totalMemory() - runtime.freeMemory();
			peakMemory = Math.max(peakMemory, memAfter);
			
			long executionTimeMs = (endTime - startTime) / 1_000_000;
			long memoryUsageMb = (memAfter - memBefore) / 1024 / 1024;
			long peakMemoryMb = peakMemory / 1024 / 1024;
			
			result.addRun(executionTimeMs, Math.max(0, memoryUsageMb), peakMemoryMb);
			System.out.print(".");
		}
		System.out.println(" done");
		System.out.println();
		
		return result;
	}
	
	/**
	 * Run a single benchmark iteration (used for warmup)
	 */
	private static void runBenchmarkIteration(CampaignReader reader) throws IOException {
		reader.readStats(FILENAME);
	}
	
	/**
	 * Print detailed results for each reader
	 */
	private static void printResults(List<BenchmarkResult> results) {
		System.out.println("DETAILED RESULTS");
		System.out.println("=".repeat(80));
		
		for (BenchmarkResult result : results) {
			System.out.println("\n" + result.name);
			System.out.println("-".repeat(80));
			
			System.out.println("Execution Time (ms):");
			System.out.printf("  Average:        %.2f%n", result.getAverageExecutionTime());
			System.out.printf("  Min:            %.2f%n", result.getMinExecutionTime());
			System.out.printf("  Max:            %.2f%n", result.getMaxExecutionTime());
			System.out.printf("  Std Dev:        %.2f%n", result.getStdDevExecutionTime());
			
			System.out.println("Memory Usage (MB):");
			System.out.printf("  Average:        %.2f%n", result.getAverageMemoryUsage());
			System.out.printf("  Std Dev:        %.2f%n", result.getStdDevMemoryUsage());
			System.out.printf("  Avg Peak:       %.2f%n", result.getAveragePeakMemory());
			
			System.out.println("Raw Data (ms / MB / Peak MB):");
			for (int i = 0; i < result.executionTimes.size(); i++) {
				System.out.printf("  Run %d: %d ms / %d MB / %d MB%n",
					i + 1,
					result.executionTimes.get(i),
					result.memoryUsages.get(i),
					result.peakMemories.get(i));
			}
		}
		System.out.println();
	}
	
	/**
	 * Print comparison table
	 */
	private static void printComparisonTable(List<BenchmarkResult> results) {
		System.out.println("COMPARISON TABLE");
		System.out.println("=".repeat(100));
		System.out.printf("%-40s | %15s | %15s | %15s | %15s%n",
			"Reader", "Avg Time (ms)", "Std Dev (ms)", "Avg Memory (MB)", "Avg Peak (MB)");
		System.out.println("-".repeat(100));
		
		for (BenchmarkResult result : results) {
			System.out.printf("%-40s | %15.2f | %15.2f | %15.2f | %15.2f%n",
				result.name,
				result.getAverageExecutionTime(),
				result.getStdDevExecutionTime(),
				result.getAverageMemoryUsage(),
				result.getAveragePeakMemory());
		}
		
		System.out.println("=".repeat(100));
		System.out.println();
		
		// Find and display the fastest reader
		if (!results.isEmpty()) {
			BenchmarkResult fastest = results.stream()
				.min((a, b) -> Double.compare(a.getAverageExecutionTime(), b.getAverageExecutionTime()))
				.orElse(null);
			
			if (fastest != null) {
				System.out.println("FASTEST READER: " + fastest.name);
				System.out.printf("Average execution time: %.2f ms%n", fastest.getAverageExecutionTime());
				System.out.println();
			}
		}
	}
}

