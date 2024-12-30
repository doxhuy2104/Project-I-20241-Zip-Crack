package multithreadbruteforce;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.win32.StdCallLibrary;

public class CPUAffinity {
    public interface Kernel32 extends StdCallLibrary {
        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);

        boolean SetProcessAffinityMask(long hProcess, long dwProcessAffinityMask);

        long GetCurrentProcess();
    }

    public static void setCPUAffinity(int... cpuIndices) {
        if (!Platform.isWindows()) {
            return;
        }

        long mask = 0;
        for (int cpu : cpuIndices) {
            mask |= (1L << cpu);
        }

        long process = Kernel32.INSTANCE.GetCurrentProcess();
        Kernel32.INSTANCE.SetProcessAffinityMask(process, mask);
    }
}