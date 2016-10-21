package com.nablarch.example.code;

/**
 * コード値Enumを定義したインタフェース。
 *
 * @author Nabu Rakutaro
 */
public interface CodeEnum {
    /**
     * ラベルを返却する。
     * @return ラベル
     */
    String getLabel();

    /**
     * コード値を返却する。
     * @return コード値
     */
    String getCode();
}
