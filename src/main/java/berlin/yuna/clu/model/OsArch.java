package berlin.yuna.clu.model;

import static berlin.yuna.clu.model.OsType.osCase;

public enum OsArch {
    ARCH_AMD("amd"),
    ARCH_ARM("arm", "aarch"),
    ARCH_PPC("ppc"),
    ARCH_INTEL("x86", "686", "386", "368", "64"),
    ARCH_UNKNOWN;

    private final String[] contains;

    OsArch(final String... contains) {
        this.contains = contains;
    }

    public String[] getContains() {
        return contains;
    }

    public static OsArch of(final String osArch) {
        final String os = osArch == null ? "" : osArch.toLowerCase();
        for (OsArch osType : OsArch.values()) {
            for (String content : osType.getContains()) {
                if (os.contains(content)) {
                    return osType;
                }
            }
        }
        return ARCH_UNKNOWN;
    }

    @Override
    public String toString() {
        return osCase(name(), 5);
    }
}
