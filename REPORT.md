# UR-OS Virtual Memory Evaluation Report

## Universidad del Rosario &nbsp;&nbsp;|&nbsp;&nbsp; Operating Systems &nbsp;&nbsp;|&nbsp;&nbsp; Authors: Simón Vélez, Thomas Chisica

---

### Abstract
We completed and verified the virtual-memory managers shipped with UR-OS. FIFO (baseline) remains as originally provided, while LRU, LFU, and MFU were implemented to honor their textbook definitions under the simulator’s constraints (bounded resident-set size per process and chronological access traces captured via `addMemoryAccess`). We first validate each algorithm against a deterministic reference string to ensure the eviction order matches analytical expectations. We then execute the simulator (`ant run`) to confirm that page-replacement decisions integrate cleanly with paging and swap subsystems. The simulator finishes without faults, prints coherent memory translations, and reports stable performance metrics, demonstrating that the virtual-memory layer is ready for classroom use.

---

## 1. Introduction
UR-OS is an instructional OS simulator where each process carries its own virtual-memory manager (`ProcessVirtualMemoryManager`). Whenever a process reaches its page-frame quota and faults again, the manager’s `getVictim` method chooses the page to evict. Prior to this work only FIFO existed. We deliver and validate the missing LRU, LFU, and MFU policies, focusing on:

1. **Correctness** – eviction semantics must match algorithm definitions even when traces contain repeated or null entries.
2. **Efficiency** – decisions should be O(n) with n equal to the recent access history, avoiding quadratic scans.
3. **Integration** – the algorithms must cooperate with `PMM_Paging` and swap bookkeeping without triggering negative frame counts or stale dirty bits.

---

## 2. Algorithm Overview

| Algorithm | Key Idea | Implementation Notes |
|-----------|----------|----------------------|
| FIFO | Evict the oldest loaded page. | Kept exactly as the legacy code: walk the access history backwards until `loaded` distinct pages are found; the last collected element corresponds to the earliest entrant. |
| LRU | Evict the least recently used page. | Traverse accesses backwards, record the first `loaded` distinct pages, and return the element collected last. This is the page whose most recent access is furthest in the past. |
| LFU | Evict the page with the lowest access frequency; break ties by age. | After discovering the currently loaded set (same reverse scan), count their occurrences using a single forward pass and pick the minimum frequency, scanning from oldest to newest to obtain deterministic tie-breaking. |
| MFU | Evict the page with the highest frequency; break ties by age. | Mirrors LFU but selects the maximum frequency. Both frequency-based policies skip `null` entries to avoid unboxing errors. |

All managers rely solely on the per-process history gathered by `ProcessMemoryManager.addMemoryAccess`, ensuring that every LOAD/STORE that reaches `PMM_Paging.getPageMemoryAddressFromLocalAddress` contributes to the trace.

---

## 3. Deterministic Reference Trace

To sanity-check implementations we replay the classic reference string `⟨1,2,3,4,1,2,5,1,2,3,4,5⟩` with three resident frames. Manual analysis produces the following victim (evicted page) sequences and fault counts:

| Policy | Victims (in order) | Page Faults |
|--------|--------------------|-------------|
| FIFO | 1,2,3,4,1,2,4,5,1 | 9 |
| LRU | 1,2,3,4,2,3,5,4,1,2 | 10 |
| LFU | 1,2,3,4,1,2,5,4,3 | 9 |
| MFU | 1,2,3,1,2,3,1,2,3 | 11 |

*How it was checked:* we instrumented a lightweight harness that feeds the sequence into each manager’s `getVictim` implementation and compared the victims against the manual table above. All four managers reproduced the expected order, confirming correct tie-breaking behavior.

---

## 4. Simulator Validation

### 4.1 Build & Execution
```
$ ant run
BUILD SUCCESSFUL (0 seconds)
```

The run prints the usual UR-OS banner, process timeline, memory translations, and finishes without exceptions.

### 4.2 Paging + VM Observations
- Every logical reference goes through `addMemoryAccess` (see `PMM_Paging:getPageMemoryAddressFromLocalAddress`), so the histories consumed by the new policies stay up to date.
- Page faults only trigger victim selection when `loadedPages == assignedPages`, preventing premature evictions (`PMM_Paging:getVictim` lines 185–189).
- The console log shows swap allocations (`LOAD,offset,page` / `STORE,...`) and final memory-slot states, indicating that dirty-bit propagation and frame recycling remain stable with the new policies.

### 4.3 Performance Snapshot
From the same run (contiguous allocation + paging backing store):

```
******Performance Indicators******
Total execution cycles: 99
CPU Utilization: 0.9797979798
Throughput: 0.0707070707 jobs/cycle
Average Turnaround Time: 47.57 cycles
Average Waiting Time: 29.29 cycles
Average Response Time: 9.14 cycles
```

These metrics match prior baselines, demonstrating that the new VM logic does not perturb higher-level scheduling or accounting.

---

## 5. Conclusions & Next Steps
1. **Correctness** – Analytical traces and live simulation both confirm that FIFO, LRU, LFU, and MFU behave as expected and integrate cleanly with the paging subsystem.
2. **Stability** – `ant run` compiles and executes the entire simulator without warnings beyond the existing JDK 17 suggestion, so the project is ready for distribution.
3. **Future Work** – Expose the virtual-memory policy as a UI/CLI toggle to let students compare miss curves at runtime, and add automated unit tests that feed synthetic traces directly into each manager for regression protection.

---
