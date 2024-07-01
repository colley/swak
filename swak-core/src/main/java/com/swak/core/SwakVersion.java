package com.swak.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class SwakVersion implements Comparable<SwakVersion> {
    public static final SwakVersion CURRENT = new SwakVersion(2, 4, 0);
    private static final Pattern PREFIX_DIGITS_PATTERN = Pattern.compile("([0-9]).([0-9]).([0-9])");
    private static final String VERSION = getVersion(SwakVersion.class, "").toString();

    private final int major;
    private final int minor;
    private final int micro;

    public SwakVersion(int major, int minor, int micro) {
        this.major = major;
        this.minor = minor;
        this.micro = micro;
    }

    //1.0.00

    public SwakVersion(String major, String minor, String micro) {
        this(Integer.parseInt(major), Integer.parseInt(minor), Integer.parseInt(micro));
    }

    private static SwakVersion getVersion(String version) {
        if (StringUtils.isNotEmpty(version)) {
            Matcher matcher = PREFIX_DIGITS_PATTERN.matcher(version);
            if (matcher.find()) {
                String[] vArr = matcher.group(0).split("\\.");
                return new SwakVersion(vArr[0], vArr[1], vArr[2]);
            }
        }
        return CURRENT;
    }

    public static void main(String[] args) {
        System.out.println(SwakVersion.getVersion());
    }

    public static String toVersion(Integer intVersion) {
        int major = intVersion / 10000;
        intVersion = intVersion - major * 10000;
        int minor = (intVersion / 100) % 1000;
        int micro = intVersion % 100;
        return new SwakVersion(major, minor, micro).toString();
    }

    public static int versionStrToInt(String version) {
        int v = 0;
        String[] vArr = version.split("\\.");
        int len = vArr.length;
        for (int i = 0; i < len; i++) {
            String subV = vArr[i];
            if (StringUtils.isNotEmpty(subV)) {
                v += Integer.parseInt(subV) * Math.pow(10, (len - i - 1) * 2);
            }
        }
        return v;
    }

    public static String getVersion() {
        return VERSION;
    }

    public static SwakVersion getVersion(Class<?> cls, String defaultVersion) {
        try {
            // find version info from MANIFEST.MF first
            Package pkg = cls.getPackage();
            String version = null;
            if (pkg != null) {
                version = pkg.getImplementationVersion();
                if (StringUtils.isNotEmpty(version)) {
                    return getVersion(version);
                }

                version = pkg.getSpecificationVersion();
                if (StringUtils.isNotEmpty(version)) {
                    return getVersion(version);
                }
            }
        } catch (Throwable e) {
            log.error("return default version, ignore exception " + e.getMessage(), e);
        }
        return getVersion(defaultVersion);
    }

    /**
     * Returns whether the current {@link SwakVersion} is greater (newer) than the given
     * one.
     *
     * @param version - candidate version
     * @return true or false based on version comparison
     */
    public boolean isGreaterThan(SwakVersion version) {
        return compareTo(version) > 0;
    }

    /**
     * Returns whether the current {@link SwakVersion} is greater (newer) or the same as
     * the given one.
     *
     * @param version - candidate version
     * @return true or false based on version comparison
     */
    public boolean isGreaterThanOrEqualTo(SwakVersion version) {
        return compareTo(version) >= 0;
    }

    /**
     * Returns whether the current {@link SwakVersion} is the same as the given one.
     *
     * @param version - candidate version
     * @return true or false based on version comparison
     */
    public boolean is(SwakVersion version) {
        return equals(version);
    }

    /**
     * Returns whether the current {@link SwakVersion} is less (older) than the given
     * one.
     *
     * @param version - candidate version
     * @return true or false based on version comparison
     */
    public boolean isLessThan(SwakVersion version) {
        return compareTo(version) < 0;
    }

    /**
     * Returns whether the current {@link SwakVersion} is less (older) or equal to the
     * current one.
     *
     * @param version - candidate version
     * @return true or false based on version comparison
     */
    public boolean isLessThanOrEqualTo(SwakVersion version) {
        return compareTo(version) <= 0;
    }

    @Override
    public int compareTo(SwakVersion that) {
        if (that == null) {
            return 1;
        }

        if (major != that.major) {
            return major - that.major;
        }

        if (minor != that.minor) {
            return minor - that.minor;
        }

        if (micro != that.micro) {
            return micro - that.micro;
        }

        return 0;
    }

    @Override
    public String toString() {
        return "" + major + "." + minor + "." + micro;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Integer.hashCode(major);
        result = prime * result + Integer.hashCode(minor);
        result = prime * result + Integer.hashCode(micro);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SwakVersion)) {
            return false;
        }
        SwakVersion that = (SwakVersion) obj;
        return this.major == that.major && this.minor == that.minor && this.micro == that.micro;
    }


}
