package com.nablarch.example.action;

import nablarch.test.core.messaging.MessagingRequestTestSupport;
import org.junit.Test;

/**
 * プロジェクト登録ウェブサービスのリクエスト単体テストクラス。
 *
 * @author Nabu Rakutaro
 */
public class ProjectSaveActionRequestTest  extends MessagingRequestTestSupport {

    /**
     * 正常終了のテストケース。
     * <p/>
     * 全ての出力対象フィールドにデータがある場合。
     */
    @Test
    public void testNormalEndExistAllFields() {
        execute();
    }

    /**
     * 正常終了のテストケース。
     * <p/>
     * 必須の出力対象フィールドにのみデータがある場合。
     */
    @Test
    public void testNormalEndOnlyRequireFields() {
        execute();
    }


    /** 異常系のテスト。 */
    @Test
    public void testAbNormalEnd() {
        execute();
    }
}
