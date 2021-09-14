package sp.phone.mvp.contract;

import sp.phone.param.MessagePostParam;
import sp.phone.task.MessagePostTask;

/**
 * Created by Justwen on 2017/5/28.
 */

public interface MessagePostContract {

    interface Presenter {

        void commit(String title, String to, String body);

        void setPostParam(MessagePostParam param);

    }

    interface View {

        void setRecipient(String recipient);

        void finish(int resultCode);

        void hideRecipientEditor();
    }

    interface Model  {

        void postMessage(MessagePostParam param, MessagePostTask.CallBack callBack);

    }
}
