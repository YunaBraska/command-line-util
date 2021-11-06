package berlin.yuna.clu.model;

public enum OsType {
    OS_LINUX("linux"),
    OS_MAC("mac"),
    OS_WINDOWS("windows"),
    OS_AIX("aix"),
    OS_IRIX("irix"),
    OS_HP_UX("hp-ux"),
    OS_400("os/400"),
    OS_FREE_BSD("freebsd"),
    OS_OPEN_BSD("openbsd"),
    OS_NET_BSD("netbsd"),
    OS_2("os/2"),
    OS_SOLARIS("solaris"),
    OS_SUN("sunos"),
    OS_MIPS("mips"),
    OS_ZOS("z/os"),
    OS_UNKNOWN;

    private final String[] prefix;

    OsType(final String... prefix) {
        this.prefix = prefix;
    }

    public String[] getPrefix() {
        return prefix;
    }

    public boolean isUnix() {
        return isUnix(this);
    }

    public static boolean isUnix(final OsType os) {
        return os.isOneOf(
                OsType.OS_AIX,
                OsType.OS_HP_UX,
                OsType.OS_IRIX,
                OsType.OS_LINUX,
                OsType.OS_MAC,
                OsType.OS_SUN,
                OsType.OS_SOLARIS,
                OsType.OS_FREE_BSD,
                OsType.OS_OPEN_BSD,
                OsType.OS_NET_BSD
        );
    }

    public boolean isOneOf(final OsType... osTypes) {
        for (OsType type : osTypes) {
            if (this == type) {
                return true;
            }
        }
        return false;
    }

    public static OsType of(final String osName) {
        final String os = osName == null ? "" : osName.toLowerCase();
        for (OsType osType : OsType.values()) {
            for (String prefix : osType.getPrefix()) {
                if (os.startsWith(prefix)) {
                    return osType;
                }
            }
        }
        return OS_UNKNOWN;
    }
}