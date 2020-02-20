package org.mkosem;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import org.mkosem.impl.ConcurrentMapCache;
import org.mkosem.impl.Ehcache;
import org.mkosem.impl.GuavaCache;
import org.mkosem.impl.JCSCache;
import org.mkosem.impl.MapCache;
import org.mkosem.impl.NitroCache;
import org.mkosem.impl.NonBlockingMapCache;
import org.mkosem.impl.OnHeapMapDBCache;
import org.mkosem.impl.ReadWriteLockMapCache;
import org.mkosem.impl.SE7ConcurrentMapCache;

/*
 * Copyright 2014 Matt Kosem
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class TestCache {
	// config values
	private static final int threads = 4;// must be evenly divisible by two
	private static final int size = 1000000;// the total number of active test objects - peak capacity will be 2* this value
	private static final int recordSize = 256;// size, in bytes, of the payload of each object
	private static final int testIterations = 8;// the number of measured test iterations (note, one primer iteration occurs in addition to this count)

	// calculated config values
	private static final int totalCacheCapacity = size * 2;
	private static final int cacheConcurrencyLevel = threads * 2;
	private static final int threadsPerSegment = threads / 2;
	private static final int submitChunkSize = size / threadsPerSegment;

	// iteration-specific test members
	private ICache<String, ValueBox> testMap;
	private CountDownLatch testSync;
	private CountDownLatch startTimeSync;
	private volatile long startTime;

	public static final void main(String[] args) {
		try {
			new TestCache().testCache();
		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	static void shuffleArray(TestElement[] ar) {
		final Random rnd = ThreadLocalRandom.current();
		for (int i = ar.length - 1; i > 0; i--) {
			final int index = rnd.nextInt(i + 1);
			final TestElement a = ar[index];
			ar[index] = ar[i];
			ar[i] = a;
		}
	}

	@SuppressWarnings("unchecked")
	public void testCache() throws Exception {
		// set up a threadpool for the test
		final ExecutorService testThreads = Executors.newFixedThreadPool(threads);

		// generate test data
		final TestElement[] firstDataSet = java.util.stream.IntStream.range(0, size).parallel().mapToObj(i -> new TestElement(Integer.toString(i), new ValueBox(recordSize))).toArray(TestElement[]::new);
		final TestElement[] secondDataSet = java.util.stream.IntStream.range(0, size).parallel().mapToObj(i -> new TestElement(Integer.toString(size + i), new ValueBox(recordSize))).toArray(TestElement[]::new);

		// prepare worker callables
		final Callable<Long>[] writeCallables = java.util.stream.IntStream.range(0, threadsPerSegment).parallel().mapToObj(j -> {
			final TestElement[] setTwoValues = new TestElement[submitChunkSize];
			System.arraycopy(secondDataSet, j * submitChunkSize, setTwoValues, 0, submitChunkSize);
			shuffleArray(setTwoValues);
			return new CacheWriter(setTwoValues);
		}).toArray(Callable[]::new);
		final Callable<Long>[] readCallables = java.util.stream.IntStream.range(0, threadsPerSegment).parallel().mapToObj(j -> {
			final TestElement[] setOneValues = new TestElement[submitChunkSize];
			System.arraycopy(firstDataSet, j * submitChunkSize, setOneValues, 0, submitChunkSize);
			shuffleArray(setOneValues);
			return new CacheReader(setOneValues);
		}).toArray(Callable[]::new);

		// write an informational message
		System.out.println("Finished generating test data.");

		// compile a set of test targets
		Map<Class<? extends ICache>, TestResult> testCandidates = new LinkedHashMap<>();
		testCandidates.put(MapCache.class, new TestResult());
		testCandidates.put(ReadWriteLockMapCache.class, new TestResult());
		testCandidates.put(ConcurrentMapCache.class, new TestResult());
		testCandidates.put(SE7ConcurrentMapCache.class, new TestResult());
		testCandidates.put(NonBlockingMapCache.class, new TestResult());
		testCandidates.put(Ehcache.class, new TestResult());
		testCandidates.put(GuavaCache.class, new TestResult());
		testCandidates.put(JCSCache.class, new TestResult());
		testCandidates.put(NitroCache.class, new TestResult());
		testCandidates.put(OnHeapMapDBCache.class, new TestResult());

		// run test sequence testIterations times
		for (int i = 0; i < testIterations + 1; i++) {
			// run the test for this type
			for (Entry<Class<? extends ICache>, TestResult> cacheType : testCandidates.entrySet()) {
				// initialize the implementation
				testMap = cacheType.getKey().getConstructor(int.class, int.class).newInstance(new Object[] { totalCacheCapacity, cacheConcurrencyLevel });

				// set the description variable
				cacheType.getValue().setDescription(testMap.getDescription());

				// initialize countdownlatches
				testSync = new CountDownLatch(threads);
				startTimeSync = new CountDownLatch(1);

				// prime the cache with values that will be read by the reader threads
				Arrays.stream(firstDataSet).parallel().forEach(e -> testMap.put(e.getKey(), e.getValue()));

				// log a status
				System.out.println("(" + testMap.getDescription() + ") Finished priming cache for iteration " + i + ".");

				// submit reads and writes
				final Future<Long>[] writeFutures = Arrays.stream(writeCallables).map(c -> testThreads.submit(c)).toArray(Future[]::new);
				final Future<Long>[] readFutures = Arrays.stream(readCallables).map(c -> testThreads.submit(c)).toArray(Future[]::new);

				// wait for all threads to reach readiness
				try {
					testSync.await();
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}

				// force garbage collection before starting
				System.gc();

				// set the test start time and release all of the test threads
				startTime = System.nanoTime();
				startTimeSync.countDown();

				// retrieve reads and writes
				final long iterationWriteTime = Arrays.stream(writeFutures).mapToLong((e) -> {
					try {
						return e.get();
					} catch (final Exception ex) {
						throw new RuntimeException("An exception occurred.", ex);
					}
				}).sum();
				final long iterationReadTime = Arrays.stream(readFutures).mapToLong((e) -> {
					try {
						return e.get();
					} catch (final Exception ex) {
						throw new RuntimeException("An exception occurred.", ex);
					}
				}).sum();

				// process results
				if (i > 0) {
					cacheType.getValue().addWriteTime(iterationWriteTime);
					cacheType.getValue().addReadTime(iterationReadTime);
					System.out.println("(" + testMap.getDescription() + ") Iteration " + i + " average write time: " + iterationWriteTime / threadsPerSegment / size + "ns");
					System.out.println("(" + testMap.getDescription() + ") Iteration " + i + " average read time: " + iterationReadTime / threadsPerSegment / size + "ns");
				}

				// clean up after testing
				testMap.destroy();
				testMap = null;
				System.gc();
			}
		}

		// shut down the worker threadpool
		testThreads.shutdown();

		// print final status info
		System.out.println("\n\nResults:");
		for (Entry<Class<? extends ICache>, TestResult> cacheType : testCandidates.entrySet()) {
			System.out.println(cacheType.getValue().getResultData());
		}
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

			Arrays.stream(elements_).forEach(e -> {
				if (!testMap.get(e.getKey()).equals(e.getValue())) {
					throw new RuntimeException("Read failed for key " + e.getKey() + ".  Returned value was not " + e.getValue() + ".");
				}
			});
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

			Arrays.stream(elements_).forEach(e -> testMap.put(e.getKey(), e.getValue()));
			final long writeEndTime = System.nanoTime();
			return writeEndTime - startTime;
		}
	}

	private class TestResult {
		private long writeTimes_ = 0L;
		private long readTimes_ = 0L;
		private String cacheDescription_ = "";

		private void addReadTime(long argReadTime) {
			readTimes_ += argReadTime;
		}

		private void addWriteTime(long argWriteTime) {
			writeTimes_ += argWriteTime;
		}

		private String getResultData() {
			StringBuilder resultStringBuilder = new StringBuilder("\n").append(cacheDescription_).append(":\n");
			resultStringBuilder.append("Overall average write time: " + (writeTimes_ / threadsPerSegment / size / testIterations) + "ns").append("\n");
			resultStringBuilder.append("Overall average read time: " + (readTimes_ / threadsPerSegment / size / testIterations) + "ns").append("\n");
			return resultStringBuilder.toString();
		}

		private void setDescription(String argDescription) {
			cacheDescription_ = argDescription;
		}
	}
}