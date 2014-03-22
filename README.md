testcache
=========

Concurrent Read/Write Performance Test for Java Maps/Caches

This map/chache performance test performs concurrent read/write operations with the desired number of threads, with the desired number of samples with the desired size.

The test operates via a simple pattern:
1) primes the cache with a set of values
2) concurrently performs randomly ordered validating reads as well as randomly ordered writes against the cache, with reading the primed values and writing new values

Support is included, out of the box, for testing against a simple Synchronized HashMap, a ConcurrentHashMap, and a Guava-based Cache.

On my Core i5-4570S desktop PC with 16GB of ram running Gentoo Linux with a 3.13.5 kernel within Eclipse on Oracle JDK 1.7.0u51, I see the following performance figures for each of these storage units.

Synchronized HashMap:
Overall average write time: 471ns
Overall average read time: 587ns

Concurrent HashMap:
Overall average write time: 71ns
Overall average read time: 125ns

Guava Cache:
Overall average write time: 127ns
Overall average read time: 326ns
