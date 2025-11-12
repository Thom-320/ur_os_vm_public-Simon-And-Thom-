/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ur_os.virtualmemory;

import java.util.LinkedList;

/**
 *
 * @author user
 */
public class PVMM_LFU extends ProcessVirtualMemoryManager{

    public PVMM_LFU(){
        type = ProcessVirtualMemoryManagerType.LFU;
    }
    
    @Override
    public int getVictim(LinkedList<Integer> memoryAccesses, int loaded) {
        if (memoryAccesses == null || memoryAccesses.isEmpty() || loaded <= 0) {
            return -1;
        }

        java.util.HashSet<Integer> activePages = new java.util.HashSet<>(Math.max(loaded * 2, 4));
        int[] order = new int[loaded];
        int tracked = 0;

        java.util.Iterator<Integer> backwards = memoryAccesses.descendingIterator();
        while (backwards.hasNext() && tracked < loaded) {
            int page = backwards.next();
            if (activePages.add(page)) {
                order[tracked++] = page;
            }
        }

        if (tracked == 0) {
            return -1;
        }

        java.util.HashMap<Integer, Integer> frequency = new java.util.HashMap<>(tracked * 2);
        for (Integer pageObj : memoryAccesses) {
            if (pageObj == null) {
                continue;
            }
            int page = pageObj;
            if (activePages.contains(page)) {
                Integer count = frequency.get(page);
                frequency.put(page, count == null ? 1 : count + 1);
            }
        }

        int victim = order[tracked - 1];
        int minFrequency = Integer.MAX_VALUE;

        for (int i = tracked - 1; i >= 0; i--) {
            int page = order[i];
            Integer count = frequency.get(page);
            int pageFrequency = count == null ? 0 : count;
            if (pageFrequency < minFrequency) {
                minFrequency = pageFrequency;
                victim = page;
            }
        }

        return victim;
    }
    
}
