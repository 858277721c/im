package com.fanwe.lib.im;

/**
 * Created by zhengjun on 2017/11/22.
 */
public interface FIMMsg
{
    int getDataType();

    FIMMsgData getData();

    boolean isSelf();

    String getConversationPeer();

    long getTimestamp();

    FIMMsgState getState();

    FIMConversationType getConversationType();
}
