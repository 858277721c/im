package com.sd.www.im.tim;

import com.sd.lib.im.FIMMsgParser;
import com.sd.lib.im.conversation.FIMConversation;
import com.sd.lib.im.conversation.FIMConversationType;
import com.sd.lib.im.msg.FIMMsgData;
import com.sd.lib.im.msg.FIMMsgState;
import com.tencent.TIMCustomElem;
import com.tencent.TIMElem;
import com.tencent.TIMElemType;
import com.tencent.TIMGroupSystemElem;
import com.tencent.TIMImageElem;
import com.tencent.TIMMessage;
import com.tencent.TIMSoundElem;
import com.tencent.TIMTextElem;

/**
 * Created by Administrator on 2017/11/23.
 */

public class AppIMMsgParser extends FIMMsgParser<TIMMessage>
{
    public static final String DEFAULT_CHARSET = "UTF-8";

    private TIMCustomElem timCustomElem;
    private TIMGroupSystemElem timGroupSystemElem;
    private TIMImageElem timImageElem;
    private TIMSoundElem timSoundElem;
    private TIMTextElem timTextElem;

    @Override
    public boolean isSelf()
    {
        return getSDKMsg().isSelf();
    }

    @Override
    public long getTimestamp()
    {
        return getSDKMsg().timestamp();
    }

    @Override
    public FIMMsgState getState()
    {
        switch (getSDKMsg().status())
        {
            case SendFail:
                return FIMMsgState.SendFail;
            case Sending:
                return FIMMsgState.Sending;
            case SendSucc:
                return FIMMsgState.SendSuccess;
            case HasDeleted:
                return FIMMsgState.HasDeleted;
            default:
                return FIMMsgState.Invalid;
        }
    }

    private FIMConversation mConversation;

    @Override
    public FIMConversation getConversation()
    {
        if (mConversation == null)
        {
            mConversation = new FIMConversation()
            {
                @Override
                public String getPeer()
                {
                    String peer = null;
                    if (timGroupSystemElem != null)
                    {
                        peer = timGroupSystemElem.getGroupId();
                    } else
                    {
                        peer = getSDKMsg().getConversation().getPeer();
                    }
                    return peer;
                }

                @Override
                public FIMConversationType getType()
                {
                    switch (getSDKMsg().getConversation().getType())
                    {
                        case C2C:
                            return FIMConversationType.C2C;
                        case Group:
                            return FIMConversationType.Group;
                        case System:
                            return FIMConversationType.System;
                        default:
                            return FIMConversationType.Invalid;
                    }
                }

                @Override
                public long getUnreadMessageNum()
                {
                    return getSDKMsg().getConversation().getUnreadMessageNum();
                }
            };
        }
        return mConversation;
    }

    @Override
    public boolean remove()
    {
        return getSDKMsg().remove();
    }

    @Override
    protected String onParseToJson(TIMMessage sdkMsg) throws Exception
    {
        FIMMsgData result = null;

        long count = getSDKMsg().getElementCount();
        TIMElem elem = null;
        for (int i = 0; i < count; i++)
        {
            elem = getSDKMsg().getElement(i);
            if (elem == null)
            {
                continue;
            }
            TIMElemType elemType = elem.getType();
            switch (elemType)
            {
                case Custom:
                    timCustomElem = (TIMCustomElem) elem;
                    break;
                case GroupSystem:
                    timGroupSystemElem = (TIMGroupSystemElem) elem;
                    break;
                case Image:
                    this.timImageElem = (TIMImageElem) elem;
                    break;
                case Sound:
                    this.timSoundElem = (TIMSoundElem) elem;
                    break;
                case Text:
                    this.timTextElem = (TIMTextElem) elem;
                    break;
                default:
                    break;
            }
        }

        byte[] data = null;
        if (timGroupSystemElem != null)
            data = timGroupSystemElem.getUserData();

        if (data == null && timCustomElem != null)
            data = timCustomElem.getData();

        if (data != null)
            return new String(data, DEFAULT_CHARSET);

        return null;
    }

    @Override
    protected FIMMsgData<TIMMessage> onParseToMsgData(int type, String json, Class clazz) throws Exception
    {
        return null;
    }
}
