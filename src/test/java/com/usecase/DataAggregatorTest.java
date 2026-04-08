package com.usecase;

import com.domain.model.CampaignStats;
import com.domain.repositories.CampaignReader;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DataAggregator class.
 * Tests aggregation logic, integration with CampaignReader, and campaign map management.
 */
public class DataAggregatorTest {
	@Mock
	private CampaignReader mockReader;
	private DataAggregator dataAggregator;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		dataAggregator = new DataAggregator(mockReader);
	}
	
	/**
	 * Test constructor initializes reader and empty campaign map.
	 */
	@Test
	public void testConstructor() {
		assertNotNull("DataAggregator should not be null", dataAggregator);
		Map<String, CampaignStats> campaignMap = dataAggregator.getCampaignMap();
		assertNotNull("Campaign map should not be null", campaignMap);
		assertTrue("Campaign map should be empty initially", campaignMap.isEmpty());
	}
	
	/**
	 * Test aggregate with single campaign.
	 */
	@Test
	public void testAggregateWithSingleCampaign() throws IOException {
		String filename = "sample_data_test.csv";
		Map<String, CampaignStats> expectedData = new HashMap<>();
		CampaignStats stats = new CampaignStats("camp_001");
		stats.updateStats(100, 10, 50.0, 5);
		expectedData.put("camp_001", stats);
		when(mockReader.readStats(filename)).thenReturn(expectedData);
		dataAggregator.aggregate(filename);
		verify(mockReader, times(1)).readStats(filename);
		Map<String, CampaignStats> result = dataAggregator.getCampaignMap();
		assertEquals("Campaign map should contain one campaign", 1, result.size());
		assertTrue("Campaign map should contain camp_001", result.containsKey("camp_001"));
		assertEquals("Campaign stats should match", stats.getTotalImpressions(), result.get("camp_001").getTotalImpressions());
	}
	
	/**
	 * Test aggregate with multiple campaigns.
	 */
	@Test
	public void testAggregateWithMultipleCampaigns() throws IOException {
		String filename = "sample_data_test.csv";
		Map<String, CampaignStats> expectedData = new HashMap<>();
		CampaignStats stats1 = new CampaignStats("camp_001");
		stats1.updateStats(100, 10, 50.0, 5);
		expectedData.put("camp_001", stats1);
		CampaignStats stats2 = new CampaignStats("camp_002");
		stats2.updateStats(200, 20, 100.0, 10);
		expectedData.put("camp_002", stats2);
		CampaignStats stats3 = new CampaignStats("camp_003");
		stats3.updateStats(300, 30, 150.0, 15);
		expectedData.put("camp_003", stats3);
		when(mockReader.readStats(filename)).thenReturn(expectedData);
		dataAggregator.aggregate(filename);
		Map<String, CampaignStats> result = dataAggregator.getCampaignMap();
		assertEquals("Campaign map should contain three campaigns", 3, result.size());
		assertTrue("Campaign map should contain all campaigns",
			result.containsKey("camp_001") && result.containsKey("camp_002") && result.containsKey("camp_003"));
	}
	
	/**
	 * Test aggregate with empty result from reader.
	 */
	@Test
	public void testAggregateWithEmptyResult() throws IOException {
		String filename = "empty.csv";
		Map<String, CampaignStats> emptyData = new HashMap<>();
		when(mockReader.readStats(filename)).thenReturn(emptyData);
		dataAggregator.aggregate(filename);
		Map<String, CampaignStats> result = dataAggregator.getCampaignMap();
		assertTrue("Campaign map should be empty", result.isEmpty());
		verify(mockReader, times(1)).readStats(filename);
	}
	
	/**
	 * Test aggregate throws IOException when reader throws.
	 */
	@Test(expected = IOException.class)
	public void testAggregateThrowsIOException() throws IOException {
		String filename = "nonexistent.csv";
		when(mockReader.readStats(filename)).thenThrow(new IOException("File not found"));
		dataAggregator.aggregate(filename);
	}
	
	/**
	 * Test getCampaignMap returns same reference after aggregate.
	 */
	@Test
	public void testGetCampaignMapAfterAggregate() throws IOException {
		String filename = "sample_data_test.csv";
		Map<String, CampaignStats> expectedData = new HashMap<>();
		CampaignStats stats = new CampaignStats("camp_001");
		stats.updateStats(100, 10, 50.0, 5);
		expectedData.put("camp_001", stats);
		when(mockReader.readStats(filename)).thenReturn(expectedData);
		dataAggregator.aggregate(filename);
		Map<String, CampaignStats> result1 = dataAggregator.getCampaignMap();
		Map<String, CampaignStats> result2 = dataAggregator.getCampaignMap();
		assertSame("Should return same reference", result1, result2);
		assertEquals("Both references should have same data", 1, result1.size());
		assertEquals("Both references should have same data", 1, result2.size());
	}
	
	/**
	 * Test aggregate overwrites previous data.
	 */
	@Test
	public void testAggregateOverwritesPreviousData() throws IOException {
		String filename1 = "test1.csv";
		String filename2 = "test2.csv";
		Map<String, CampaignStats> data1 = new HashMap<>();
		CampaignStats stats1 = new CampaignStats("camp_001");
		stats1.updateStats(100, 10, 50.0, 5);
		data1.put("camp_001", stats1);
		when(mockReader.readStats(filename1)).thenReturn(data1);
		dataAggregator.aggregate(filename1);
		Map<String, CampaignStats> result1 = dataAggregator.getCampaignMap();
		assertEquals("First aggregation should have 1 campaign", 1, result1.size());
		Map<String, CampaignStats> data2 = new HashMap<>();
		CampaignStats stats2 = new CampaignStats("camp_002");
		stats2.updateStats(200, 20, 100.0, 10);
		CampaignStats stats3 = new CampaignStats("camp_003");
		stats3.updateStats(300, 30, 150.0, 15);
		data2.put("camp_002", stats2);
		data2.put("camp_003", stats3);
		when(mockReader.readStats(filename2)).thenReturn(data2);
		dataAggregator.aggregate(filename2);
		Map<String, CampaignStats> result2 = dataAggregator.getCampaignMap();
		assertEquals("Second aggregation should have 2 campaigns", 2, result2.size());
		assertFalse("camp_001 should be replaced", result2.containsKey("camp_001"));
		assertTrue("camp_002 should be present", result2.containsKey("camp_002"));
		assertTrue("camp_003 should be present", result2.containsKey("camp_003"));
	}
	
	/**
	 * Test aggregate with campaigns having zero conversions.
	 */
	@Test
	public void testAggregateWithCampaignsZeroConversions() throws IOException {
		String filename = "sample_data_test.csv";
		Map<String, CampaignStats> expectedData = new HashMap<>();
		CampaignStats stats1 = new CampaignStats("camp_001");
		stats1.updateStats(100, 10, 50.0, 0);
		expectedData.put("camp_001", stats1);
		CampaignStats stats2 = new CampaignStats("camp_002");
		stats2.updateStats(200, 20, 100.0, 10);
		expectedData.put("camp_002", stats2);
		when(mockReader.readStats(filename)).thenReturn(expectedData);
		dataAggregator.aggregate(filename);
		Map<String, CampaignStats> result = dataAggregator.getCampaignMap();
		assertEquals("Campaign map should contain both campaigns", 2, result.size());
		assertNull("Campaign 1 CPA should be null", result.get("camp_001").getCPA());
		assertNotNull("Campaign 2 CPA should not be null", result.get("camp_002").getCPA());
	}
	
	/**
	 * Test aggregate preserves campaign statistics accuracy.
	 */
	@Test
	public void testAggregatePreservesAccuracy() throws IOException {
		String filename = "sample_data_test.csv";
		Map<String, CampaignStats> expectedData = new HashMap<>();
		CampaignStats stats = new CampaignStats("camp_001");
		long impressions = 1000;
		long clicks = 50;
		double spend = 250.75;
		long conversions = 12;
		stats.updateStats(impressions, clicks, spend, conversions);
		expectedData.put("camp_001", stats);
		when(mockReader.readStats(filename)).thenReturn(expectedData);
		dataAggregator.aggregate(filename);
		CampaignStats result = dataAggregator.getCampaignMap().get("camp_001");
		assertEquals("Impressions should be preserved", impressions, result.getTotalImpressions());
		assertEquals("Clicks should be preserved", clicks, result.getTotalClicks());
		assertEquals("Spend should be preserved", spend, result.getTotalSpend(), 0.001);
		assertEquals("Conversions should be preserved", conversions, result.getTotalConversions());
	}
	
	/**
	 * Test reader is called with correct filename.
	 */
	@Test
	public void testReaderCalledWithCorrectFilename() throws IOException {
		String filename = "/path/to/sample_data_test.csv";
		Map<String, CampaignStats> expectedData = new HashMap<>();
		when(mockReader.readStats(filename)).thenReturn(expectedData);
		dataAggregator.aggregate(filename);
		verify(mockReader, times(1)).readStats(filename);
		verifyNoMoreInteractions(mockReader);
	}
	
	/**
	 * Test aggregate can be called multiple times with different files.
	 */
	@Test
	public void testAggregateBehaviorWithDifferentFiles() throws IOException {
		String file1 = "file1.csv";
		String file2 = "file2.csv";
		Map<String, CampaignStats> data1 = new HashMap<>();
		CampaignStats stats1 = new CampaignStats("camp_A");
		stats1.updateStats(100, 10, 50.0, 5);
		data1.put("camp_A", stats1);
		Map<String, CampaignStats> data2 = new HashMap<>();
		CampaignStats stats2 = new CampaignStats("camp_B");
		stats2.updateStats(200, 20, 100.0, 10);
		data2.put("camp_B", stats2);
		when(mockReader.readStats(file1)).thenReturn(data1);
		when(mockReader.readStats(file2)).thenReturn(data2);
		dataAggregator.aggregate(file1);
		assertEquals("After first aggregate, should have 1 campaign", 1, dataAggregator.getCampaignMap().size());
		assertTrue("Should contain camp_A", dataAggregator.getCampaignMap().containsKey("camp_A"));
		dataAggregator.aggregate(file2);
		assertEquals("After second aggregate, should have 1 campaign", 1, dataAggregator.getCampaignMap().size());
		assertFalse("Should not contain camp_A anymore", dataAggregator.getCampaignMap().containsKey("camp_A"));
		assertTrue("Should contain camp_B", dataAggregator.getCampaignMap().containsKey("camp_B"));
	}
	
	/**
	 * Test CTR and CPA calculations work correctly through aggregator.
	 */
	@Test
	public void testCTRAndCPACalculationsThroughAggregator() throws IOException {
		String filename = "sample_data_test.csv";
		Map<String, CampaignStats> expectedData = new HashMap<>();
		CampaignStats stats = new CampaignStats("camp_001");
		stats.updateStats(1000, 50, 100.0, 20);
		expectedData.put("camp_001", stats);
		when(mockReader.readStats(filename)).thenReturn(expectedData);
		dataAggregator.aggregate(filename);
		CampaignStats result = dataAggregator.getCampaignMap().get("camp_001");
		double expectedCTR = 50.0 / 1000.0;
		double expectedCPA = 100.0 / 20.0;
		assertEquals("CTR should be calculated correctly", expectedCTR, result.getCTR(), 0.0001);
		assertEquals("CPA should be calculated correctly", expectedCPA, result.getCPA(), 0.0001);
	}
	
	/**
	 * Test that modifications to returned campaign map affect internal state.
	 */
	@Test
	public void testMapModificationsAffectInternalState() throws IOException {
		String filename = "sample_data_test.csv";
		Map<String, CampaignStats> expectedData = new HashMap<>();
		CampaignStats stats = new CampaignStats("camp_001");
		stats.updateStats(100, 10, 50.0, 5);
		expectedData.put("camp_001", stats);
		when(mockReader.readStats(filename)).thenReturn(expectedData);
		dataAggregator.aggregate(filename);
		Map<String, CampaignStats> map = dataAggregator.getCampaignMap();
		map.put("camp_002", new CampaignStats("camp_002"));
		Map<String, CampaignStats> newMap = dataAggregator.getCampaignMap();
		assertTrue("New campaign should be in aggregator's map", newMap.containsKey("camp_002"));
		assertEquals("Map should have 2 campaigns", 2, newMap.size());
	}
	
	/**
	 * Test getCampaignMap returns non-null value initially.
	 */
	@Test
	public void testGetCampaignMapInitiallyNonNull() {
		DataAggregator aggregator = new DataAggregator(mockReader);
		assertNotNull("getCampaignMap should never return null", aggregator.getCampaignMap());
	}
	
	/**
	 * Test reader is called exactly once per aggregate call.
	 */
	@Test
	public void testReaderCalledExactlyOnce() throws IOException {
		String filename = "sample_data_test.csv";
		Map<String, CampaignStats> expectedData = new HashMap<>();
		when(mockReader.readStats(filename)).thenReturn(expectedData);
		dataAggregator.aggregate(filename);
		verify(mockReader, times(1)).readStats(filename);
	}
	
	/**
	 * Integration test with realistic campaign data.
	 */
	@Test
	public void testRealisticDataIntegration() throws IOException {
		String filename = "realistic.csv";
		Map<String, CampaignStats> expectedData = new HashMap<>();
		
		CampaignStats searchStats = new CampaignStats("search");
		searchStats.updateStats(100000L, 5000L, 25000.00, 500L);
		expectedData.put("search", searchStats);
		
		CampaignStats socialStats = new CampaignStats("social");
		socialStats.updateStats(200000L, 4000L, 30000.00, 600L);
		expectedData.put("social", socialStats);
		
		CampaignStats displayStats = new CampaignStats("display");
		displayStats.updateStats(50000L, 1000L, 10000.00, 100L);
		expectedData.put("display", displayStats);
		
		CampaignStats videoStats = new CampaignStats("video");
		videoStats.updateStats(300000L, 3000L, 45000.00, 1000L);
		expectedData.put("video", videoStats);
		
		when(mockReader.readStats(filename)).thenReturn(expectedData);
		dataAggregator.aggregate(filename);
		Map<String, CampaignStats> result = dataAggregator.getCampaignMap();
		assertEquals("Should have 4 campaigns", 4, result.size());
		CampaignStats search = result.get("search");
		assertNotNull("Search campaign should exist", search);
		assertEquals("Search CTR should be 5%", 0.05, search.getCTR(), 0.0001);
		assertEquals("Search CPA should be 50", 50.0, search.getCPA(), 0.001);
		CampaignStats video = result.get("video");
		assertNotNull("Video campaign should exist", video);
		assertEquals("Video CTR should be 1%", 0.01, video.getCTR(), 0.0001);
		assertEquals("Video CPA should be 45", 45.0, video.getCPA(), 0.001);
	}
}
