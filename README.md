testcache
=========

Concurrent Read/Write Performance Test for Java Maps/Caches

This map/cache performance test executes concurrent read/write operations with the desired number of threads, with the desired number of samples of the desired size.

The test operates via a simple pattern:
1) primes the cache with a set of values
2) concurrently performs randomly ordered reads, with validation, of primed values and randomly ordered writes

Support is included, out of the box, for testing against a simple Synchronized HashMap, three variants of Concurrent HashMaps, one lock-free HashMap, an Ehcache-based cache, a Guava-based Cache, a JCS-based cache, a NitroCache-based, and a MapDB-based cache.

On my Core i5-4570S desktop PC with 16GB of DDR3-2400 cas11 ram running Arch Linux with a 3.15 kernel within Eclipse on Oracle JDK 1.8.0, I see the following performance figures for each of these storage units with 4 threads (2 read 2 write) using a sample size of 1024 bytes and 2M reads/writes.

Synchronized HashMap:
 - Overall average write time: 537ns
 - Overall average read time: 457ns

ReadWriteLock Synchronized HashMap:
 - Overall average write time: 435ns
 - Overall average read time: 426ns

ConcurrentHashMap:
 - Overall average write time: 176ns
 - Overall average read time: 48ns

ConcurrentHashMap (SE7 code backported to Java 8):
 - Overall average write time: 93ns
 - Overall average read time: 67ns

Ehcache (LRU/eternal/heap only/max capacity set):
 - Overall average write time: 774ns
 - Overall average read time: 631ns

Guava Cache (initial/max capacity set):
 - Overall average write time: 622ns
 - Overall average read time: 134ns

Guava ConcurrentHashMap:
 - Overall average write time: 68ns
 - Overall average read time: 45ns

JCS Cache (max capacity set):
 - Overall average write time: 2039ns
 - Overall average read time: 699ns

NitroCache (FIFO):
 - Overall average write time: 1743ns
 - Overall average read time: 1404ns

Non-Blocking HashMap:
 - Overall average write time: 319ns
 - Overall average read time: 238ns
 
MapDB (on-heap):
 - Overall average write time: 4005ns
 - Overall average read time: 2359ns
 
