package com.nablarch.example.action;

import nablarch.test.core.messaging.MessagingRequestTestSupport;
import nablarch.test.junit5.extension.messaging.MessagingRequestTest;
import org.junit.jupiter.api.Test;

/**
 * プロジェクト登録ウェブサービスのリクエスト単体テストクラス。
 *
 * @author Nabu Rakutaro
 */
@MessagingRequestTest
class ProjectSaveActionRequestTest {

    MessagingRequestTestSupport support;

    /**
     * 正常終了のテストケース。
     * <p/>
     * 全ての出力対象フィールドにデータがある場合。
     */
    @Test
    void testNormalEndExistAllFields() {
        support.execute(support.testName.getMethodName());
    }

    /**
     * 正常終了のテストケース。
     * <p/>
     * 必須の出力対象フィールドにのみデータがある場合。
     */
    @Test
    void testNormalEndOnlyRequireFields() {
        support.execute(support.testName.getMethodName());
    }


    /** 異常系のテスト。 */
    @Test
    void testAbNormalEnd() {
        support.execute(support.testName.getMethodName());
    }
}
