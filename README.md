testcache
=========

Concurrent Read/Write Performance Test for Java Maps/Caches

This map/cache performance test executes concurrent read/write operations with the desired number of threads, with the desired number of samples of the desired size.

The test operates via a simple pattern:
1) primes the cache with a set of values
2) concurrently performs randomly ordered reads, with validation, of primed values and randomly ordered writes

Support is included, out of the box, for testing against a simple Synchronized HashMap, three variants of Concurrent HashMaps, one lock-free HashMap, an Ehcache-based cache, a Guava-based Cache, a JCS-based cache, a NitroCache-based, and a MapDB-based cache.

On my Core i5-4570S desktop PC with 16GB of DDR3-2400 cas11 ram running Arch Linux with a 3.15 kernel within Eclipse on Oracle JDK 1.8.0 and a minimal XFCE desktop environment, I see the following performance figures for each of these storage units with 4 threads (2 read 2 write) using a sample size of 1024 bytes and 2M reads/writes on a 10GB heap with stock GC configs.

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

JCS Cache (max capacity set):
 - Overall average write time: 2039ns
 - Overall average read time: 699ns

NitroCache (FIFO):
 - Overall average write time: 1743ns
 - Overall average read time: 1404ns

Non-Blocking HashMap (Cliff Click):
 - Overall average write time: 319ns
 - Overall average read time: 238ns
 
MapDB (on-heap):
 - Overall average write time: 4005ns
 - Overall average read time: 2359ns
 

The same test conditions on a system with a pair of Xeon X5520 CPUs and 16GB of DDR3-1066 CAS7 ram, but on Windows 7 and with 16 test threads show:
 
Synchronized HashMap:
 - Overall average write time: 932ns
 - Overall average read time: 646ns

ReadWriteLock Synchronized HashMap:
 - Overall average write time: 1103ns
 - Overall average read time: 882ns

ConcurrentHashMap:
 - Overall average write time: 104ns
 - Overall average read time: 98ns

ConcurrentHashMap (SE7 code backported to Java 8):
 - Overall average write time: 107ns
 - Overall average read time: 80ns

Ehcache (LRU/eternal/heap only/max capacity set):
 - Overall average write time: 317ns
 - Overall average read time: 262ns

Guava Cache (initial/max capacity set):
 - Overall average write time: 571ns
 - Overall average read time: 132ns

JCS Cache (max capacity set):
 - Overall average write time: 1725ns
 - Overall average read time: 853ns

NitroCache (FIFO):
 - Overall average write time: 824ns
 - Overall average read time: 222ns

Non-Blocking HashMap (Cliff Click):
 - Overall average write time: 893ns
 - Overall average read time: 207ns
 
MapDB (on-heap):
 - Overall average write time: 3508ns
 - Overall average read time: 1182ns
 
And a subset of the tests on the same box, but with 32 threads:

ConcurrentHashMap:
 - Overall average write time: 72ns
 - Overall average read time: 98ns

ConcurrentHashMap (SE7 code backported to Java 8):
 - Overall average write time: 81ns
 - Overall average read time: 78ns
 
Guava Cache (initial/max capacity set):
 - Overall average write time: 392ns
 - Overall average read time: 101ns

Ehcache (LRU/eternal/heap only/max capacity set):
 - Iteration 8 average write time: 244ns
 - Iteration 8 average read time: 284ns
 
And the same tests, but with 64 threads:

ConcurrentHashMap:
 - Overall average write time: 57ns
 - Overall average read time: 95ns

ConcurrentHashMap (SE7 code backported to Java 8):
 - Overall average write time: 51ns
 - Overall average read time: 79ns
 
Guava Cache (initial/max capacity set):
 - Overall average write time: 268ns
 - Overall average read time: 83ns

Ehcache (LRU/eternal/heap only/max capacity set):
 - Overall average write time: 213ns
 - Overall average read time: 258ns
 
And the same tests, but with 128 threads:

ConcurrentHashMap:
 - Overall average write time: 43ns
 - Overall average read time: 90ns

ConcurrentHashMap (SE7 code backported to Java 8):
 - Overall average write time: 55ns
 - Overall average read time: 70ns
 
Guava Cache (initial/max capacity set):
 - Overall average write time: 215ns
 - Overall average read time: 84ns

Ehcache (LRU/eternal/heap only/max capacity set):
 - Overall average write time: 192ns
 - Overall average read time: 239ns
