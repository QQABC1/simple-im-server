package com.shixun.simpleimserver.service;

import com.shixun.simpleimserver.model.dto.FriendAddDTO;
import com.shixun.simpleimserver.model.dto.FriendApproveDTO;
import com.shixun.simpleimserver.model.vo.ContactVO;
import com.shixun.simpleimserver.model.vo.UserVO;

import java.util.List;

public interface FriendService {
    /**
     * 搜索用户
     * @param username
     * @return
     */
    UserVO searchUser(String username);

    /**
     * 发起好友申请
     * @param dto
     */
    void sendRequest(FriendAddDTO dto);


    /**
     * 获取我的好友列表
     * @return
     */
    List<ContactVO> getFriendList();


    /**
     * 获取待处理申请
     * @return
     */
    List<ContactVO> getPendingRequests();


    /**
     * 处理申请
     * @param dto
     */
    void approveRequest(FriendApproveDTO dto);
}