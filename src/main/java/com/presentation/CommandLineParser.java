package com.presentation;

/**
 * Parses and validates command-line arguments.
 * Provides input file path, output directory, reader type, and thread count configuration.
 */
public class CommandLineParser {
	private String inputFile;
	private String outputDirectory;
	private String readerType = "thread"; // default reader type
	private int numThreads = 8; // default threads
	
	/**
	 * Parses command-line arguments.
	 * Expected format:
	 *   --input <file>
	 *   [--output <directory>]
	 *   [--reader <buffer|stream|thread>]
	 *   [--threads <number>]
	 *
	 * @param args command-line arguments
	 */
	public CommandLineParser(String[] args) {
		this.outputDirectory = "results/"; // default output directory
		
		for (int i = 0; i < args.length - 1; i++) {
			switch (args[i]) {
				case "--input":
					this.inputFile = args[i + 1];
					break;
				case "--output":
					this.outputDirectory = args[i + 1];
					break;
				case "--reader":
					this.readerType = args[i + 1].toLowerCase();
					break;
				case "--threads":
					try {
						this.numThreads = Integer.parseInt(args[i + 1]);
						if (numThreads < 1) {
							throw new IllegalArgumentException("Number of threads must be >= 1");
						}
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException("Invalid number for --threads: " + args[i + 1]);
					}
					break;
			}
		}
	}
	
	public String getInputFile() {
		return inputFile;
	}
	
	public String getOutputDirectory() {
		return outputDirectory;
	}
	
	public String getReaderType() {
		return readerType;
	}
	
	public int getNumThreads() {
		return numThreads;
	}
	
	public boolean isValid() {
		return inputFile != null && !inputFile.trim().isEmpty();
	}
}