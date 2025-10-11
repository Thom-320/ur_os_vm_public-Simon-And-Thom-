/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ur_os.memory.freememorymagament;

/**
 *
 * @author super
 */
public class BestFitMemorySlotManager extends FreeMemorySlotManager{
    
    public BestFitMemorySlotManager(int memSize){
        super(memSize);
    }
    
    @Override
    public MemorySlot getSlot(int size) {
        MemorySlot candidate = null;
        int bestWaste = Integer.MAX_VALUE; // Track minimal internal fragmentation
        for (MemorySlot slot : list) {
            if(slot.canContain(size)){
                int waste = slot.getRemainder(size);
                if(waste < bestWaste){
                    candidate = slot;
                    bestWaste = waste;
                }
            }
        }
        if(candidate == null){
            System.out.println("Error - Memory cannot allocate a slot big enough for the requested memory");
            return null;
        }
        if(candidate.getSize() == size){
            list.remove(candidate);
            return candidate;
        }
        return candidate.assignMemory(size);
    }
    
}
