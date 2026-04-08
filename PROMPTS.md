# 📋 Development Prompts & Thought Process

This document outlines the prompts used to develop the Ad Performance Aggregator, presented sequentially to show the problem-solving approach and iteration process.

><p style="color:#ff0033;"> **Note**: Some debugging and refinement prompts have been omitted to avoid being too lengthy. But the prompt remains intact.

---

## 🔍 Phase 1: Requirements Analysis & Initial Research

### Prompt 1: Understanding Requirements
**Tool**: ChatGPT  
**Purpose**: Initial solution research

```
Read the README file above and summarize the requirements, 
suggesting some solutions.
```

**Outcome**: Understood the core challenge - process 1GB CSV files efficiently with aggregation and top-10 reporting.

---

### Prompt 2: Performance & Memory Optimization Strategy
**Tool**: ChatGPT  
**Purpose**: Identify optimization approaches

```
Your solution must handle large datasets efficiently with good 
performance and memory optimization. I want optimize solution to 
handle large datasets, please suggest some solution
```

**Outcome**: 
- Use streaming I/O instead of loading entire file
- Implement HashMap-based aggregation
- Consider multi-threading for I/O parallelization
- Use Priority Queue for top-K selection

---

### Prompt 3: Large File CSV Reading Techniques
**Tool**: ChatGPT  
**Purpose**: Explore effective CSV reading methods

```
How to read large file csv effectively?
```

**Outcome**: Researched three main approaches:
1. BufferedReader (traditional)
2. FileInputStream with byte buffers (streaming)
3. MultiThreading with chunk processing

---

### Prompt 4: Multi-threaded Processing Strategy
**Tool**: ChatGPT  
**Purpose**: Design concurrent file processing

```
I want chunk processing and multithreading
```

**Outcome**: Designed architecture for:
- Dividing file into chunks
- Per-thread independent aggregation
- Final merge phase without synchronization overhead

---

### Prompt 5: RandomAccessFile Chunk Management
**Tool**: ChatGPT  
**Purpose**: Understand efficient file seeking

```
How to seek in offset line and skip n line?
Performance of RandomAccessFile.seek() in java?
```

**Outcome**: 
- RandomAccessFile is efficient for chunk seeking
- Skip partial lines at chunk boundaries
- Performance is negligible for I/O operations

---

### Prompt 6: File Partitioning Strategy
**Tool**: ChatGPT  
**Purpose**: Design chunk-based processing

```
How to partition csv file by chunk for multi processing?
```

**Outcome**: Implemented chunk partitioning by byte offset, not line-based.

---

### Prompt 7: Edge Case Analysis
**Tool**: ChatGPT  
**Purpose**: Identify potential issues

```
Analyzing several edge cases?
```

**Outcome**: Identified and handled:
- Files not ending with newline
- Partial lines at chunk boundaries
- Carriage return characters (Windows line endings)
- Zero-conversion campaigns (for CPA calculation)

---

## 🏗️ Phase 2: Code Implementation & Architecture

### Prompt 8: Clean Code Structure
**Tool**: GitHub Copilot  
**Purpose**: Establish clean architecture

```
Read the README.md file and write a function to read the input 
filename. I want to process the data using a HashMap instead of 
saving each line individually, and use the HashMap to summarize 
the results. Must follow this instruction:
- Clean, readable code — meaningful names, consistent style, 
  no dead code or commented-out blocks
- Error handling — handle missing files, malformed rows, and 
  edge cases gracefully
```

**Outcome**: 
- Created `CampaignStats` domain model
- Implemented `CampaignReader` interface
- Built `DataAggregator` with HashMap-based aggregation

---

### Prompt 9: Separation of Concerns
**Tool**: GitHub Copilot  
**Purpose**: Enable benchmarking and flexibility

```
I want cleaner code: Separate the classes, each function has 
its own task, and reading the file and handling the row is done 
by a different function, so that benchmarks can be used to 
replace various file reading methods.
```

**Outcome**: 
- Created `CSVRowParser` for row parsing logic
- Separated infrastructure concerns
- Enabled pluggable reader implementations

---

