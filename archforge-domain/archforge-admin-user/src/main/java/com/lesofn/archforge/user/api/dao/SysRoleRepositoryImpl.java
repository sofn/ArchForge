package com.lesofn.archforge.user.api.dao;

import com.lesofn.archforge.common.repository.BaseEntity_;
import com.lesofn.archforge.common.repository.CriteriaQuerySupport;
import com.lesofn.archforge.user.api.domain.SysMenu;
import com.lesofn.archforge.user.api.domain.SysMenu_;
import com.lesofn.archforge.user.api.domain.SysRole;
import com.lesofn.archforge.user.api.domain.SysRoleMenu;
import com.lesofn.archforge.user.api.domain.SysRoleMenu_;
import com.lesofn.archforge.user.api.domain.SysRole_;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;

/** Criteria API implementation — no raw JPQL strings. */
public class SysRoleRepositoryImpl extends CriteriaQuerySupport implements SysRoleRepositoryCustom {

    @Override
    public List<SysRole> findAllActiveRoles() {
        return findByActive(
                SysRole.class, BaseEntity_.deleted, SysRole_.status, (short) 1, SysRole_.roleSort);
    }

    @Override
    public List<SysMenu> getMenuListByRoleId(Long roleId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<SysMenu> cq = cb.createQuery(SysMenu.class);

        Root<SysMenu> menu = cq.from(SysMenu.class);
        Root<SysRoleMenu> rm = cq.from(SysRoleMenu.class);
        Root<SysRole> role = cq.from(SysRole.class);

        cq.select(menu)
                .where(
                        cb.equal(menu.get(SysMenu_.menuId), rm.get(SysRoleMenu_.menuId)),
                        cb.equal(role.get(SysRole_.roleId), rm.get(SysRoleMenu_.roleId)),
                        cb.equal(menu.get(BaseEntity_.deleted), false),
                        cb.equal(menu.get(SysMenu_.status), 1),
                        cb.equal(role.get(BaseEntity_.deleted), false),
                        cb.equal(role.get(SysRole_.status), (short) 1),
                        cb.equal(role.get(SysRole_.roleId), roleId));

        return entityManager.createQuery(cq).getResultList();
    }
}
