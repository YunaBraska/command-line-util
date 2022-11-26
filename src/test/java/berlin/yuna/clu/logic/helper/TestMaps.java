package berlin.yuna.clu.logic.helper;

import berlin.yuna.clu.model.OsArch;
import berlin.yuna.clu.model.OsArchType;
import berlin.yuna.clu.model.OsType;

import java.util.HashMap;

import static berlin.yuna.clu.model.OsType.OS_2;
import static berlin.yuna.clu.model.OsType.OS_400;
import static berlin.yuna.clu.model.OsType.OS_AIX;
import static berlin.yuna.clu.model.OsType.OS_FREE_BSD;
import static berlin.yuna.clu.model.OsType.OS_IRIX;
import static berlin.yuna.clu.model.OsType.OS_DARWIN;
import static berlin.yuna.clu.model.OsType.OS_MIPS;
import static berlin.yuna.clu.model.OsType.OS_OPEN_BSD;
import static berlin.yuna.clu.model.OsType.OS_SOLARIS;
import static berlin.yuna.clu.model.OsType.OS_SUN;
import static berlin.yuna.clu.model.OsType.OS_WINDOWS;
import static berlin.yuna.clu.model.OsType.OS_ZOS;

public class TestMaps {

    public static final HashMap<String, OsType> OS_TEST_MAP = generateOsTestMap();
    public static final HashMap<String, ExpectedArch> ARCH_TEST_MAP = generateArchTestMap();

    private static HashMap<String, OsType> generateOsTestMap() {
        final HashMap<String, OsType> result = new HashMap<>();
        result.put("Windows NT", OS_WINDOWS);
        result.put("Windows ME", OS_WINDOWS);
        result.put("Windows XP", OS_WINDOWS);
        result.put("Windows 2003", OS_WINDOWS);
        result.put("Mac OS X", OS_DARWIN);
        result.put("SunOS", OS_SUN);
        result.put("AiX", OS_AIX);
        result.put("Irix", OS_IRIX);
        result.put("os/400", OS_400);
        result.put("FreeBSD", OS_FREE_BSD);
        result.put("OpenBSD", OS_OPEN_BSD);
        result.put("OS/2", OS_2);
        result.put("Solaris", OS_SOLARIS);
        result.put("Mips", OS_MIPS);
        result.put("Z/OS", OS_ZOS);


        return result;
    }

    public record ExpectedArch(OsArch osArch, OsArchType osArchType) {
    }

    public static HashMap<String, ExpectedArch> generateArchTestMap() {
        final HashMap<String, ExpectedArch> result = new HashMap<>();
        result.put("x86", new ExpectedArch(OsArch.ARCH_INTEL, OsArchType.AT_86));
        result.put("i386", new ExpectedArch(OsArch.ARCH_INTEL, OsArchType.AT_86));
        result.put("686", new ExpectedArch(OsArch.ARCH_INTEL, OsArchType.AT_86));
        result.put("368", new ExpectedArch(OsArch.ARCH_INTEL, OsArchType.AT_86));
        result.put("64", new ExpectedArch(OsArch.ARCH_INTEL, OsArchType.AT_64));
        result.put("arm6", new ExpectedArch(OsArch.ARCH_ARM, OsArchType.AT_6));
        result.put("arm7", new ExpectedArch(OsArch.ARCH_ARM, OsArchType.AT_7));
        result.put("arm64", new ExpectedArch(OsArch.ARCH_ARM, OsArchType.AT_64));
        result.put("aarch64", new ExpectedArch(OsArch.ARCH_ARM, OsArchType.AT_64));
        result.put("amd6", new ExpectedArch(OsArch.ARCH_AMD, OsArchType.AT_UNKNOWN));
        result.put("amd7", new ExpectedArch(OsArch.ARCH_AMD, OsArchType.AT_UNKNOWN));
        result.put("amd64", new ExpectedArch(OsArch.ARCH_AMD, OsArchType.AT_64));
        result.put("ppc", new ExpectedArch(OsArch.ARCH_PPC, OsArchType.AT_PPC));
        result.put("arm1", new ExpectedArch(OsArch.ARCH_ARM, OsArchType.AT_UNKNOWN));
        result.put("amd1", new ExpectedArch(OsArch.ARCH_AMD, OsArchType.AT_UNKNOWN));
        result.put("xxx", new ExpectedArch(OsArch.ARCH_UNKNOWN, OsArchType.AT_UNKNOWN));
        return result;
    }

}
