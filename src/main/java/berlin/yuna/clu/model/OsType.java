package berlin.yuna.clu.model;

public enum OsType {
    OS_LINUX(true, "linux"),
    OS_DARWIN(true, "mac"),
    OS_WINDOWS(false, "windows"),
    OS_AIX(true, "aix"),
    OS_IRIX(true, "irix"),
    OS_HP_UX(true, "hp-ux"),
    OS_400(false, "os/400"),
    OS_FREE_BSD(true, "freebsd"),
    OS_OPEN_BSD(true, "openbsd"),
    OS_NET_BSD(true, "netbsd"),
    OS_2(false, "os/2"),
    OS_SOLARIS(true, "solaris"),
    OS_SUN(true, "sunos"),
    OS_MIPS(false, "mips"),
    OS_ZOS(false, "z/os"),
    OS_UNKNOWN(false);

    private final boolean unix;
    private final String[] prefix;

    OsType(final boolean unix, final String... prefix) {
        this.unix = unix;
        this.prefix = prefix;
    }

    public String[] getPrefix() {
        return prefix;
    }

    public boolean isUnix() {
        return unix;
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

    @Override
    public String toString() {
        return osCase(name(), 3);
    }

    public static String osCase(final String upperCase, final int from) {
        final int up = upperCase.indexOf("_", from);
        return upperCase.charAt(from) + (up == -1
                ? upperCase.substring(from + 1).toLowerCase()
                : upperCase.substring(from + 1, up).toLowerCase() + upperCase.substring(up + 1)
        );
    }
}