package com.lesofn.archforge.user.menu.repository;

import com.lesofn.archforge.common.repository.BaseEntity_;
import com.lesofn.archforge.common.repository.CriteriaQuerySupport;
import com.lesofn.archforge.user.domain.SysMenu;
import com.lesofn.archforge.user.domain.SysMenu_;
import com.lesofn.archforge.user.domain.SysRoleMenu;
import com.lesofn.archforge.user.domain.SysRoleMenu_;
import com.lesofn.archforge.user.domain.SysUser;
import com.lesofn.archforge.user.domain.SysUser_;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;

/** Criteria API implementation — no raw JPQL strings. */
public class SysMenuRepositoryImpl extends CriteriaQuerySupport implements SysMenuRepositoryCustom {

    @Override
    public List<SysMenu> findAllActiveMenus() {
        CriteriaBuilder cb = cb();
        CriteriaQuery<SysMenu> cq = cb.createQuery(SysMenu.class);
        Root<SysMenu> menu = cq.from(SysMenu.class);

        cq.select(menu)
                .where(
                        cb.equal(menu.get(BaseEntity_.deleted), false),
                        cb.equal(menu.get(SysMenu_.status), 1))
                .orderBy(cb.asc(menu.get(SysMenu_.parentId)), cb.asc(menu.get(SysMenu_.menuId)));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<SysMenu> findMenusByRoleId(Long roleId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<SysMenu> cq = cb.createQuery(SysMenu.class);

        Root<SysMenu> menu = cq.from(SysMenu.class);
        Root<SysRoleMenu> rm = cq.from(SysRoleMenu.class);

        cq.select(menu)
                .distinct(true)
                .where(
                        cb.equal(menu.get(SysMenu_.menuId), rm.get(SysRoleMenu_.menuId)),
                        cb.equal(rm.get(SysRoleMenu_.roleId), roleId),
                        cb.equal(menu.get(SysMenu_.status), 1),
                        cb.equal(menu.get(BaseEntity_.deleted), false))
                .orderBy(cb.asc(menu.get(SysMenu_.parentId)), cb.asc(menu.get(SysMenu_.menuId)));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<SysMenu> selectMenuListByUserId(Long userId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<SysMenu> cq = cb.createQuery(SysMenu.class);

        Root<SysMenu> menu = cq.from(SysMenu.class);
        Root<SysRoleMenu> rm = cq.from(SysRoleMenu.class);
        Root<SysUser> user = cq.from(SysUser.class);

        cq.select(menu)
                .distinct(true)
                .where(
                        cb.equal(menu.get(SysMenu_.menuId), rm.get(SysRoleMenu_.menuId)),
                        cb.equal(rm.get(SysRoleMenu_.roleId), user.get(SysUser_.roleId)),
                        cb.equal(user.get(SysUser_.userId), userId),
                        cb.equal(menu.get(SysMenu_.status), 1),
                        cb.equal(menu.get(BaseEntity_.deleted), false))
                .orderBy(cb.asc(menu.get(SysMenu_.parentId)));

        return entityManager.createQuery(cq).getResultList();
    }
}