### Prompt 10: Multiple Reader Implementations
**Tool**: GitHub Copilot  
**Purpose**: Implement alternative CSV readers

```
Read three classes: MultiThreadReader (reads files in chunks by 
thread), SingleLineBufferReader (reads files line by line), and 
StreamCSVReader (reads files using byte streams). Apply this to 
provide multiple ways to read files instead of relying solely 
on bufferreaders. I will benchmark based on these different 
methods; for multi-threading, handle it separately to ensure 
consistency (each thread should have its own statistics and 
aggregate them upon completion).
```

**Outcome**: 
- `BufferedCSVReader`: Traditional line-by-line reading
- `StreamCSVReader`: Byte-stream with manual parsing
- `MultiThreadedCSVReader`: Chunk-based with parallel processing

---

### Prompt 11: Removing Synchronization Overhead
**Tool**: GitHub Copilot  
**Purpose**: Fair benchmark comparison

```
I see that multi-threading is already working with synchronized 
rowHandler, which can create multiple row handlers per request. 
I want to modify the CSVReader interface to readFile and return 
a HashMap<String, CampaignStats>. Remove the synchronization 
from MultiThreadedCSVReader to ensure benchmarks are performed 
within the same context.
```

**Outcome**: 
- Each thread maintains isolated HashMap
- No synchronization during reading phase
- Single merge phase after all threads complete
- Fair performance comparison across readers

---

## ⚡ Phase 3: Performance Optimization

### Prompt 12: Top-K Selection Optimization
**Tool**: GitHub Copilot  
**Purpose**: Optimize result filtering

```
Optimize ResultsGenerator with PriorityQueue size with 10
```

**Outcome**: 
- Replaced full sorting with min-heap approach
- O(n log k) vs O(n log n) complexity
- ~7x faster for top-10 selection
- Minimal memory footprint (10 items vs millions)

---

### Prompt 13: CLI Argument Flexibility
**Tool**: GitHub Copilot  
**Purpose**: Enable reader strategy selection

```
Change code Main accept type of read csv (buffer, stream, thread). 
With option use thread, read args numberThread and pass to 
MultiThreadedCSVReader
```

**Outcome**: 
- `--reader buffer|stream|thread` flag
- `--threads N` parameter for multi-threaded mode
- Factory pattern in `Main.createCSVReader()`

---

## ✅ Phase 4: Verification & Testing

### Prompt 14: Results Verification
**Tool**: ChatGPT  
**Purpose**: Validate correctness

```
Write a simple code snippet to verify my results, read CSV data 
from a file, and perform two tests: Top 10 campaigns with the 
highest CTR and Top 10 campaigns with the lowest CPA. Keep it 
as simple as possible, using a single file to run the verification test.
```

**Outcome**: 
- Created verification script
- Tested against correct_results/ baseline
- Confirmed CTR and CPA calculations

---

### Prompt 15: Output Format Correction
**Tool**: ChatGPT  
**Purpose**: Fix output format issues

```
Not print, export with folder csv file?
Output is wrong format: CTR and total_spend, fix now
```

**Outcome**: 
- Implemented `CSVResultWriter`
- Fixed column ordering and formatting
- Proper float precision (2 decimals for money, 4 for rates)

---

### Prompt 16: Multi-threading Bug Fixing
**Tool**: GitHub Copilot  
**Purpose**: Debug thread count issue

```
Something seems wrong here. In MultiThreadedReader, 
if the number of threads is 8, everything works fine, 
but if it's 16, the output is incorrect.
```

**Outcome**: 
- Identified chunk boundary handling bug
- Fixed row skipping logic for non-first chunks
- Verified with 4, 8, 16 thread configurations

---

### Prompt 17: Chunk Boundary Logic
**Tool**: GitHub Copilot  
**Purpose**: Clarify thread coordination

```
But if currentPos can > end without encountering \n, 
shouldn't that be handled by another thread?
```

**Outcome**: 
- Understood that partial lines at boundaries are acceptable
- Only one thread reads past chunk boundary
- Other threads properly skip incomplete lines
- Verified correctness with integration tests

---

