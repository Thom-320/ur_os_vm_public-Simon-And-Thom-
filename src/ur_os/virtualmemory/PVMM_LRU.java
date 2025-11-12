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
public class PVMM_LRU extends ProcessVirtualMemoryManager{

    public PVMM_LRU(){
        type = ProcessVirtualMemoryManagerType.LRU;
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

        return tracked == 0 ? -1 : order[tracked - 1];
    }
    
}
