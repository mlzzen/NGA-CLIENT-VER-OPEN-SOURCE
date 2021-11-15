package sp.phone.mvp.contract;

import sp.phone.mvp.model.entity.Board;

/**
 * Created by Justwen on 2017/6/29.
 */

public interface BoardContract {

    interface Presenter {

        boolean addBoard(String fid, String name, String stid);

    }
}
