testcache
=========

Concurrent Read/Write Performance Test for Java Maps/Caches

This map/chache performance test performs concurrent read/write operations with the desired number of threads, with the desired number of samples with the desired size.

The test operates via a simple pattern:
1) primes the cache with a set of values
2) concurrently performs randomly ordered validating reads as well as randomly ordered writes against the cache, with reading the primed values and writing new values

Support is included, out of the box, for testing against a simple Synchronized HashMap, a ConcurrentHashMap, a Guava-based Cache, and a NitroCache-based cache.

On my Core i5-4570S desktop PC with 16GB of DDR3-2400 cas11 ram running Gentoo Linux with a 3.13.5 kernel within Eclipse on Oracle JDK 1.7.0u51, I see the following performance figures for each of these storage units.

Synchronized HashMap:
Overall average write time: 471ns
Overall average read time: 587ns

Concurrent HashMap:
Overall average write time: 71ns
Overall average read time: 125ns

Ehcache (LRU/eternal/heap only/max capacity set):
Overall average write time: 334ns
Overall average read time: 395ns

Guava Cache (initial/max capacity set):
Overall average write time: 178ns
Overall average read time: 548ns

NitroCache (FIFO):
Overall average write time: 460ns
Overall average read time: 599ns
