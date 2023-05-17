package com.oraycn.ovcs.models;

/**
 * 群组扩展信息
 */
public class GroupExtension {

    /// <summary>
    /// 主持人ID
    /// </summary>
    public String ModeratorID ;

    /// <summary>
    /// 正在共享远程桌面的用户ID
    /// </summary>
    public String DesktopSharedUserID ;

    /// <summary>
    /// 主持人是否开启白板
    /// </summary>
    public boolean IsModeratorWhiteBoardNow ;
}
