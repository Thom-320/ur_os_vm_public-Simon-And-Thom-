# UR_OS
This tool is an academic operating systems simulator developed in Java that allows the implementation of process planning, memory allocation, free memory management, and virtual memory algorithms.
Created by Pedro Wightman. Associate Professor at the School of Engineering, Science, and Technology at Universidad del Rosario, Bogota, Colombia.

This version only has some implemented algorithms, so it can be used to create assignments in OS courses: 

Process Planning: FCFS
Memory Allocation: Contiguous Allocation and Paging
Free Memory Management: First Fit (Memory Slots) and FreeFrames manager
Virtual Memory: FIFO (for victim page selection)

Enjoy using UR-OS!

## VM: Quick Usage

- Single run via `vm.properties` (recommended):
  1. Create a `vm.properties` file in the project root:
     - `smm=PAGING`
     - `vm.enabled=true`
     - `vm.policy=LRU`  (or `FIFO|LFU|MFU`)
     - `vm.frames=3`
  2. Run: `ant run`
  3. Look for the `VM_CSV:` line near the end; it prints: `policy,frames,vm_accesses,vm_faults,vm_evictions,vm_dirty,fault_rate,EAT_ns`.

- Sweep and figure:
  - `./scripts/run_vm_sweep.sh`
  - Outputs `results/results_vm.csv` and `figs/miss_curves_vm.png` (headless-safe). If desired, set `VM_SEED` or `-Dvm.seed` to log a seed.
