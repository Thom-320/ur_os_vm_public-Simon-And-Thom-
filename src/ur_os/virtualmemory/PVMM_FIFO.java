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
public class PVMM_FIFO extends ProcessVirtualMemoryManager{

    public PVMM_FIFO(){
        type = ProcessVirtualMemoryManagerType.FIFO;
    }
    
    @Override
    public int getVictim(LinkedList<Integer> memoryAccesses, int loaded) {
        LinkedList<Integer> pages = new LinkedList<>();
        int size = memoryAccesses.size()-1;
        
        while(size >= 0 && pages.size()<loaded){
            if(!pages.contains(memoryAccesses.get(size))){
                pages.add(memoryAccesses.get(size));
            }
            size--;
        }
        
        return pages.getLast();
    }
    
}
