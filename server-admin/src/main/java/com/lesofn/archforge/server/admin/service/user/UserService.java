package com.lesofn.archforge.server.admin.service.user;

import com.lesofn.archforge.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archforge.server.admin.dto.AdminPageResult;
import com.lesofn.archforge.server.admin.dto.AdminUserItemDTO;
import com.lesofn.archforge.server.admin.dto.AdminUserListRequest;
import com.lesofn.archforge.server.admin.dto.CurrentLoginUserDTO;
import com.lesofn.archforge.server.admin.dto.request.UserCreateRequest;
import com.lesofn.archforge.server.admin.dto.request.UserDeleteRequest;
import com.lesofn.archforge.server.admin.dto.request.UserPasswordRequest;
import com.lesofn.archforge.server.admin.dto.request.UserRoleRequest;
import com.lesofn.archforge.server.admin.dto.request.UserStatusRequest;
import com.lesofn.archforge.server.admin.dto.request.UserUpdateRequest;
import java.util.List;

/**
 * 管理端用户应用服务接口
 *
 * @author lesofn
 */
public interface UserService {

    /**
     * 获取登录用户信息
     *
     * @param loginUser 系统登录用户
     * @return 当前登录用户信息
     */
    CurrentLoginUserDTO getLoginUserInfo(SystemLoginUser loginUser);

    /**
     * 获取用户列表
     *
     * @param request 列表查询请求
     * @return 分页结果
     */
    AdminPageResult<AdminUserItemDTO> getUserList(AdminUserListRequest request);

    /**
     * 创建用户
     *
     * @param request 创建用户请求
     * @return 新用户ID
     */
    Long createUser(UserCreateRequest request);

    /**
     * 更新用户
     *
     * @param request 更新用户请求
     * @return 是否成功
     */
    Boolean updateUser(UserUpdateRequest request);

    /**
     * 删除用户
     *
     * @param request 删除用户请求
     * @return 是否成功
     */
    Boolean deleteUser(UserDeleteRequest request);

    /**
     * 获取用户角色ID列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> getRoleIds(Long userId);

    /**
     * 更新用户状态
     *
     * @param request 状态更新请求
     * @return 是否成功
     */
    Boolean updateStatus(UserStatusRequest request);

    /**
     * 重置用户密码
     *
     * @param request 重置密码请求
     * @return 是否成功
     */
    Boolean resetPassword(UserPasswordRequest request);

    /**
     * 分配用户角色
     *
     * @param request 角色分配请求
     * @return 是否成功
     */
    Boolean assignRole(UserRoleRequest request);
}
