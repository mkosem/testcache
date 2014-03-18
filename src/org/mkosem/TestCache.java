package org.mkosem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class TestCache {
	private final int threads = 4;
	private final int threadsPerSegment = threads / 2;
	private final int cacheConcurrencyLevel = threads * 2;
	private final int size = 2000000;
	private final int recordSize = 1024;
	private final int submitChunkSize = size / threadsPerSegment;

	public static final void main(String[] args) {
		try {
			new TestCache().testCache();
		} catch (Exception e) {
			System.out.println("A fatal error occurred during testing: " + e.toString());
		}
	}

	public void testCache() throws Exception {
		// initialize values for success/failure statistics and timings
		long writeTimes = 0L;
		long readTimes = 0L;
		boolean writeSuccess = true;
		boolean readSuccess = true;
		
		// create a threadpool for generic non-test-related work
		final ExecutorService workerThreads = Executors.newFixedThreadPool(2);

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
		int iterations = 8;
		for (int i = 0; i < iterations && readSuccess && writeSuccess; i++) {
			// initialize a cache implementation
			// final ICache<Object,Object> testMap = new MapCache<Object,Object>(cacheConcurrencyLevel, (int) (size * 2), 1f);
			final ICache<Object, Object> testMap = new ConcurrentMapCache<Object, Object>(cacheConcurrencyLevel, (int) (size * 2), 1f);
			// final ICache<Object,Object> testMap = new GuavaCache<Object,Object>(cacheConcurrencyLevel, (int) (size * 2), 1f);

			// prepare worker callables
			Future<List<Callable<Boolean>>> readPrimer = workerThreads.submit(new Callable<List<Callable<Boolean>>>() {
				@Override
				public List<Callable<Boolean>> call() throws Exception {
					final List<Callable<Boolean>> writeCallables = new ArrayList<Callable<Boolean>>();
					for (int j = 0 ; j < threadsPerSegment; j++) {
						TestElement[] setTwoValues = new TestElement[submitChunkSize];
						System.arraycopy(secondDataSet, j * submitChunkSize, setTwoValues, 0, submitChunkSize);
						shuffleArray(setTwoValues);
						writeCallables.add(new CacheWriter(setTwoValues, testMap));
					}
					return writeCallables;
				}
				
			});
			Future<List<Callable<Boolean>>> writePrimer = workerThreads.submit(new Callable<List<Callable<Boolean>>>() {
				@Override
				public List<Callable<Boolean>> call() throws Exception {
					final List<Callable<Boolean>> readCallables = new ArrayList<Callable<Boolean>>();
					for (int j = 0 ; j < threadsPerSegment; j++) {
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
			final List<Callable<Boolean>> writeCallables = writePrimer.get();
			final List<Callable<Boolean>> readCallables = readPrimer.get();
			
			// log a status
			System.out.println("Finished preparing test elements for iteration " + i + ".");

			// force garbage collection before starting
			System.gc();
			
			// set up a threadpool for the test
			final ExecutorService testThreads = Executors.newFixedThreadPool(threads);
			
			// kick off writes
			Future<Long> writeFuture = workerThreads.submit(new Callable<Long>() {
						@Override
						public Long call() throws Exception {
							// submit writes
							final List<Future<Boolean>> writeFutures = new ArrayList<Future<Boolean>>();
							final long writeStartTime = System.nanoTime();
							for (final Callable<Boolean> callableChunk : writeCallables) {
								writeFutures.add(testThreads.submit(callableChunk));
							}

							// retrieve writes
							for (final Future<Boolean> returnValue : writeFutures) {
								returnValue.get();
							}
							final long writeEndTime = System.nanoTime();
							return writeEndTime - writeStartTime;
						}

					});

			// kick off reads
			Future<Long> readFuture = workerThreads.submit(new Callable<Long>() {
						@Override
						public Long call() throws Exception {
							// submit reads
							List<Future<Boolean>> readFutures = new ArrayList<Future<Boolean>>();
							Long readStartTime = System.nanoTime();
							for (Callable<Boolean> callableChunk : readCallables) {
								readFutures.add(testThreads.submit(callableChunk));
							}

							// retrieve reads
							for (Future<Boolean> returnValue : readFutures) {
								if (!returnValue.get()) {
									throw new Exception("Problem reading data.");
								}
							}

							final long readEndTime = System.nanoTime();
							return readEndTime - readStartTime;
						}

					});

			// read results
			try {
				long iterationWriteTime = writeFuture.get();
				writeTimes += iterationWriteTime;
				System.out.println("Iteration " + i + " average wrie time: " + iterationWriteTime / size + "ns");
			} catch (Exception e) {
				writeSuccess = false;
				e.printStackTrace();
			}
			try {
				long iterationReadTime = readFuture.get();
				readTimes += iterationReadTime;
				System.out.println("Iteration " + i + " average read time: " + iterationReadTime / size + "ns");
			} catch (Exception e) {
				readSuccess = false;
				e.printStackTrace();
			}

			// shut down the executor
			testThreads.shutdown();
		}

		if (readSuccess && writeSuccess) {
			System.out.println("Overall average write time: "
					+ (writeTimes / size / iterations) + "ns");
			System.out.println("Overall average read time: "
					+ (readTimes / size / iterations) + "ns");
		} else {
			System.out.println("TEST FAILED! Write status: " + writeSuccess
					+ " Read Status: " + readSuccess);
		}
		
		// shut down the worker threadpool
		workerThreads.shutdown();
	}

	private class CacheWriter implements Callable<Boolean> {
		private final TestElement[] elements_;
		private final ICache<Object, Object> cache_;

		private CacheWriter(TestElement[] writeElements,
				ICache<Object, Object> cache) {
			elements_ = writeElements;
			cache_ = cache;
		}

		@Override
		public Boolean call() throws Exception {
			for (TestElement element : elements_) {
				cache_.put(element.getKey(), element.getValue());
			}
			return true;
		}
	}

	private class CacheReader implements Callable<Boolean> {
		private final TestElement[] elements_;
		private final ICache<Object, Object> cache_;

		private CacheReader(TestElement[] readElements,
				ICache<Object, Object> cache) {
			elements_ = readElements;
			cache_ = cache;
		}

		@Override
		public Boolean call() throws Exception {
			boolean success = true;
			for (TestElement element : elements_) {
				if (!cache_.get(element.getKey()).equals(element.getValue())) {
					success = false;
				}
			}
			return success;
		}

	}

	static void shuffleArray(TestElement[] ar) {
		Random rnd = new Random();
		for (int i = ar.length - 1; i > 0; i--) {
			int index = rnd.nextInt(i + 1);
			// Simple swap
			TestElement a = ar[index];
			ar[index] = ar[i];
			ar[i] = a;
		}
	}
}