## 📊 Phase 5: Benchmarking & Profiling

### Prompt 18: Performance Analysis
**Tool**: Gemini  
**Purpose**: Identify remaining optimizations

```
Read the project and analyze it to see if there are any other 
ways to optimize performance and memory usage.
```

**Outcome**: 
- Confirmed streaming I/O is optimal
- HashMap aggregation avoids large intermediate collections
- Multi-threading provides 2.5x speedup
- Further optimizations have diminishing returns

---

### Prompt 19: JMH Benchmarking Setup
**Tool**: ChatGPT  
**Purpose**: Learn JMH framework

```
Write a benchmark showing how this file is read, and suggest 
some methods.
How to run benchmark with JMH?
Where do I add the build plugin?
Explain some annotations @BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1) // Only 1 JVM process 
@Warmup(iterations = 1) 
@Measurement(iterations = 1)?
Write Dockerfile
```

**Outcome**: 
- Set up JMH in Maven configuration
- Implemented `CSVReaderBenchmark` with proper annotations
- Understood JVM warmup, measurement, and forking
- Created lightweight Docker setup

---

## 📈 Phase 6: Custom Benchmarking Tool

### Prompt 20: Detailed Benchmark Implementation
**Tool**: GitHub Copilot  
**Purpose**: Create custom benchmarking tool

```
Write code JMH benchmark with in folder com.infrastructure.benmark 
BufferedCSVReader, MultiThreadedCSVReader, StreamCSVReader. 
I want to benchmark time process, memory usage. Get average in 
many runs
```

**Outcome**: 
- Created `DetailedBenchmark.java`
- Measures execution time and memory usage
- Computes averages and standard deviations across 5 runs
- Generates human-readable comparison tables

---

### Prompt 21: Console Output Benchmarking
**Tool**: GitHub Copilot  
**Purpose**: Alternative benchmarking approach

```
Write code custom with console benchmark
```

**Outcome**: 
- Detailed per-run output
- Statistics summary with min/max/std dev
- Easy interpretation of results
- No external dependencies required

---

## 📚 Phase 7: Documentation

### Prompt 22: Comprehensive README
**Tool**: GitHub Copilot  
**Purpose**: Complete project documentation

```
Write README.md, it contain:
- Setup instructions  
  - How to run the program (with maven and docker)
  - Libraries used  
  - Processing time for the 1GB file  
+ Explain structure code clean architecture
+ Explain how to optimize memory when read file
+ Explain how to optimize performance with priority queue in ResultsGenerator
+ Read file benchmark/benchmark.txt and benchmark/jmh_benchmark.txt 
  and analyst effective of 3 csv reader: BufferedCSVReader, 
  StreamCSVReader MultiThreadedCSVReader
```

**Outcome**: 
- Complete README with all sections
- Architecture diagrams and explanations
- Memory optimization techniques documented
- Priority Queue algorithm explained with complexity analysis
- Comprehensive benchmark analysis with recommendations

---

### Prompt 23: Pretty Prompt Documentation
**Tool**: GitHub Copilot  
**Purpose**: Document the development process with improved formatting

```
Write a file named `PROMPTS.md` describing the prompts I used 
to handle this requirement. Note that they are presented 
sequentially to follow my thought process and problem-solving 
steps. Also, note that I skipped some other prompts during 
coding and debugging to avoid being too verbose.

Please pretty format
```

**Outcome**: This file - clearly documenting the complete development journey with organized phases and easy-to-follow structure.

---

## 📊 Summary Statistics

### AI Tools Used
- **ChatGPT**: Research, algorithm design, JMH setup, Docker
- **GitHub Copilot (Claude Haiku 4.5)**: Code implementation, optimization, debugging  
- **Gemini**: Architecture analysis and optimization review

### Development Phases
1. **Requirements Analysis**: 7 prompts
2. **Code Implementation**: 4 prompts
3. **Performance Optimization**: 2 prompts
4. **Verification & Testing**: 4 prompts
5. **Benchmarking**: 2 prompts
6. **Custom Tools**: 2 prompts
7. **Documentation**: 2 prompts

---
