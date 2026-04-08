package com.infrastructure.benchmark;

import com.domain.repositories.CampaignReader;
import com.infrastructure.io.reader.BufferedCSVReader;
import com.infrastructure.io.reader.MultiThreadedCSVReader;
import com.infrastructure.io.reader.StreamCSVReader;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.util.Map;

@BenchmarkMode({Mode.Throughput, Mode.SampleTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class CSVReaderBenchmark {

    private String filename = "ad_data.csv";
    private CampaignReader bufferReader;
    private CampaignReader multiThreadReader8;
    private CampaignReader multiThreadReader16;
    private CampaignReader multiThreadReader4;
    
    private CampaignReader streamReader;

    @Setup(Level.Trial)
    public void setup() {
        bufferReader = new BufferedCSVReader();
        multiThreadReader8 = new MultiThreadedCSVReader(8);
        streamReader = new StreamCSVReader();
        multiThreadReader16 = new MultiThreadedCSVReader(16);
        multiThreadReader4 = new MultiThreadedCSVReader(4);
        
    }
    
    @TearDown(Level.Trial)
    public void tearDown() {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memoryUsed = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Memory used after trial: " + memoryUsed / 1024 / 1024 + " MB");
    }
    
    @Benchmark
    @Fork(2)
    @Warmup(iterations = 1)
    @Measurement(iterations = 3)
    public Map<String, ?> benchmarkBufferReader() throws IOException {
        return bufferReader.readStats(filename);
    }
    
    @Benchmark
    @Fork(2)
    @Warmup(iterations = 1)
    @Measurement(iterations = 3)
    public Map<String, ?> benchmarkMultiThreadWith8Core() throws IOException {
        return multiThreadReader8.readStats(filename);
    }
    
    @Benchmark
    @Fork(2)
    @Warmup(iterations = 1)
    @Measurement(iterations = 3)
    public Map<String, ?> benchmarkMultiThreadWith16Core() throws IOException {
        return multiThreadReader16.readStats(filename);
    }
    
    @Benchmark
    @Fork(2)
    @Warmup(iterations = 1)
    @Measurement(iterations = 3)
    public Map<String, ?> benchmarkMultiThreadWith4Core() throws IOException {
        return multiThreadReader4.readStats(filename);
    }
    
    
    @Benchmark
    @Fork(2)
    @Warmup(iterations = 1)
    @Measurement(iterations = 3)
    public Map<String, ?> benchmarkStreamReader() throws IOException {
        return streamReader.readStats(filename);
    }
}