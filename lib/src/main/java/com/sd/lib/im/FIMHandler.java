package com.sd.lib.im;

import com.sd.lib.im.callback.FIMMsgSendCallback;
import com.sd.lib.im.callback.FIMResultCallback;
import com.sd.lib.im.common.SendMsgParam;
import com.sd.lib.im.msg.FIMMsg;
import com.sd.lib.im.msg.FIMMsgData;

import java.util.Collection;

/**
 * IM处理类
 *
 * @param <M> 第三方IM消息类型
 */
public abstract class FIMHandler<M>
{
    /**
     * 返回新创建的第三方IM消息接收对象
     *
     * @return
     */
    public abstract FIMMsgParser<M> newMsgParser();

    /**
     * 移除并返回结果回调
     *
     * @param callbackId 回调id
     * @return
     */
    protected final FIMResultCallback removeCallbackById(String callbackId)
    {
        return FIMManager.getInstance().removeCallbackById(callbackId);
    }

    /**
     * 返回所有消息发送回调
     *
     * @return
     */
    protected final Collection<FIMMsgSendCallback> getMsgSendCallback()
    {
        return FIMManager.getInstance().getMsgSendCallback();
    }

    /**
     * 发送消息
     *
     * @param param      参数配置
     * @param msgData    消息数据
     * @param callbackId 回调id
     * @return
     */
    public abstract FIMMsg sendMsg(SendMsgParam param, FIMMsgData<M> msgData, String callbackId);

    /**
     * 加入群组
     *
     * @param groupId    群组id
     * @param callbackId 回调id
     */
    public abstract void joinGroup(String groupId, String callbackId);

    /**
     * 退出群组
     *
     * @param groupId    群组id
     * @param callbackId 回调id
     */
    public abstract void quitGroup(String groupId, String callbackId);
}
