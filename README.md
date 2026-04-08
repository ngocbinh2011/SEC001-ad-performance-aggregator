# Ad Performance Aggregator
<p style="color:#ff6200;"> Author: Binh Can Ngoc

A high-performance Java application for processing large CSV datasets (~1GB) containing advertising performance metrics. The application aggregates campaign statistics and generates top 10 reports by Click-Through Rate (CTR) and Cost Per Action (CPA).
<p style="color:#ff0033;">
<strong>Note:</strong> Due to the limited time available, I prioritized <strong>performance</strong> and <strong>memory efficiency</strong>.
As a result, some parts of the implementation may not be as fully refined or clean as they could be under normal development conditions.
This was a deliberate trade-off to focus on the core technical requirements of the challenge.
</p>

<p style="color:#ff0062;font-size:19px; font-weight: bold;">
<strong>Important:</strong> You should run with args param `--reader thread` for better performance (details in CLI Arguments & Performance Metrics sections)
</p>
## 📋 Table of Contents

- [Features](#features)
- [Setup Instructions](#setup-instructions)
- [How to Run](#how-to-run)
- [Libraries Used](#libraries-used)
- [Performance Metrics](#performance-metrics)
- [Architecture](#architecture)
- [Memory Optimization](#memory-optimization)
- [Performance Optimization: Priority Queue](#performance-optimization-priority-queue)
- [Benchmark Analysis](#benchmark-analysis)

---

## ✨ Features

- ✅ **Large Dataset Handling**: Processes 1GB+ CSV files efficiently
- ✅ **Multiple CSV Reading Strategies**: BufferedReader, Stream-based, Multi-threaded
- ✅ **Optimized Aggregation**: Using HashMaps for O(1) lookups
- ✅ **Priority Queue Optimization**: For top-K selection without full sorting
- ✅ **Clean Architecture**: Clear separation of concerns with domain/infrastructure layers
- ✅ **Configurable Processing**: Choose reader strategy via CLI arguments
- ✅ **Docker Support**: Pre-configured Docker setup for easy deployment
- ✅ **Comprehensive Benchmarking**: JMH and custom benchmark tools included

---

## 🚀 Setup Instructions

### Prerequisites

- **Java 11+** (OpenJDK or Oracle JDK)
- **Maven 3.6+**
- **Docker** (optional, for containerized deployment)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd ad-performance-aggregator
   ```

2. **Download the dataset**
   - Use `sample_data_test.csv` for test, or use the provided `ad_data.csv.zip` for full performance testing

3. **Build the project**
   ```bash
   mvn clean package
   ```

---

## 🏃 How to Run

### 1. Running with Maven

#### Default (MultiThreadedCSVReader)
```bash
mvn exec:java -Dexec.mainClass="com.presentation.Main" \
  -Dexec.args="--input ad_data.csv --output results/"
```

#### Using StreamCSVReader
```bash
mvn exec:java -Dexec.mainClass="com.presentation.Main" \
  -Dexec.args="--input ad_data.csv --reader stream --output results/"
```

#### Using MultiThreadedCSVReader (8 threads)
```bash
mvn exec:java -Dexec.mainClass="com.presentation.Main" \
  -Dexec.args="--input ad_data.csv --reader thread --threads 8 --output results/"
```

### 2. Running with Docker

#### Build the Docker image
```bash
docker build -t ad-aggregator:latest .
```

#### Run the container
```bash
docker run --rm -v $(pwd):/data ad-aggregator:latest \
  --input /data/ad_data.csv --output /data/results/ --reader thread --threads 8
```

### 3. Running from JAR directly
```bash
java -cp target/ad-performance-aggregator-1.0-SNAPSHOT.jar \
  com.presentation.Main --input ad_data.csv --output results/
```

### 4. Compare with correct result
```bash
diff --strip-trailing-cr correct_results/top10_ctr.csv <output_dir>/top10_ctr.csv
```


### CLI Arguments

| Argument | Description | Default      | Example |
|----------|-------------|--------------|---------|
| `--input` | Path to CSV file | **Required** | `--input ad_data.csv` |
| `--output` | Output directory | `results/`   | `--output ./output/` |
| `--reader` | Reader type: `buffer`, `stream`, `thread` | `thread`     | `--reader thread` |
| `--threads` | Number of threads (for `thread` reader) | CPU cores    | `--threads 8` |


## 📚 Libraries Used

### Core Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| **JDK** | 11+ | Java Runtime Environment |
| **JMH** | 1.37 | Java Microbenchmark Harness - Performance testing |
| **JUnit** | 4.13.2 | Unit testing framework |
| **Mockito** | 5.2.0+ | Mocking framework for tests |
| **Maven Shade Plugin** | 3.4.1 | Fat JAR creation with dependencies |

### No External Business Logic Dependencies
- The application is built with pure Java standard library
- No Spring, Hibernate, or other heavyweight frameworks
- Minimal dependencies keep the JAR lightweight (~10MB)

---

## ⚡ Performance Metrics

### 1GB CSV File Processing

**System Configuration:**
- File Size: ~1GB (approx. 12-15 million rows)
- Processor: 8 cores
- Memory: 16GB

**Processing Times:**

| Reader Strategy | Avg Time | Min | Max | Std Dev | Peak Memory |
|-----------------|----------|-----|-----|---------|-------------|
| **BufferedCSVReader** | 8.70s | 8.33s | 9.21s | 324ms | 3.2 MB |
| **StreamCSVReader** | 10.09s | 9.62s | 10.52s | 321ms | 3.0 MB |
| **MultiThreaded (4 threads)** | 4.34s | 4.02s | 4.78s | 291ms | 3.0 MB |
| **MultiThreaded (8 threads)** | **3.39s** | 3.26s | 3.63s | 128ms | 3.0 MB |
| **MultiThreaded (16 threads)** | **3.38s** | 3.31s | 3.60s | 109ms | 3.0 MB |

**Key Insights:**
- Multi-threaded approach is **2.5x faster** than single-threaded
- 8-16 threads provide optimal performance on modern CPUs
- Memory footprint is minimal (~3 MB per run)
- Total end-to-end processing: **~4-9 seconds depending on strategy**

---

## 🏗️ Architecture

### Clean Architecture Design

The application follows **Clean Architecture** principles with strict layer separation:

```
scripts/verify.py                   # Simple solution with python to verify correct result
│
benchmark/                          # Benchmark logs and results  
│
src/main/java/com/
├── domain/                          # Business Logic Layer (Core)
│   ├── model/
│   │   └── CampaignStats.java      # Business entity
│   └── repositories/
│       └── CampaignReader.java      # Interface contract
│
├── infrastructure/                  # Technical Implementation Layer
│   ├── benmark/
│   │   ├── CSVReaderBenchmark.java (JMH benchmarks)
│   │   └── DetailedBenchmark.java   # Custom benchmark tool
│   └── io/
│       ├── reader/
│       │   ├── BufferedCSVReader.java
│       │   ├── StreamCSVReader.java
│       │   ├── MultiThreadedCSVReader.java
│       │   └── CSVRowParser.java
│       └── writer/
│           └── CSVResultWriter.java
│
└── presentation/                    # User Interface Layer
    ├── Main.java                    # Application entry point
    └── CommandLineParser.java       # CLI argument parsing
```

### Layer Responsibilities

1. **Domain Layer (business logic)**
   - Contains pure business entities (`CampaignStats`)
   - Defines contracts via interfaces (`CampaignReader`)
   - No external dependencies
   - Independent from infrastructure

2. **Infrastructure Layer (technical details)**
   - Implements CSV reading strategies
   - Handles file I/O and byte-level operations
   - Contains benchmarking code
   - Depends on domain layer

3. **Presentation Layer (user interaction)**
   - Handles CLI arguments
   - Orchestrates the aggregation workflow
   - Outputs results to files

### Benefits of This Architecture

✓ **Testability**: Domain logic is easy to test independently
✓ **Maintainability**: Clear separation makes changes localized
✓ **Flexibility**: Easy to add new reader strategies without modifying existing code
✓ **Scalability**: Can replace readers or add new ones with minimal impact
✓ **Reusability**: Domain logic is independent of infrastructure

---

## 💾 Memory Optimization

### 1. **Streaming I/O with Fixed Buffers**

Instead of loading entire file into memory, the application uses streaming with fixed-size buffers:

```java
// ❌ Bad: Loads entire file
byte[] allData = Files.readAllBytes(path);

// ✅ Good: Fixed 64KB buffer
private static final int BUFFER_SIZE = 65536; // 64 KB
byte[] buffer = new byte[BUFFER_SIZE];
while ((bytesRead = fis.read(buffer)) != -1) {
    // Process buffer contents
}
```

**Memory Benefit**: Only ~64KB in memory at a time, not entire 1GB file

### 2. **HashMap Over HashSet with Custom Objects**

Uses efficient HashMap aggregation instead of collecting all rows:

```java
// Single pass through data
Map<String, CampaignStats> campaignMap = new HashMap<>();
for (String row : csvLines) {
    String campaignId = extractCampaignId(row);
    campaignMap.computeIfAbsent(campaignId, CampaignStats::new)
        .updateStats(impressions, clicks, spend, conversions);
}
```

**Memory Benefit**: 
- Only stores aggregated stats (~100KB for 1000s of campaigns)
- Not entire row data (would be 1GB+)
- O(1) lookups via HashMap

### 3. **StringBuilder for String Concatenation**

In multi-threaded reader, uses `StringBuilder` for line accumulation:

```java
StringBuilder line = new StringBuilder(256); // Pre-sized to 256 chars
line.append((char) b);
// More efficient than: String line = line + (char) b;
```

**Memory Benefit**: Avoids creating temporary String objects

### 4. **Reusable byte[] Buffer in Loops**

Reuses the same buffer across iterations:

```java
byte[] buffer = new byte[BUFFER_SIZE]; // Create once
while ((bytesRead = fis.read(buffer)) != -1) {
    // Reuse buffer, no new allocation
}
```

**Memory Benefit**: Single allocation, reused across millions of reads

### 5. **Garbage Collection Awareness**

Multi-threaded reader performs aggregation AFTER reading:

```java
// Phase 1: Each thread reads and aggregates to its own HashMap
// (No synchronization overhead, garbage friendly)
Map<String, CampaignStats>[] threadResults = new HashMap[numThreads];

// Phase 2: Final aggregation (minimal allocations)
Map<String, CampaignStats> finalMap = aggregateResults(threadResults);
```

### 6. **Avoid split and Try to make the most of the string pool.**

To reduce allocation overhead, the CSV parser avoids using `String.split()` and instead parses fields directly using character indexes.

Numeric fields (`impressions`, `clicks`, `spend`, `conversions`) are processed with lightweight parsing logic to minimize temporary object creation.

That said, the implementation does not fully eliminate all `String` allocations (for example, `campaignId` extraction and `Double.parseDouble()` still require them). This was an intentional trade-off to balance performance, readability, and implementation complexity within the scope of the challenge.
```java
   Follow CSVRowParser.java for details on how fields are parsed with minimal allocations.
```
```java
campaignId = row.substring(fieldStart, fieldEnd).intern();
```

**Memory Benefit**: Reduces GC pressure during I/O phase

### Result: **Peak Memory ~3 MB for 1GB file** (99.7% efficiency)

---

## ⚙️ Performance Optimization: Priority Queue

### Problem: Finding Top 10 Without Sorting All Data

**Naive approach**: Sort all campaigns (O(n log n))
```java
List<CampaignStats> all = campaignMap.values();
all.sort((a, b) -> Double.compare(b.getCTR(), a.getCTR()));
return all.stream().limit(10).collect(toList());
```

**Complexity**: O(n log n) where n = millions of campaigns

### Solution: Min-Heap with PriorityQueue

**Top-K approach**: Maintain only top 10 using min-heap (O(n log k))
```java
PriorityQueue<CampaignStats> topK = new PriorityQueue<>(10,
    (a, b) -> Double.compare(a.getCTR(), b.getCTR())); // Min-heap

for (CampaignStats campaign : campaignMap.values()) {
    topK.offer(campaign);           // O(log 10)
    if (topK.size() > 10) {
        topK.poll();                // O(log 10) - remove smallest
    }
}
```

### How It Works (Example)

For **Top 10 by CTR**:
1. Use **min-heap** (smallest CTR at head)
2. For each campaign:
   - Add to heap: O(log 10)
   - If heap > 10 items, remove smallest: O(log 10)
3. Result: Always have 10 best items, discard rest

### Performance Comparison

| Operation | Full Sort | Min-Heap |
|-----------|-----------|----------|
| Time Complexity | O(n log n) | **O(n log k)** |
| For n=10M, k=10 | O(10M × 23) | **O(10M × 3.3)** |
| Speedup | Baseline | **~7x faster** |

### Code Implementation

```java
// Top 10 by CTR (highest CTR first)
public static List<CampaignStats> getTop10ByCTR(
        Map<String, CampaignStats> campaignMap) {
    // Min-heap: keeps top 10 with smallest CTR at head
    PriorityQueue<CampaignStats> topK = new PriorityQueue<>(10,
        (a, b) -> Double.compare(a.getCTR(), b.getCTR()));
    
    for (CampaignStats campaign : campaignMap.values()) {
        topK.offer(campaign);
        if (topK.size() > 10) {
            topK.poll();
        }
    }
    
    // Convert to sorted list descending
    List<CampaignStats> result = new ArrayList<>(topK);
    result.sort((a, b) -> Double.compare(b.getCTR(), a.getCTR()));
    return result;
}

// Top 10 by CPA (lowest CPA first)
public static List<CampaignStats> getTop10ByCPA(
        Map<String, CampaignStats> campaignMap) {
    // Max-heap: keeps top 10 with highest CPA at head
    PriorityQueue<CampaignStats> topK = new PriorityQueue<>(10,
        (a, b) -> {
            Double cpaA = a.getCPA();
            Double cpaB = b.getCPA();
            if (cpaA == null) return 1;
            if (cpaB == null) return -1;
            return Double.compare(cpaB, cpaA); // Reverse for max-heap
        });
    
    for (CampaignStats campaign : campaignMap.values()) {
        if (campaign.getTotalConversions() > 0) {
            topK.offer(campaign);
            if (topK.size() > 10) {
                topK.poll();
            }
        }
    }
    
    // Convert to sorted list ascending by CPA
    List<CampaignStats> result = new ArrayList<>(topK);
    result.sort((a, b) -> {
        Double cpaA = a.getCPA();
        Double cpaB = b.getCPA();
        if (cpaA == null) return 1;
        if (cpaB == null) return -1;
        return Double.compare(cpaA, cpaB);
    });
    return result;
}
```

### Memory Benefit

- **Without optimization**: Stores all campaigns in memory for sorting
- **With optimization**: Only 10 items in heap at any time
- For 1M campaigns: **10 items vs 1M items = 100,000x less memory**

---

## 📊 Benchmark Analysis

### Benchmark Setup

- **File**: 1GB CSV (~12-15 million rows)
- **System**: 8 core CPU
- **Runs**: 5 measurement runs with 2 warmup runs
- **Metric**: Execution time (ms) + Memory usage (MB)

### Detailed Results

#### 1. **BufferedCSVReader**

```
Execution Time: 8,704.80 ms (avg)
- Min: 8,332 ms
- Max: 9,206 ms
- Std Dev: 324.26 ms
- Peak Memory: 3.20 MB
```

**Characteristics**:
- Single-threaded baseline
- Simple `BufferedReader` wrapper
- Good for small files
- Consistent performance (std dev = 324ms)

**When to use**: Small files (<100MB), simplicity preferred

---

#### 2. **StreamCSVReader**

```
Execution Time: 10,086.60 ms (avg)
- Min: 9,616 ms
- Max: 10,516 ms
- Std Dev: 321.50 ms
- Peak Memory: 3.00 MB
```

**Characteristics**:
- Single-threaded byte-stream approach
- Manual line parsing
- **15% SLOWER** than BufferedReader
- Fine-grained buffer control

**Why it's slower**:
- Manual byte-by-byte character parsing
- More overhead than BufferedReader's optimized line reading
- No buffering advantage

**When to use**: Only when specific byte-level control is needed

---

#### 3. **MultiThreadedCSVReader (4 threads)**

```
Execution Time: 4,342.60 ms (avg)
- Min: 4,023 ms
- Max: 4,780 ms
- Std Dev: 291.09 ms
- Peak Memory: 3.00 MB

Speedup vs BufferedReader: 2.01x
```

**Characteristics**:
- Divides file into 4 chunks
- Each thread reads independently
- Final aggregation phase combines results
- **50% FASTER** than single-threaded

**Why 4 threads works well**:
- I/O parallelization on multi-core systems
- Minimal synchronization (only final merge)
- No lock contention during reading phase

---

#### 4. **MultiThreadedCSVReader (8 threads)** ⭐ RECOMMENDED

```
Execution Time: 3,387.00 ms (avg)
- Min: 3,260 ms
- Max: 3,633 ms
- Std Dev: 127.87 ms ← LOWEST variance
- Peak Memory: 3.00 MB

Speedup vs BufferedReader: 2.57x
```

**Characteristics**:
- Optimal for most systems
- **LOWEST standard deviation** (127ms vs 300ms others)
- **MOST STABLE** performance
- 8 threads matches typical CPU count

**Why 8 threads is optimal**:
- Matches common CPU core count (8-16)
- Maximizes I/O parallelization
- Minimal thread overhead (context switching)
- Best throughput-to-stability ratio

---

#### 5. **MultiThreadedCSVReader (16 threads)**

```
Execution Time: 3,377.80 ms (avg)
- Min: 3,314 ms
- Max: 3,595 ms
- Std Dev: 108.79 ms ← BEST
- Peak Memory: 3.00 MB

Speedup vs BufferedReader: 2.58x
```

**Characteristics**:
- Marginal improvement over 8 threads (0.3% faster)
- **BEST standard deviation** (108.79ms)
- More thread overhead from context switching
- Slightly better stability

**Performance plateau**:
- 8 vs 16 threads: Only **10ms difference**
- Additional threads add overhead > benefit
- Demonstrates law of diminishing returns

---

### Performance Summary Table

```
Reader                              Avg Time    Speedup   Stability
─────────────────────────────────────────────────────────────────
BufferedCSVReader                   8,704 ms    1.00x     ●●●
StreamCSVReader                    10,086 ms    0.86x     ●●●
MultiThreadedCSVReader (4 threads)  4,342 ms    2.01x     ●●●
MultiThreadedCSVReader (8 threads)  3,387 ms    2.57x     ●●●●
MultiThreadedCSVReader (16 threads) 3,377 ms    2.58x     ●●●●●
```

---

### Key Findings

1. **Multi-threading is essential** for large files
   - 2.5-2.6x faster than single-threaded

2. **8 threads is the sweet spot**
   - Best balance of speed and stability
   - Typical system configuration

3. **I/O is the bottleneck**
   - Parallelizing reads gives massive speedup
   - Parser itself is very efficient

4. **Memory is not the constraint**
   - All readers use only 3 MB
   - Aggregation is the memory-efficient part

5. **StreamCSVReader is slower**
   - Manual parsing overhead > BufferedReader benefits
   - Avoid unless specific requirements

---

### Recommendation

For production use on 1GB files:

```bash
# ✅ RECOMMENDED
java -cp app.jar com.presentation.Main \
  --input ad_data.csv \
  --reader thread \
  --threads 8 \
  --output results/

# Processing time: ~3.4 seconds
# Memory usage: ~3 MB
# Stability: Excellent
```

---

## 📈 JMH Benchmark Results

See `benchmark/jmh_benchmark.txt` for complete JMH analysis including:
- Sampling time percentiles (p0, p50, p90, p99, p100)
- Throughput measurements (ops/ms)
- Histogram distributions
- Multiple fork results

Key metrics extracted:
- **Sampling time (50th percentile)**:
  - BufferedCSVReader: 8,791 ms
  - MultiThreadedCSVReader (8): 3,995 ms
  - **2.2x faster with 8 threads**

---

## 🧪 Testing

### Test Files

- `DataAggregatorTest.java`: Tests aggregation logic
- `CampaignStatsTest.java`: Tests calculation correctness

---

## 📁 Output Files

The application generates two CSV files:

### `top10_ctr.csv`

```csv
campaign_id,total_impressions,total_clicks,total_spend,total_conversions,CTR,CPA
CMP042,125000,6250,12500.50,625,0.0500,20.00
CMP015,340000,15300,30600.25,1530,0.0450,20.00
...
```

Columns sorted by **highest CTR first**

### `top10_cpa.csv`

```csv
campaign_id,total_impressions,total_clicks,total_spend,total_conversions,CTR,CPA
CMP007,450000,13500,13500.00,1350,0.0300,10.00
CMP019,780000,23400,23400.00,2340,0.0300,10.00
...
```

Columns sorted by **lowest CPA first**

---

## 🔧 Configuration

### Tuning Parameters

In source code (easily configurable):

```java
// BufferedCSVReader.java
private static final int BUFFER_SIZE = 65536; // 64 KB buffer

// MultiThreadedCSVReader.java
private static final int BUFFER_SIZE = 65536;   // 64 KB buffer
private static final int CHUNK_SIZE = 10 * 1024 * 1024; // 10 MB chunks
```

### Optimal Settings for Different File Sizes

| File Size | Reader | Threads | Approx Time |
|-----------|--------|---------|-------------|
| < 50 MB | BufferedCSVReader | N/A | < 0.5s |
| 50-500 MB | MultiThreadedCSVReader | 4 | 0.5-3s |
| 500MB - 2GB | MultiThreadedCSVReader | 8 | 3-7s |
| > 2GB | MultiThreadedCSVReader | 16 | 7-15s |

---

## 📦 Docker

### Build

```bash
docker build -t ad-aggregator:1.0 .
```

### Run

```bash
# Mount data directory
docker run --rm \
  -v /path/to/data:/data \
  ad-aggregator:1.0 \
  --input /data/ad_data.csv \
  --output /data/results/ \
  --reader thread \
  --threads 8
```

### Environment

- **Base Image**: `eclipse-temurin:17-jdk-alpine`
- **Size**: ~500 MB (lightweight)
- **Startup Time**: < 1 second

---

## 📝 Troubleshooting

### "File not found" error

```bash
# Ensure file exists and path is correct
ls -lh ad_data.csv

# Use absolute path if relative path fails
/path/to/ad_data.csv
```

### Out of Memory

The application should never OOM with default settings. If it does:

```bash
# Reduce thread count
--reader thread --threads 4

# Or use BufferedReader (less memory overall)
--reader buffer
```

### Slow Performance

1. Check system load: `top` or `htop`
2. Ensure file is on local disk (not network)
3. Try multi-threading: `--reader thread --threads 8`

---




