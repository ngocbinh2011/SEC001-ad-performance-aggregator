package com.infrastructure.io.reader;

import com.domain.model.CampaignStats;
import com.domain.repositories.CampaignReader;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Multi-threaded CSV reader that reads file in chunks.
 * Each thread maintains its own HashMap of campaign statistics.
 * Thread-safe aggregation happens AFTER reading completes (no synchronization during reading).
 * This design eliminates synchronization overhead for fair benchmark comparison.
 */
public class MultiThreadedCSVReader implements CampaignReader {
	private static final int BUFFER_SIZE = 65536; // 64 KB buffer
	private static final int CHUNK_SIZE = 10 * 1024 * 1024; // 10 MB chunks
	
	private final int numThreads;
	
	public MultiThreadedCSVReader(int numThreads) {
		if (numThreads < 0) {
			throw new IllegalArgumentException("Number of threads cannot be negative");
		}
		this.numThreads = numThreads;
	}
	
	@Override
	public Map<String, CampaignStats> readStats(String filename) throws IOException {
		File file = new File(filename);
		long fileSize = file.length();
		// If file is small, use single thread
		if (fileSize < CHUNK_SIZE) {
			return readFileSingleThread(filename);
		}
		// Multi-threaded approach for large files
		return readFileMultiThreaded(filename, fileSize);
	}
	
	/**
	 * Single-threaded read for small files
	 */
	private HashMap<String, CampaignStats> readFileSingleThread(String filename) throws IOException {
		HashMap<String, CampaignStats> campaignMap = new HashMap<>();
		try (BufferedReader br = new BufferedReader(new FileReader(filename), BUFFER_SIZE)) {
			String line;
			boolean firstLine = true;
			while ((line = br.readLine()) != null) {
				if (firstLine) {
					firstLine = false;
					continue;
				}
				processRow(line, campaignMap);
			}
		}
		return campaignMap;
	}
	
	/**
	 * Multi-threaded read for large files
	 */
	private Map<String, CampaignStats> readFileMultiThreaded(String filename, long fileSize) throws IOException {
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		long chunkSize = fileSize / numThreads;
		// Storage for per-thread results
		Map<String, CampaignStats>[] threadResults = new HashMap[numThreads];
		try {
			// Submit all chunk processing tasks
			for (int i = 0; i < numThreads; i++) {
				final int threadIndex = i;
				final long start = i * chunkSize;
				final long end = (i == numThreads - 1) ? fileSize : (i + 1) * chunkSize;
				executor.submit(() -> {
					try {
						threadResults[threadIndex] = processChunk(filename, start, end, fileSize, threadIndex == 0);
					} catch (IOException e) {
						System.err.println("Error processing chunk " + threadIndex + ": " + e.getMessage());
						e.printStackTrace();
						threadResults[threadIndex] = new HashMap<>();
					}
				});
			}
			executor.shutdown();
			executor.awaitTermination(1, TimeUnit.HOURS);
			// Aggregate results from all threads (AFTER all threads complete)
			return aggregateResults(threadResults);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException("Multi-threaded reading interrupted", e);
		}
	}
	
	/**
	 * Processes a chunk of the file in a separate thread.
	 * Returns its own HashMap with results (no synchronization during reading).
	 */
	private HashMap<String, CampaignStats> processChunk(String filename, long start, long end, long fileSize,
	                                                    boolean isFirstChunk) throws IOException {
		HashMap<String, CampaignStats> chunkMap = new HashMap<>();
		try (RandomAccessFile raf = new RandomAccessFile(filename, "r")) {
			raf.seek(start);
			byte[] buffer = new byte[BUFFER_SIZE];
			StringBuilder line = new StringBuilder(256);
			
			boolean skippingFirstPartialLine = !isFirstChunk;
			boolean headerSkipped = isFirstChunk ? false : true; // chunk 0 chưa skip header
			
			long currentPos = start;
			while (true) {
				int maxToRead = (int) Math.min(BUFFER_SIZE, fileSize - currentPos);
				if (maxToRead <= 0) break;
				
				int bytesRead = raf.read(buffer, 0, maxToRead);
				if (bytesRead == -1) break;
				
				for (int i = 0; i < bytesRead; i++) {
					byte b = buffer[i];
					currentPos++;
					
					// Skip partial line at the beginning of non-first chunks
					if (skippingFirstPartialLine) {
						if (b == '\n') {
							skippingFirstPartialLine = false;
						}
						continue;
					}
					
					if (b == '\n') {
						String row = cleanLine(line);
						line.setLength(0);
						
						// Skip header in first chunk
						if (!headerSkipped) {
							headerSkipped = true;
							continue;
						}
						
						if (!row.isEmpty()) {
							processRow(row, chunkMap);
						}
						
						// Nếu đã đọc quá end, nhưng vừa hoàn thành dòng → dừng
						if (currentPos >= end && !skippingFirstPartialLine) {
							return chunkMap;
						}
						
					} else {
						line.append((char) b);
					}
				}
			}
			
			// Xử lý dòng cuối nếu file không kết thúc bằng newline
			if (line.length() > 0 && headerSkipped) {
				String row = cleanLine(line);
				if (!row.isEmpty()) {
					processRow(row, chunkMap);
				}
			}
		}
		return chunkMap;
	}
	
	/**
	 * Aggregates results from all threads into a single HashMap.
	 * This is the ONLY place where cross-thread coordination happens.
	 */
	private Map<String, CampaignStats> aggregateResults(Map<String, CampaignStats>[] threadResults) {
		Map<String, CampaignStats> finalMap = new HashMap<>();
		// Merge all thread results
		for (Map<String, CampaignStats> threadMap : threadResults) {
			if (threadMap != null) {
				for (Map.Entry<String, CampaignStats> entry : threadMap.entrySet()) {
					String campaignId = entry.getKey();
					CampaignStats stats = entry.getValue();
					// Merge with final map
					finalMap.computeIfAbsent(campaignId, k -> new CampaignStats(campaignId))
						.updateStats(stats.getTotalImpressions(), stats.getTotalClicks(),
							stats.getTotalSpend(), stats.getTotalConversions());
				}
			}
		}
		return finalMap;
	}
	
	/**
	 * Processes a single CSV row and updates campaign statistics
	 */
	private void processRow(String row, Map<String, CampaignStats> campaignMap) {
		CSVRowParser.handle(row, campaignMap);
	}
	
	/**
	 * Removes trailing carriage return if present
	 */
	private String cleanLine(StringBuilder line) {
		int len = line.length();
		if (len > 0 && line.charAt(len - 1) == '\r') {
			return line.substring(0, len - 1);
		}
		return line.toString();
	}
}
