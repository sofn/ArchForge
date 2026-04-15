package com.lesofn.archsmith.user.domain;

import com.lesofn.archsmith.common.repository.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * 文件信息表
 *
 * @author sofn
 */
@Setter
@Getter
@Accessors(chain = true)
@Entity
@Table(name = "sys_file")
@DynamicInsert
@DynamicUpdate
public class SysFile extends BaseEntity<SysFile> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fileId;

    /** 原始文件名 */
    private String originalName;

    /** 存储文件名 */
    private String storageName;

    /** 存储路径 */
    private String storagePath;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 文件类型 (MIME type) */
    private String contentType;

    /** 文件后缀 */
    private String extension;

    /** 存储类型: local / s3 */
    private String storageType;

    /** 备注 */
    private String remark;
}
