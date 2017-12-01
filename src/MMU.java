import java.util.Random;

public class MMU {

    /**
     * MAX_VIRT_ADDRESS - Same as maximum physical address, last entry in virtual or physical memory, 4Gb
     * MAX_P1 - Number of entries in the outer page table, from 0 to 1023
     * MAX_P2 - Number of entries in the page of page table (POPT), happens to be the same as MAX_P1 due to 4Kb pages
     * MAX_OFFSET - Number of entries in one frame of physical memory, from 0 to 4095, 4Kb frames
     */
    private final static long MAX_VIRT_ADDRESS = 4294967295L;
    private final static int MAX_P1 = 1023;
    private final static int MAX_P2 = 1023;
    private final static int MAX_OFFSET = 4095;

    /**
     * Creates a valid virtual address from the range of 0 to MAX_VIRT_ADDRESS
     * @return a random virtual address
     * //TODO Possibly change minimum from 0 to account for kernel space?
     */
    public static long getRandomAddress(){
        Random random = new Random();
        int first = random.nextInt(Integer.MAX_VALUE);
        long second = (long) random.nextInt(Integer.MAX_VALUE) + 1;
        return first + second;
    }

    /**
     * Process a virtual address, breaking it up into P1, P2, and Offset.
     *  Converts the address into a string of its binary form, pads left with 0 if less than 32 bits,
     *      and calculates P1, P2, and Offset from the first 10 bits, second 10 bits, and last 12 bits respectively.
     * @param pid the pid of the process requesting the address, just gets sent with the translated virtual address
     * @param addr A virtual address
     * @return Integer array of size 4, named logical
     *      logical[0] == pid
     *      logical[1] == P1
     *      logical[2] == P2
     *      logical[3] == Offset
     * @throws IllegalArgumentException if addr is not a valid address
     */
    public static int[] processAddress(int pid, long addr) throws IllegalArgumentException{
        if (addr>MAX_VIRT_ADDRESS || addr<0){
            throw new IllegalArgumentException("MMU passed invalid virtual address: " + addr);
        }

        String binary = Long.toBinaryString(addr);
        if (binary.length()>32){
            throw new IllegalArgumentException("MMU passed invalid virtual address: " + addr);
        }

        while (binary.length() < 32){
            binary = "0" + binary;
        }

        String p1Bin = binary.substring(0, 10);
        String p2Bin = binary.substring(10, 20);
        String offsetBin = binary.substring(20, 32);

        int p1 = Integer.parseInt(p1Bin, 2);
        int p2 = Integer.parseInt(p2Bin, 2);
        int offset = Integer.parseInt(offsetBin, 2);

        //Verify all values are valid
        if (p1>MAX_P1 || p2>MAX_P2 || offset>MAX_OFFSET) {
            throw new IllegalArgumentException("MMU passed invalid virtual address: " + addr);
        }

        int[] logical = new int[4];
        logical[0] = pid;
        logical[1] = p1;
        logical[2] = p2;
        logical[3] = offset;

        return logical;
    }

}
