package com.github.walkvoid.zone.user.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.wvframework.models.PageResponse;
import com.github.walkvoid.wvframework.utils.BeanCopyUtils;
import com.github.walkvoid.zone.user.business.db.mapper.UserInfoMapper;
import com.github.walkvoid.zone.user.model.dto.UserInfoDTO;
import com.github.walkvoid.zone.user.model.entity.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 鐢ㄦ埛淇℃伅DAO绫?
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 * @desc 鐢ㄦ埛淇℃伅鏁版嵁璁块棶灞傜被锛屾彁渚涚敤鎴风浉鍏崇殑鏁版嵁搴撴搷浣?
 */
@Repository
public class UserInfoDAO {

    @Autowired
    private UserInfoMapper userInfoMapper;

    /**
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ鐢ㄦ埛淇℃伅
     * @param id 鐢ㄦ埛ID
     * @return 鐢ㄦ埛淇℃伅
     */
    public UserInfo selectById(Long id) {
        return userInfoMapper.selectById(id);
    }

    /**
     * 鏍规嵁鐢ㄦ埛鍚嶆煡璇㈢敤鎴蜂俊鎭?
     * @param username 鐢ㄦ埛鍚?
     * @return 鐢ㄦ埛淇℃伅
     */
    public UserInfo selectByUsername(String username) {
        // 浣跨敤鏉′欢鏋勯€犲櫒鏌ヨ
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserInfo> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userInfoMapper.selectOne(queryWrapper);
    }

