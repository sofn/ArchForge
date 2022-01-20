package com.appengine.common.context;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.appengine.common.utils.URLUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.Serializable;

/**
 * 客户端版本号解析类
 *
 * @author sofn
 */
public class ClientVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String VERSION_HEADER = "X-Version";

    public static final String SPLIT = "-";


    public static class Version implements Comparable<Version>, Serializable {

        private static final long serialVersionUID = 1L;

        public static final Version NULL = new Version(0, 0, 0, 0);

        public final int major;
        public final int minor;
        public final int revision;
        public final int build;

        public static final String SPLIT_REGX = "\\.";
        public static final String SPLIT = ".";

        public Version(int major, int minor, int revision, int build) {
            super();
            this.major = major;
            this.minor = minor;
            this.revision = revision;
            this.build = build;
        }

        @JsonCreator
        public static Version valueOf(String version) {
            if (StringUtils.isBlank(version)) {
                return NULL;
            }
            String[] parts = version.split(SPLIT_REGX);
            int major = NumberUtils.toInt(parts[0], 0);
            int minor = parts.length >= 2 ? NumberUtils.toInt(parts[1], 0) : 0;
            int reversion = parts.length >= 3 ? NumberUtils.toInt(parts[2], 0) : 0;
            int build = parts.length >= 4 ? NumberUtils.toInt(parts[3], 0) : 0;
            return new Version(major, minor, reversion, build);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + major;
            result = prime * result + minor;
            result = prime * result + revision;
            result = prime * result + build;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Version other = (Version) obj;
            if (major != other.major)
                return false;
            if (minor != other.minor)
                return false;
            if (revision != other.revision)
                return false;
            if (build != other.build)
                return false;
            return true;
        }

        public String toString() {
            return this.major + SPLIT + this.minor + SPLIT + this.revision + SPLIT + build;
        }

        @Override
        public int compareTo(Version o) {
            if (o == null) {
                return 1;
            }
            if (this.equals(o)) {
                return 0;
            }
            if (this.major > o.major
                    || (this.major == o.major && this.minor > o.minor)
                    || (this.major == o.major && this.minor == o.minor && this.revision > o.revision)
                    || (this.major == o.major && this.minor == o.minor && this.revision == o.revision && this.build > o.build)) {
                return 1;
            }
            return -1;
        }
    }

    public static final ClientVersion NULL = new ClientVersion(Version.NULL, Version.NULL);
    public static final String DEFAULT_UNKNOW = "unknow";

    public final Version sdkVersion;
    public final Version clientVersion;
    public final String udid;
    public final String device;
    public final String channel;

    public ClientVersion(Version sdkVersion, Version clientVersion) {
        this.sdkVersion = sdkVersion;
        this.clientVersion = clientVersion;
        udid = DEFAULT_UNKNOW;
        device = DEFAULT_UNKNOW;
        channel = DEFAULT_UNKNOW;
    }

    public ClientVersion(Version sdkVersion, Version clientVersion, String udid, String device, String channel) {
        this.sdkVersion = sdkVersion;
        this.clientVersion = clientVersion;
        this.udid = udid;
        this.device = device;
        this.channel = channel;
    }


    @JsonCreator
    public static ClientVersion valueOf(String versionHeader) {
        if (StringUtils.isBlank(versionHeader)) {
            return NULL;
        } else {
            Version sdkVersion;
            Version clientVersion;
            String[] parts = versionHeader.split(SPLIT);
            sdkVersion = Version.valueOf(parts[0]);
            clientVersion = parts.length > 1 ? Version.valueOf(parts[1]) : Version.NULL;
            String uuid = parts.length > 2 ? parts[2] : DEFAULT_UNKNOW;
            String device = parts.length > 3 ? parts[3] : DEFAULT_UNKNOW;
            String channel = null;
            if (parts.length > 4) {
                // 由于客户端device中可能出现 - 号。 所以 channel取最后一个字段
                channel = parts[parts.length - 1];
            } else {
                channel = DEFAULT_UNKNOW;
            }
            // 由于device的字符不确定，所以encode一下
            device = URLUtils.encode(device);
            return new ClientVersion(sdkVersion, clientVersion, uuid, device, channel);
        }
    }

    public String toString() {
        return sdkVersion + SPLIT + clientVersion + SPLIT + udid + SPLIT + device + SPLIT + channel;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((clientVersion == null) ? 0 : clientVersion.hashCode());
        result = prime * result + ((sdkVersion == null) ? 0 : sdkVersion.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ClientVersion other = (ClientVersion) obj;
        if (clientVersion == null) {
            if (other.clientVersion != null)
                return false;
        } else if (!clientVersion.equals(other.clientVersion))
            return false;
        if (sdkVersion == null) {
            if (other.sdkVersion != null)
                return false;
        } else if (!sdkVersion.equals(other.sdkVersion))
            return false;
        return true;
    }

}
