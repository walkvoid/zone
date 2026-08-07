package com.github.walkvoid.zone.user.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.wvframework.models.PageResponse;
import com.github.walkvoid.wvframework.utils.BeanCopyUtils;
import com.github.walkvoid.zone.user.model.entity.Role;
import com.github.walkvoid.zone.user.model.dto.RoleDTO;
import com.github.walkvoid.zone.user.business.db.mapper.RoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 瑙掕壊DAO绫?
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 * @desc 瑙掕壊鏁版嵁璁块棶灞傜被锛屾彁渚涜鑹茬浉鍏崇殑鏁版嵁搴撴搷浣?
 */
@Repository
public class RoleDAO {

    @Autowired
    private RoleMapper roleMapper;

    /**
     * 鏍规嵁ID鏌ヨ瑙掕壊
     * @param id 瑙掕壊ID
     * @return 瑙掕壊淇℃伅
     */
    public Role selectById(Long id) {
        return roleMapper.selectById(id);
    }

    /**
     * 鏍规嵁瑙掕壊浠ｇ爜鏌ヨ瑙掕壊
     * @param roleCode 瑙掕壊浠ｇ爜
     * @return 瑙掕壊淇℃伅
     */
    public Role selectByRoleCode(String roleCode) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Role> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("role_code", roleCode);
        return roleMapper.selectOne(queryWrapper);
    }

    /**
     * 鎻掑叆瑙掕壊
     * @param role 瑙掕壊淇℃伅
     * @return 褰卞搷琛屾暟
     */
    public int insert(Role role) {
        return roleMapper.insert(role);
    }

    /**
     * 鏇存柊瑙掕壊
     * @param role 瑙掕壊淇℃伅
     * @return 褰卞搷琛屾暟
     */
    public int updateById(Role role) {
        return roleMapper.updateById(role);
    }

    /**
     * 鍒犻櫎瑙掕壊
     * @param id 瑙掕壊ID
     * @return 褰卞搷琛屾暟
     */
    public int deleteById(Long id) {
        return roleMapper.deleteById(id);
    }

    /**
     * 鎵归噺鍒犻櫎瑙掕壊
     * @param ids 瑙掕壊ID鍒楄〃
     * @return 褰卞搷琛屾暟
     */
    public int deleteBatchIds(List<Long> ids) {
        return roleMapper.deleteBatchIds(ids);
    }

    /**
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ瑙掕壊鍒楄〃
     * @param userId 鐢ㄦ埛ID
     * @return 瑙掕壊鍒楄〃
     */
    public List<Role> selectRolesByUserId(Long userId) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Role> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.inSql("id", "select role_id from user_role where user_id = " + userId);
        return roleMapper.selectList(queryWrapper);
    }

    /**
     * 鏌ヨ鎵€鏈夊彲鐢ㄧ殑瑙掕壊
     * @return 瑙掕壊鍒楄〃
     */
    public List<Role> selectAvailableRoles() {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Role> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("enable", 1);
        return roleMapper.selectList(queryWrapper);
    }

    /**
     * 鏌ヨ闈炵郴缁熻鑹?
     * @return 瑙掕壊鍒楄〃
     */
    public List<Role> selectNonSystemRoles() {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Role> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("is_system", 0);
        return roleMapper.selectList(queryWrapper);
    }

    /**
     * 鏌ヨ鎵€鏈夎鑹?
     * @return 瑙掕壊鍒楄〃
     */
    public List<Role> selectAll() {
        return roleMapper.selectList(null);
    }

    /**
     * 鏍规嵁鏉′欢鏌ヨ瑙掕壊鍒楄〃
     * @param role 鏌ヨ鏉′欢
     * @return 瑙掕壊鍒楄〃
     */
    public List<Role> selectList(Role role) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Role> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>(role);
        return roleMapper.selectList(queryWrapper);
    }

    /**
     * 鍒嗛〉鏌ヨ瑙掕壊鍒楄〃
     */
    public PageDTO<Role> selectPage(PageDTO<Role> pageDTO, Role role) {
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>(role);
        return roleMapper.selectPage(pageDTO, queryWrapper);
    }

    public PageResponse<RoleDTO> page(PageRequest<RoleDTO> pageRequest) {
        Role condition = BeanCopyUtils.copyBean(pageRequest.getParam(), Role.class);
        Page<Role> page = roleMapper.selectPage(
                new Page<>(pageRequest.getCurrent(), pageRequest.getSize()),
                new QueryWrapper<>(condition));
        List<RoleDTO> records = BeanCopyUtils.copyList(page.getRecords(), RoleDTO.class);
        return new PageResponse<>(page.getTotal(), (int) page.getSize(), page.getCurrent(), records);
    }

    /**
     * 缁熻瑙掕壊鏁伴噺
     * @return 瑙掕壊鏁伴噺
     */
    public long count() {
        return roleMapper.selectCount(null);
    }

    /**
     * 鏍规嵁鏉′欢缁熻瑙掕壊鏁伴噺
     * @param role 鏌ヨ鏉′欢
     * @return 瑙掕壊鏁伴噺
     */
    public long countByCondition(Role role) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Role> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>(role);
        return roleMapper.selectCount(queryWrapper);
    }

    /**
     * 妫€鏌ヨ鑹蹭唬鐮佹槸鍚﹀瓨鍦?
     * @param roleCode 瑙掕壊浠ｇ爜
     * @return 瀛樺湪鏁伴噺
     */
    public long checkRoleCodeExists(String roleCode) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Role> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("role_code", roleCode);
        return roleMapper.selectCount(queryWrapper);
    }

    /**
     * 妫€鏌ヨ鑹插悕绉版槸鍚﹀瓨鍦?
     * @param roleName 瑙掕壊鍚嶇О
     * @return 瀛樺湪鏁伴噺
     */
    public long checkRoleNameExists(String roleName) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Role> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("role_name", roleName);
        return roleMapper.selectCount(queryWrapper);
    }
}