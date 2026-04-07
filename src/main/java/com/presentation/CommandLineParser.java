package com.presentation;

/**
 * Parses and validates command-line arguments.
 * Provides input file path and output directory configuration.
 */
public class CommandLineParser {
	private String inputFile;
	private String outputDirectory;
	
	/**
	 * Parses command-line arguments.
	 * Expected format: --input <file> [--output <directory>]
	 *
	 * @param args command-line arguments
	 */
	public CommandLineParser(String[] args) {
		this.outputDirectory = "results/"; // default output directory
		for (int i = 0; i < args.length - 1; i++) {
			if ("--input".equals(args[i])) {
				this.inputFile = args[i + 1];
			} else if ("--output".equals(args[i])) {
				this.outputDirectory = args[i + 1];
			}
		}
	}
	
	/**
	 * Gets the input file path.
	 */
	public String getInputFile() {
		return inputFile;
	}
	
	/**
	 * Gets the output directory path.
	 */
	public String getOutputDirectory() {
		return outputDirectory;
	}
	
	/**
	 * Validates that required arguments are provided.
	 *
	 * @return true if input file is specified
	 */
	public boolean isValid() {
		return inputFile != null && !inputFile.trim().isEmpty();
	}
}
