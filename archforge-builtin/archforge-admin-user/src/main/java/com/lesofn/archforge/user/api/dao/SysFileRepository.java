package com.lesofn.archforge.user.api.dao;

import com.lesofn.archforge.user.api.domain.SysFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 文件表 Spring Data 仓库。
 *
 * @author sofn
 */
public interface SysFileRepository extends JpaRepository<SysFile, Long>, JpaSpecificationExecutor<SysFile> {
}