    /**
     * 鏍规嵁鎵嬫満鍙锋煡璇㈢敤鎴蜂俊鎭?
     * @param phone 鎵嬫満鍙?
     * @return 鐢ㄦ埛淇℃伅
     */
    public UserInfo selectByPhone(String phone) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserInfo> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        return userInfoMapper.selectOne(queryWrapper);
    }

    /**
     * 鏍规嵁閭鏌ヨ鐢ㄦ埛淇℃伅
     * @param email 閭
     * @return 鐢ㄦ埛淇℃伅
     */
    public UserInfo selectByEmail(String email) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserInfo> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("email", email);
        return userInfoMapper.selectOne(queryWrapper);
    }

    /**
     * 鎻掑叆鐢ㄦ埛淇℃伅
     * @param userInfo 鐢ㄦ埛淇℃伅
     * @return 褰卞搷琛屾暟
     */
    public int insert(UserInfo userInfo) {
        return userInfoMapper.insert(userInfo);
    }

    /**
     * 鏇存柊鐢ㄦ埛淇℃伅
     * @param userInfo 鐢ㄦ埛淇℃伅
     * @return 褰卞搷琛屾暟
     */
    public int updateById(UserInfo userInfo) {
        return userInfoMapper.updateById(userInfo);
    }

    /**
     * 鍒犻櫎鐢ㄦ埛
     * @param id 鐢ㄦ埛ID
     * @return 褰卞搷琛屾暟
     */
    public int deleteById(Long id) {
        return userInfoMapper.deleteById(id);
    }

    /**
     * 鎵归噺鍒犻櫎鐢ㄦ埛
     * @param ids 鐢ㄦ埛ID鍒楄〃
     * @return 褰卞搷琛屾暟
     */
    public int deleteBatchIds(List<Long> ids) {
        return userInfoMapper.deleteBatchIds(ids);
    }

    /**
     * 鏇存柊鐢ㄦ埛鏈€鍚庣櫥褰曚俊鎭?
     * @param id 鐢ㄦ埛ID
     * @param lastLoginTime 鏈€鍚庣櫥褰曟椂闂?
     * @param lastLoginIp 鏈€鍚庣櫥褰旾P
     * @return 褰卞搷琛屾暟
     */
    public int updateLastLoginInfo(Long id, LocalDateTime lastLoginTime, String lastLoginIp) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(id);
        userInfo.setLastLoginTime(lastLoginTime);
        userInfo.setLastLoginIp(lastLoginIp);
        return userInfoMapper.updateById(userInfo);
    }

    /**
     * 鏌ヨ鎵€鏈夌敤鎴?
     * @return 鐢ㄦ埛鍒楄〃
     */
    public List<UserInfo> selectAll() {
        return userInfoMapper.selectList(null);
    }

    /**
     * 鏍规嵁鏉′欢鏌ヨ鐢ㄦ埛鍒楄〃
     * @param userInfo 鏌ヨ鏉′欢
     * @return 鐢ㄦ埛鍒楄〃
     */
    public List<UserInfo> selectList(UserInfo userInfo) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserInfo> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>(userInfo);
        return userInfoMapper.selectList(queryWrapper);
    }

    /**
     * 鍒嗛〉鏌ヨ鐢ㄦ埛鍒楄〃
     * @param page 椤电爜锛堜粠1寮€濮嬶級
     * @param size 姣忛〉鏁伴噺
     * @param userInfo 鏌ヨ鏉′欢
     * @return 鍒嗛〉缁撴灉
     */
    public PageDTO<UserInfo> selectPage(PageDTO<UserInfo> pageDTO, UserInfo userInfo) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>(userInfo);
        return userInfoMapper.selectPage(pageDTO, queryWrapper);
    }

    public PageResponse<UserInfoDTO> page(PageRequest<UserInfoDTO> pageRequest) {
        UserInfo condition = BeanCopyUtils.copyBean(pageRequest.getParam(), UserInfo.class);
        Page<UserInfo> page = userInfoMapper.selectPage(
                new Page<>(pageRequest.getCurrent(), pageRequest.getSize()),
                new QueryWrapper<>(condition));
        List<UserInfoDTO> records = BeanCopyUtils.copyList(page.getRecords(), UserInfoDTO.class);
        return new PageResponse<>(page.getTotal(), (int) page.getSize(), page.getCurrent(), records);
    }

    /**
     * 缁熻鐢ㄦ埛鏁伴噺
     * @return 鐢ㄦ埛鏁伴噺
     */
    public long count() {
        return userInfoMapper.selectCount(null);
    }

    /**
     * 鏍规嵁鏉′欢缁熻鐢ㄦ埛鏁伴噺
     * @param userInfo 鏌ヨ鏉′欢
     * @return 鐢ㄦ埛鏁伴噺
     */
    public long countByCondition(UserInfo userInfo) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserInfo> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>(userInfo);
        return userInfoMapper.selectCount(queryWrapper);
    }

    /**
     * 妫€鏌ユ墜鏈哄彿鏄惁宸插瓨鍦?
     * @param phone 鎵嬫満鍙?
     * @return 瀛樺湪鏁伴噺
     */
    public int checkPhoneExists(String phone) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserInfo> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        return Math.toIntExact(userInfoMapper.selectCount(queryWrapper));
    }

    /**
     * 妫€鏌ラ偖绠辨槸鍚﹀凡瀛樺湪
     * @param email 閭
     * @return 瀛樺湪鏁伴噺
     */
    public int checkEmailExists(String email) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserInfo> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("email", email);
        return Math.toIntExact(userInfoMapper.selectCount(queryWrapper));
    }

    /**
     * 妫€鏌ョ敤鎴峰悕鏄惁宸插瓨鍦?
     * @param username 鐢ㄦ埛鍚?
     * @return 瀛樺湪鏁伴噺
     */
    public int checkUsernameExists(String username) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserInfo> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("username", username);
        return Math.toIntExact(userInfoMapper.selectCount(queryWrapper));
    }

    /**
     * 鏍规嵁瑙掕壊ID鏌ヨ鐢ㄦ埛鍒楄〃
     * @param roleId 瑙掕壊ID
     * @return 鐢ㄦ埛鍒楄〃
     */
    public List<UserInfo> selectUsersByRoleId(Long roleId) {
        // 姝ゆ柟娉曞彲鑳介渶瑕佸湪UserInfoMapper涓嚜瀹氫箟SQL瀹炵幇
        // 杩欓噷鍏堢敤绌哄垪琛ㄨ繑鍥烇紝鍚庣画鍙互鎵╁睍
        return null;
    }

    /**
     * 鏌ヨ鍚敤鐨勭敤鎴?
     * @return 鐢ㄦ埛鍒楄〃
     */
    public List<UserInfo> selectEnabledUsers() {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserInfo> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("status", 1); // 鍋囪1琛ㄧず鍚敤鐘舵€?
        return userInfoMapper.selectList(queryWrapper);
    }

    /**
     * 鎵归噺鏇存柊鐢ㄦ埛鐘舵€?
     * @param ids 鐢ㄦ埛ID鍒楄〃
     * @param status 鐘舵€?
     * @return 褰卞搷琛屾暟
     */
    public int updateBatchStatus(List<Long> ids, Integer status) {
        com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<UserInfo> updateWrapper = new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
        updateWrapper.in("id", ids).set("status", status);
        UserInfo userInfo = new UserInfo();
        return userInfoMapper.update(userInfo, updateWrapper);
    }
}
