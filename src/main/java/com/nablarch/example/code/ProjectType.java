package com.nablarch.example.code;

/**
 * プロジェクト分類を定義したEnumクラス。
 *
 * @author Nabu Rakutaro
 */
public enum ProjectType implements CodeEnum {
    /** 新規開発PJ */
    DEVELOPMENT("development", "新規開発PJ"),
    /** 保守PJ */
    MAINTENANCE("maintenance", "保守PJ");

    /** プロジェクト分類のラベル */
    private String label;
    /** プロジェクト分類のコード */
    private String code;

    /**
     * コンストラクタ。
     * @param code コード値
     * @param label ラベル
     */
    ProjectType(String code, String label) {
        this.label = label;
        this.code = code;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel() {
        return label;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCode() {
        return code;
    }
}
