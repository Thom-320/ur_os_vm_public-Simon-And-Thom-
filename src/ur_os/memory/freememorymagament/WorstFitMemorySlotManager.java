/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ur_os.memory.freememorymagament;

/**
 *
 * @author super
 */
public class WorstFitMemorySlotManager extends FreeMemorySlotManager{
    
    public WorstFitMemorySlotManager(int memSize){
        super(memSize);
    }
    
    @Override
    public MemorySlot getSlot(int size) {
        MemorySlot candidate = null;
        int widest = -1; // Track slot with the largest free space
        for (MemorySlot slot : list) {
            if(slot.canContain(size) && slot.getSize() > widest){
                candidate = slot;
                widest = slot.getSize();
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
