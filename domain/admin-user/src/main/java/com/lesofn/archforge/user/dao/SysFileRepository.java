package com.lesofn.archforge.user.dao;

import com.lesofn.archforge.user.domain.SysFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @author sofn
 */
public interface SysFileRepository extends JpaRepository<SysFile, Long>, JpaSpecificationExecutor<SysFile> {
}
