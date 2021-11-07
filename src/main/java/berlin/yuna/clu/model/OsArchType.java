package berlin.yuna.clu.model;

import static berlin.yuna.clu.model.OsType.osCase;

public enum OsArchType {
    AT_86("x86", "686", "386", "368"),
    AT_64("64"),
    AT_7("arm7"),
    AT_6("arm6"),
    AT_PPC("ppc"),
    AT_UNKNOWN;

    private final String[] contains;

    OsArchType(final String... contains) {
        this.contains = contains;
    }

    public String[] getContains() {
        return contains;
    }

    public static OsArchType of(final String osArch) {
        final String os = osArch == null ? "" : osArch.toLowerCase();
        for (OsArchType osType : OsArchType.values()) {
            for (String content : osType.getContains()) {
                if (os.contains(content)) {
                    return osType;
                }
            }
        }
        return AT_UNKNOWN;
    }

    @Override
    public String toString() {
        return osCase(name(), 3);
    }
}
