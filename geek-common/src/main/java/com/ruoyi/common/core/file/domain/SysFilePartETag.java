package com.ruoyi.common.core.file.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SysFilePartETag implements Serializable {
    private static final long serialVersionUID = 2471854027355307627L;
    private Integer partNumber;
    @JsonProperty("ETag")
    private String eTag;
    private Long partSize;
    private Long partCRC;
    private Long fileSize;
    private String filePath;
    private String taskId;

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

    public SysFilePartETag() {
    }

    public SysFilePartETag(Integer partNumber, String eTag) {
        this.partNumber = partNumber;
        this.eTag = eTag;
    }

    public SysFilePartETag(Integer partNumber, String eTag, long partSize, Long partCRC) {
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
