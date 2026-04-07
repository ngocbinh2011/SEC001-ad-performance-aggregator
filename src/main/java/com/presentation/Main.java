import com.domain.model.CampaignStats;
import com.domain.repositories.CampaignReader;
import com.infrastructure.io.reader.BufferedCSVReader;
import com.infrastructure.io.reader.MultiThreadedCSVReader;
import com.infrastructure.io.reader.StreamCSVReader;
import com.infrastructure.io.writer.CSVResultWriter;
import com.presentation.CommandLineParser;
import com.usecase.DataAggregator;
import com.usecase.ResultsGenerator;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Main entry point for the Ad Performance Aggregator application.
 *
 * Supported options:
 * --input <file>
 * --output <directory>
 * --reader <buffer|stream|thread>
 * --threads <number>   (used only when --reader thread)
 */
public class Main {
	
	public static void main(String[] args) {
		// Parse command-line arguments
		CommandLineParser parser = new CommandLineParser(args);
		
		// If you want to force CLI validation, uncomment this
		if (!parser.isValid()) {
			printUsage();
			System.exit(1);
		}
		
		// Defaults for testing
//		String inputFile = parser.getInputFile() != null ? parser.getInputFile() : "ad_data.csv";
//		String outputDir = parser.getOutputDirectory() != null ? parser.getOutputDirectory() : "results";
//		String readerType = parser.getReaderType() != null ? parser.getReaderType() : "thread";
//		int numThreads = parser.getNumThreads() > 0
//			? parser.getNumThreads()
//			: Runtime.getRuntime().availableProcessors();
		
		String inputFile = "";
		String outputDir = "output";
		String readerType = "thread";
		int numThreads = 16;
		outputDir += readerType + "_" + numThreads;
		// Validate input file exists
		if (!new File(inputFile).exists()) {
			System.out.println("Error: File not found: " + inputFile);
			System.exit(1);
		}
		
		// Create output directory if it doesn't exist
		File outDir = new File(outputDir);
		if (!outDir.exists()) {
			outDir.mkdirs();
		}
		
		long startTime = System.currentTimeMillis();
		
		try {
			// Select CSV reader implementation
			CampaignReader reader = createCSVReader(readerType, numThreads);
			
			DataAggregator aggregator = new DataAggregator(reader);
			
			System.out.println("======================================");
			System.out.println("Ad Performance Aggregator");
			System.out.println("======================================");
			System.out.println("Input file   : " + inputFile);
			System.out.println("Output dir   : " + outputDir);
			System.out.println("Reader type  : " + readerType);
			if ("thread".equalsIgnoreCase(readerType)) {
				System.out.println("Threads      : " + numThreads);
			}
			System.out.println("======================================");
			
			System.out.println("Reading and aggregating data...");
			aggregator.aggregate(inputFile);
			
			long aggregationTime = System.currentTimeMillis();
			System.out.println("✓ Aggregation completed in " + (aggregationTime - startTime) + " ms");
			
			// Get aggregated data
			Map<String, CampaignStats> campaignMap = aggregator.getCampaignMap();
			System.out.println("✓ Total campaigns: " + campaignMap.size());
			
			// Generate results
			System.out.println("\nGenerating top 10 lists...");
			List<CampaignStats> top10CTR = ResultsGenerator.getTop10ByCTR(campaignMap);
			List<CampaignStats> top10CPA = ResultsGenerator.getTop10ByCPA(campaignMap);
			
			// Write results to CSV files
			CSVResultWriter.write(top10CTR, outputDir + "/top10_ctr.csv", "CTR");
			CSVResultWriter.write(top10CPA, outputDir + "/top10_cpa.csv", "CPA");
			
			long endTime = System.currentTimeMillis();
			System.out.println("\n✓ Total processing time: " + (endTime - startTime) + " ms");
			System.out.println("✓ Results written to " + outputDir);
			
		} catch (IOException e) {
			System.err.println("IO Error: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		} catch (IllegalArgumentException e) {
			System.err.println("Argument Error: " + e.getMessage());
			printUsage();
			System.exit(1);
		}
	}
	
	/**
	 * Factory method to create the appropriate CSVReader implementation.
	 */
	private static CampaignReader createCSVReader(String readerType, int numThreads) {
		if (readerType == null || readerType.isEmpty()) {
			return new BufferedCSVReader();
		}
		
		switch (readerType.toLowerCase()) {
			case "buffer":
				return new BufferedCSVReader();
			case "stream":
				return new StreamCSVReader();
			case "thread":
				return new MultiThreadedCSVReader(numThreads);
			default:
				throw new IllegalArgumentException(
					"Unsupported reader type: " + readerType +
						". Supported values: buffer, stream, thread"
				);
		}
	}
	
	private static void printUsage() {
		System.out.println("Usage: java Main --input <file> [options]");
		System.out.println();
		System.out.println("Required:");
		System.out.println("  --input <file>              Path to the input CSV file");
		System.out.println();
		System.out.println("Optional:");
		System.out.println("  --output <directory>        Output directory (default: results/)");
		System.out.println("  --reader <buffer|stream|thread>");
		System.out.println("                              CSV reading strategy (default: buffer)");
		System.out.println("  --threads <number>          Number of threads (only for --reader thread)");
		System.out.println("                              Default: available processors");
		System.out.println();
		System.out.println("Examples:");
		System.out.println("  java Main --input ad_data.csv --reader buffer");
		System.out.println("  java Main --input ad_data.csv --reader stream");
		System.out.println("  java Main --input ad_data.csv --reader thread --threads 8");
	}
}