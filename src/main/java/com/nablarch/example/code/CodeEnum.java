package com.nablarch.example.code;

import nablarch.core.validation.ee.EnumElement;
import nablarch.core.validation.ee.EnumElement.WithValue;
/**
 * コード値Enumを定義したインタフェース。
 *
 * @author Nabu Rakutaro
 */
public interface CodeEnum extends WithValue<String> {
    /**
     * ラベルを返却する。
     * @return ラベル
     */
    String getLabel();

    /**
     * コード値を返却する。
     * また、{@link EnumElement}で許容する列挙値を取得するためにも使用する。
     * @return コード値
     */
    @Override
    String getValue();
}
