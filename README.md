testcache
=========

Concurrent Read/Write Performance Test for Java Maps/Caches

This map/chache performance test performs concurrent read/write operations with the desired number of threads, with the desired number of samples with the desired size.

The test operates via a simple pattern:
1) primes the cache with a set of values
2) concurrently performs randomly ordered validating reads as well as randomly ordered writes against the cache, with reading the primed values and writing new values

Support is included, out of the box, for testing against a simple Synchronized HashMap, a ConcurrentHashMap, a back-ported Java SE 7 ConcurrentHashMap an Ehcache-based cache, a Guava-based Cache, a JCS-based cache, and a NitroCache-based cache.

On my Core i5-4570S desktop PC, with 16GB of DDR3-2400 cas11 ram running Gentoo Linux with a 3.13.5 kernel within Eclipse on Oracle JDK 1.8.0, I see the following performance figures for each of these storage units on an 8GB heap with a total size of 4 million records (2 million read (preloaded) + 2 million write).

Results with 4 threads (2 read 2 write):

Synchronized HashMap:
 - Overall average write time: 368ns
 - Overall average read time: 572ns

ConcurrentHashMap:
 - Overall average write time: 52ns
 - Overall average read time: 191ns

ConcurrentHashMap (SE7 - backported):
 - Overall average write time: 81ns
 - Overall average read time: 130ns

Ehcache (LRU/eternal/heap only/max capacity set):
 - Overall average write time: 329ns
 - Overall average read time: 380ns

Guava Cache (initial/max capacity set):
 - Overall average write time: 172ns
 - Overall average read time: 567ns

JCS Cache (max capacity set):
 - Overall average write time: 781ns
 - Overall average read time: 1105ns

NitroCache (FIFO):
 - Overall average write time: 504ns
 - Overall average read time: 570ns

 
Results with 128 threads (64 read 64 write):

Synchronized HashMap:
 - Overall average write time: 344ns
 - Overall average read time: 590ns

ConcurrentHashMap:
 - Overall average write time: 29ns
 - Overall average read time: 71ns

ConcurrentHashMap (SE7 - backported):
 - Overall average write time: 28ns
 - Overall average read time: 81ns

Ehcache (LRU/eternal/heap only/max capacity set):
 - Overall average write time: 297ns
 - Overall average read time: 262ns

Guava Cache (initial/max capacity set):
 - Overall average write time: 81ns
 - Overall average read time: 253ns

JCS Cache (max capacity set):
 - Overall average write time: 574ns
 - Overall average read time: 1149ns

NitroCache (FIFO):
 - Overall average write time: 214ns
 - Overall average read time: 572ns
 