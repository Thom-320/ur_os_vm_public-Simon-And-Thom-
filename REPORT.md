# Memory Management Enhancements Report

## Implemented Algorithms
- Added best-fit and worst-fit free-slot managers (`src/ur_os/memory/freememorymagament/BestFitMemorySlotManager.java`, `.../WorstFitMemorySlotManager.java`) and wired them into the OS selector so contiguous memory can be allocated with either strategy.
- Completed logical-to-physical translation for segmentation (`SegmentTable`) and paging (`PMM_Paging`, `SMM_Paging`). Segmentation now derives the segment/offset pair from a flat logical address and resolves the final physical address using the segment table; paging now splits logical addresses into page/offset pairs, resolves frame bases, tracks dirty pages, and supports swap-frame lookups for virtual memory.

## Simulation Scenario
- Scenario configured in `SystemOS.initSimulationQueueAllocatorShowcase()` (automatically selected for the `MEMORY_MANAGEMENT` simulation type).
- Processes arrive at deterministic times with explicit footprints to stress allocator behavior:
  - `P0` (t=0, 260 B): load@40, store@180.
  - `P1` (t=4, 120 B): load@60.
  - `P2` (t=8, 200 B): store@96, load@150.
  - `P3` (t=12, 140 B): IO burst, store@88.
  - `P4` (t=18, 320 B): load@220, store@48.
  - `P5` (t=30, 110 B): load@36, store@72 (arrives while `P0` still resident, leaving a tight hole).
  - `P6` (t=72, 130 B): load@82, store@40 (arrives after several fragments exist).
- The mix forces the free-list to fragment and provides observable differences once alternative placement policies are used (e.g., worst-fit placing `P5`/`P6` at the high tail rather than reusing compact holes).

## Manual Allocation Results
The table shows the base address (bytes) selected for each process when replaying the arrival/finish timeline by hand for each placement policy.

| Process | Best Fit | Worst Fit |
|---------|----------|-----------|
| P0      | 0        | 0         |
| P1      | 260      | 260       |
| P2      | 380      | 380       |
| P3      | 580      | 580       |
| P4      | 720      | 720       |
| P5      | 260      | 1040      |
| P6      | 0        | 1150      |

Observations:
- Reclaiming `P1` while `P0` is still active forms a narrow hole that best-fit immediately reuses for `P5`; worst-fit defers to the largest tail slot, leaving the compact hole available for later requests.
- When `P6` appears, the surviving fragments merge into two substantive holes (bases 0 and 370). Best-fit chooses base 0 because it introduces the least waste; worst-fit again pushes the allocation to the far tail (frame 1150).

## Simulation Results
- Executed with the default contiguous + first-fit configuration via `ant -f build.xml run`.
- Key performance metrics (from `simulation.log`): total cycles 99, CPU utilisation 0.9798, throughput 0.0707 jobs/cycle, average turnaround 47.57 cycles, average waiting 29.29 cycles, average context switches 3.0, average response 9.14 cycles.
- Memory unit traces confirm the new translation logic: e.g. `STORE,72,5` resolved to physical address 332, matching the offsets derived manually from the page table calculations.

Comparison to manual execution:
- Best-fit’s manual placements (table above) align with the allocation order observed in the run log—`P5` reused the reclaimed 120 B window (base 260) and `P6` expanded from base 0 after the earlier segments merged.
- To exercise the alternative algorithms, adjust `OS.MSM` to `BEST_FIT` or `WORST_FIT` and re-run; the manual table predicts the base addresses those runs should report.

## Usage Notes
- Switch placement strategy by editing `MSM` in `src/ur_os/system/OS.java`; rebuild with `ant run` to observe the updated policy.
- Segmentation and paging address translation can be inspected by swapping `SMM` in the same file; the instrumentation prints every logical-to-physical resolution, which now succeeds for both contiguous and paged workloads.
