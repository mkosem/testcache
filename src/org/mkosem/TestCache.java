package org.mkosem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

public class TestCache {
	// config values
	private static final int threads = 4;
	private static final int size = 2000000;
	private static final int recordSize = 1024;
	private static final int testIterations = 8;

	// calculated config values
	private static final int totalCacheCapacity = size * 2;
	private static final int cacheConcurrencyLevel = threads * 2;
	private static final int threadsPerSegment = threads / 2;
	private static final int submitChunkSize = size / threadsPerSegment;
	
	private ICache<String, ValueBox> testMap;
	private CountDownLatch testSync;
	private CountDownLatch startTimeSync;
	private volatile long startTime;

	public static final void main(String[] args) {
		try {
			new TestCache().testCache();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	static void shuffleArray(TestElement[] ar) {
		Random rnd = new Random();
		for (int i = ar.length - 1; i > 0; i--) {
			int index = rnd.nextInt(i + 1);
			TestElement a = ar[index];
			ar[index] = ar[i];
			ar[i] = a;
		}
	}

	public void testCache() throws Exception {
		// initialize countdownlatch
		testSync = new CountDownLatch(threads);
		startTimeSync = new CountDownLatch(1);

		// initialize values for success/failure statistics and timings
		long writeTimes = 0L;
		long readTimes = 0L;

		// set up a threadpool for the test
		final ExecutorService testThreads = Executors.newFixedThreadPool(threads);

		// generate test data
		final TestElement[] firstDataSet = new TestElement[size];
		final TestElement[] secondDataSet = new TestElement[size];
		for (int i = 0; i < size; i++) {
			// create data for first batch
			byte[] firstBytes = new byte[recordSize];
			ThreadLocalRandom.current().nextBytes(firstBytes);
			firstDataSet[i] = new TestElement(Integer.toString(i), new ValueBox(firstBytes));

			// create data for second batch
			byte[] secondBytes = new byte[recordSize];
			ThreadLocalRandom.current().nextBytes(secondBytes);
			secondDataSet[i] = new TestElement(Integer.toString(size + i), new ValueBox(secondBytes));
		}

		// prepare worker callables
		Future<List<Callable<Long>>> readPrimer = testThreads.submit(new Callable<List<Callable<Long>>>() {
			@Override
			public List<Callable<Long>> call() throws Exception {
				final List<Callable<Long>> writeCallables = new ArrayList<Callable<Long>>();
				for (int j = 0; j < threadsPerSegment; j++) {
					TestElement[] setTwoValues = new TestElement[submitChunkSize];
					System.arraycopy(secondDataSet, j * submitChunkSize, setTwoValues, 0, submitChunkSize);
					shuffleArray(setTwoValues);
					writeCallables.add(new CacheWriter(setTwoValues));
				}
				return writeCallables;
			}

		});
		Future<List<Callable<Long>>> writePrimer = testThreads.submit(new Callable<List<Callable<Long>>>() {
			@Override
			public List<Callable<Long>> call() throws Exception {
				final List<Callable<Long>> readCallables = new ArrayList<Callable<Long>>();
				for (int j = 0; j < threadsPerSegment; j++) {
					TestElement[] setOneValues = new TestElement[submitChunkSize];
					System.arraycopy(firstDataSet, j * submitChunkSize, setOneValues, 0, submitChunkSize);
					shuffleArray(setOneValues);
					readCallables.add(new CacheReader(setOneValues));
				}
				return readCallables;
			}

		});
		
		// retrieve prepared callables
		final List<Callable<Long>> writeCallables = writePrimer.get();
		final List<Callable<Long>> readCallables = readPrimer.get();
		
		// write an informational message
		System.out.println("Finished generating test data.");

		// run this many times
		for (int i = 0; i < testIterations + 1; i++) {
			// initialize a cache implementation
			// testMap = new MapCache<String, ValueBox>(totalCacheCapacity, 1f);
			// testMap = new SemaphoreLockedMapCache<String, ValueBox>(totalCacheCapacity, 1f, cacheConcurrencyLevel);
			// testMap = new ConcurrentMapCache<String, ValueBox>(cacheConcurrencyLevel, totalCacheCapacity, 1f);
			testMap = new SE7ConcurrentMapCache<String, ValueBox>(cacheConcurrencyLevel, totalCacheCapacity, 1f);
			// testMap = new Ehcache<String, ValueBox>(totalCacheCapacity);
			// testMap = new GuavaCache<String, ValueBox>(cacheConcurrencyLevel, totalCacheCapacity);
			// testMap = new JCSCache<String,ValueBox>(totalCacheCapacity);
			// testMap = new NitroCache<String, ValueBox>(totalCacheCapacity);
			
			// prime the cache
			for (TestElement element : firstDataSet) {
				testMap.put(element.getKey(), element.getValue());
			}
			
			// log a status
			System.out.println("Finished priming cache for iteration " + i + ".");

			// submit writes
			final List<Future<Long>> writeFutures = new ArrayList<Future<Long>>();
			for (final Callable<Long> callableChunk : writeCallables) {
				writeFutures.add(testThreads.submit(callableChunk));
			}

			// submit reads
			List<Future<Long>> readFutures = new ArrayList<Future<Long>>();
			for (Callable<Long> callableChunk : readCallables) {
				readFutures.add(testThreads.submit(callableChunk));
			}
			
			// wait for all threads to reach readiness
			try {
				testSync.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// force garbage collection before starting
			System.gc();
			
			// set the test start time and release all of the test threads
			startTime = System.nanoTime();
			startTimeSync.countDown();

			// retrieve writes
			long iterationWriteTime = 0L;
			for (final Future<Long> returnValue : writeFutures) {
				iterationWriteTime += returnValue.get();
			}

			// retrieve reads
			long iterationReadTime = 0L;
			for (Future<Long> returnValue : readFutures) {
				iterationReadTime += returnValue.get();
			}

			// clean up after testing
			testMap.destroy();
			System.gc();

			// process results
			if (i > 0) {
				writeTimes += iterationWriteTime;
				System.out.println("Iteration " + i + " average wrie time: " + iterationWriteTime / threadsPerSegment / size + "ns");
				readTimes += iterationReadTime;
				System.out.println("Iteration " + i + " average read time: " + iterationReadTime / threadsPerSegment / size + "ns");
			}
		}

		System.out.println("Overall average write time: " + (writeTimes / threadsPerSegment / size / testIterations) + "ns");
		System.out.println("Overall average read time: " + (readTimes / threadsPerSegment / size / testIterations) + "ns");

		// shut down the worker threadpool
		testThreads.shutdown();
	}

	private class CacheReader implements Callable<Long> {
		private final TestElement[] elements_;

		private CacheReader(TestElement[] readElements) {
			elements_ = readElements;
		}

		@Override
		public Long call() throws Exception {
			testSync.countDown();
			startTimeSync.await();

			for (TestElement element : elements_) {
				if (!testMap.get(element.getKey()).equals(element.getValue())) {
					throw new Exception("Read failed for key " + element.getKey() + ".  Returned value was not " + element.getValue() + ".");
				}
			}
			final long readEndTime = System.nanoTime();
			return readEndTime - startTime;
		}

	}

	private class CacheWriter implements Callable<Long> {
		private final TestElement[] elements_;

		private CacheWriter(TestElement[] writeElements) {
			elements_ = writeElements;
		}

		@Override
		public Long call() throws Exception {
			testSync.countDown();
			startTimeSync.await();

			for (TestElement element : elements_) {
				testMap.put(element.getKey(), element.getValue());
			}
			final long writeEndTime = System.nanoTime();
			return writeEndTime - startTime;
		}
	}
}
