testcache
=========

Concurrent Read/Write Performance Test for Java Maps/Caches

This map/chache performance test performs concurrent read/write operations with the desired number of threads, with the desired number of samples with the desired size.

The test operates via a simple pattern:
1) primes the cache with a set of values
2) concurrently performs randomly ordered validating reads as well as randomly ordered writes against the cache, with reading the primed values and writing new values

Support is included, out of the box, for testing against a simple Synchronized HashMap, a ConcurrentHashMap, a back-ported Java SE 7 ConcurrentHashMap an Ehcache-based cache, a Guava-based Cache, a JCS-based cache, and a NitroCache-based cache.

On my Core i5-4570S desktop PC, with 16GB of DDR3-2400 cas11 ram running Gentoo Linux with a 3.13.5 kernel within Eclipse on Oracle JDK 1.8.0, I see the following performance figures for each of these storage units with 4 threads (2 read 2 write) on an 8GB heap with a total size of 4 million records (2 million read (preloaded) + 2 million write).

Synchronized HashMap:
 - Overall average write time: 431ns
 - Overall average read time: 580ns

ConcurrentHashMap:
 - Overall average write time: 54ns
 - Overall average read time: 186ns

ConcurrentHashMap (SE7 - backported):
 - Overall average write time: 76ns
 - Overall average read time: 132ns

Ehcache (LRU/eternal/heap only/max capacity set):
 - Overall average write time: 333ns
 - Overall average read time: 396ns

Guava Cache (initial/max capacity set):
 - Overall average write time: 168ns
 - Overall average read time: 545ns

JCS Cache (max capacity set):
 - Overall average write time: 775ns
 - Overall average read time: 1112ns

NitroCache (FIFO):
 - Overall average write time: 458ns
 - Overall average read time: 598ns
