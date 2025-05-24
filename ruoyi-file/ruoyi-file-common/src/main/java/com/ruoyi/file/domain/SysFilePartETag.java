package com.ruoyi.file.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class SysFilePartETag implements Serializable {
    private static final long serialVersionUID = 2471854027355307627L;
    private int partNumber;
    private String eTag;
    private long partSize;
    private Long partCRC;

    public SysFilePartETag(int partNumber, String eTag) {
        this.partNumber = partNumber;
        this.eTag = eTag;
    }

    public SysFilePartETag(int partNumber, String eTag, long partSize, Long partCRC) {
        this.partNumber = partNumber;
        this.eTag = eTag;
        this.partSize = partSize;
        this.partCRC = partCRC;
    }

    public int hashCode() {
        int result = 1;
        result = 31 * result + (this.eTag == null ? 0 : this.eTag.hashCode());
        result = 31 * result + this.partNumber;
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof SysFilePartETag)) {
            return false;
        } else {
            SysFilePartETag other = (SysFilePartETag) obj;
            if (this.partNumber != other.partNumber) {
                return false;
            } else {
                return this.eTag == null ? other.eTag == null : this.eTag.equals(other.eTag);
            }
        }
    }
}
