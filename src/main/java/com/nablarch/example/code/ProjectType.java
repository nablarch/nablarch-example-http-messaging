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
    private final String label;
    /** プロジェクト分類のコード */
    private final String value;

    /**
     * コンストラクタ。
     * @param value コード値
     * @param label ラベル
     */
    ProjectType(String value, String label) {
        this.label = label;
        this.value = value;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getValue() {
        return value;
    }
}
