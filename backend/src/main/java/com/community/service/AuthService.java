package com.community.service;

import com.community.dto.LoginDTO;
import com.community.dto.WechatBindPhoneDTO;
import com.community.dto.WechatLoginDTO;
import com.community.vo.LoginVO;
import com.community.vo.WechatLoginVO;

public interface AuthService {
    LoginVO login(LoginDTO loginDTO);

    WechatLoginVO wechatLogin(WechatLoginDTO wechatLoginDTO);

    WechatLoginVO wechatBindPhone(WechatBindPhoneDTO bindPhoneDTO);
}
