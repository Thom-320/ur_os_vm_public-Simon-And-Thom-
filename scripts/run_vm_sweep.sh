#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

mkdir -p results logs figs
CSV="results/results_vm.csv"
# Fresh CSV by default
rm -f "$CSV"
echo "policy,frames,vm_accesses,vm_faults,vm_evictions,vm_dirty,fault_rate,EAT_ns" > "$CSV"

policies=(FIFO LRU LFU MFU)
frames=(2 3 4 5 6 7 8)

for pol in "${policies[@]}"; do
  for f in "${frames[@]}"; do
    echo "Running policy=$pol frames=$f" >&2
    # Write config file for this run
    cat > vm.properties <<CFG
smm=PAGING
vm.enabled=true
vm.policy=$pol
vm.frames=$f
CFG
    ant run | tee "logs/${pol}_F${f}.log" | awk -F 'VM_CSV: ' '/VM_CSV:/ {print $2}' >> "$CSV"
  done
done

echo "Sweep completed. CSV at $CSV"

# Optional: build figure (headless)
if command -v javac >/dev/null 2>&1; then
  javac scripts/PlotMissCurves.java >/dev/null 2>&1 || true
  java -Djava.awt.headless=true -cp scripts PlotMissCurves "$CSV" figs/miss_curves_vm.png >/dev/null 2>&1 || true
fi
