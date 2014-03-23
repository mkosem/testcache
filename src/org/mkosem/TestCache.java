package org.mkosem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
		
	private CountDownLatch testSync;
	private CountDownLatch startTimeSync;
	private volatile long startTime;

	public static final void main(String[] args) {
		try {
			new TestCache().testCache();
		} catch (Exception e) {
			e.printStackTrace();
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
		final ExecutorService testThreads = Executors.newFixedThreadPool(threads + 1);

		// generate test data
		final TestElement[] firstDataSet = new TestElement[size];
		final TestElement[] secondDataSet = new TestElement[size];
		final Random random = new Random();
		for (int i = 0; i < size; i++) {
			// create data for first batch
			byte[] firstBytes = new byte[recordSize];
			random.nextBytes(firstBytes);
			firstDataSet[i] = new TestElement(Integer.toString(i), new ValueBox(firstBytes));

			// create data for second batch
			byte[] secondBytes = new byte[recordSize];
			random.nextBytes(secondBytes);
			secondDataSet[i] = new TestElement(Integer.toString(size + i), new ValueBox(secondBytes));
		}
		System.out.println("Finished generating test data.");

		// run this many times
		for (int i = 0; i < testIterations; i++) {
			// initialize a cache implementation
			// final ICache<String, ValueBox> testMap = new MapCache<String, ValueBox>(totalCacheCapacity, 1f);
			final ICache<String, ValueBox> testMap = new ConcurrentMapCache<String, ValueBox>(cacheConcurrencyLevel, totalCacheCapacity, 1f);
			// final ICache<String, ValueBox> testMap = new GuavaCache<String, ValueBox>(cacheConcurrencyLevel, totalCacheCapacity);
			// final ICache<String, ValueBox> testMap = new NitroCache<String, ValueBox>(totalCacheCapacity);
			// final ICache<String, ValueBox> testMap = new Ehcache<String, ValueBox>(totalCacheCapacity);
			// final ICache<String, ValueBox> testMap = new JCSCache<String,ValueBox>(totalCacheCapacity);

			// prepare worker callables
			Future<List<Callable<Long>>> readPrimer = testThreads.submit(new Callable<List<Callable<Long>>>() {
				@Override
				public List<Callable<Long>> call() throws Exception {
					final List<Callable<Long>> writeCallables = new ArrayList<Callable<Long>>();
					for (int j = 0; j < threadsPerSegment; j++) {
						TestElement[] setTwoValues = new TestElement[submitChunkSize];
						System.arraycopy(secondDataSet, j * submitChunkSize, setTwoValues, 0, submitChunkSize);
						shuffleArray(setTwoValues);
						writeCallables.add(new CacheWriter(setTwoValues, testMap));
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
						readCallables.add(new CacheReader(setOneValues, testMap));
					}
					return readCallables;
				}

			});

			// while we're waiting for our workers to be created, prime the cache
			for (TestElement element : firstDataSet) {
				testMap.put(element.getKey(), element.getValue());
			}

			// retrieve prepared callables
			final List<Callable<Long>> writeCallables = writePrimer.get();
			final List<Callable<Long>> readCallables = readPrimer.get();

			// log a status
			System.out.println("Finished preparing test elements for iteration " + i + ".");

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

			// submit start time task
			testThreads.submit(new Runnable() {
				@Override
				public void run() {
					try {
						testSync.await();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// force garbage collection before starting
					System.gc();
					
					// get the test start time
					startTime = System.nanoTime();
					startTimeSync.countDown();
				}
			});

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

			// destroy cache if necessary
			testMap.destroy();

			// process results
			writeTimes += iterationWriteTime;
			System.out.println("Iteration " + i + " average wrie time: " + iterationWriteTime / threadsPerSegment / size + "ns");
			readTimes += iterationReadTime;
			System.out.println("Iteration " + i + " average read time: " + iterationReadTime / threadsPerSegment / size + "ns");
		}

		System.out.println("Overall average write time: " + (writeTimes / threadsPerSegment / size / testIterations) + "ns");
		System.out.println("Overall average read time: " + (readTimes / threadsPerSegment / size / testIterations) + "ns");

		// shut down the worker threadpool
		testThreads.shutdown();
	}

	private class CacheReader implements Callable<Long> {
		private final TestElement[] elements_;
		private final ICache<String, ValueBox> cache_;

		private CacheReader(TestElement[] readElements, ICache<String, ValueBox> cache) {
			elements_ = readElements;
			cache_ = cache;
		}

		@Override
		public Long call() throws Exception {
			testSync.countDown();
			startTimeSync.await();

			for (TestElement element : elements_) {
				if (!cache_.get(element.getKey()).equals(element.getValue())) {
					throw new Exception("Read failed for key " + element.getKey() + ".  Returned value was not " + element.getValue() + ".");
				}
			}
			final long readEndTime = System.nanoTime();
			return readEndTime - startTime;
		}

	}

	private class CacheWriter implements Callable<Long> {
		private final TestElement[] elements_;
		private final ICache<String, ValueBox> cache_;

		private CacheWriter(TestElement[] writeElements, ICache<String, ValueBox> cache) {
			elements_ = writeElements;
			cache_ = cache;
		}

		@Override
		public Long call() throws Exception {
			testSync.countDown();
			startTimeSync.await();

			for (TestElement element : elements_) {
				cache_.put(element.getKey(), element.getValue());
			}
			final long writeEndTime = System.nanoTime();
			return writeEndTime - startTime;
		}
	}
}
