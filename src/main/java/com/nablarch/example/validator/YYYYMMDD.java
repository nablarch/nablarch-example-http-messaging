package com.nablarch.example.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import nablarch.core.util.DateUtil;
import nablarch.core.util.StringUtil;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 入力値が指定した形式の日付文字列であるかをバリデーションするクラス。
 * <p/>
 * 日付フォーマットのデフォルト値は「yyyyMMdd」である。
 *
 * @author Nabu Rakutaro
 */
@Documented
@Constraint(validatedBy = { YYYYMMDD.YYYYMMDDValidator.class })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
public @interface YYYYMMDD {

    /** グループを取得する。 */
    Class<?>[] groups() default { };

    /** バリデーションエラー発生時に設定するメッセージ。 */
    String message() default "{com.nablarch.example.app.entity.core.validation.validator.YYYYMMDD.message}";

    /** Payloadを取得する。 */
    Class<? extends Payload>[] payload() default { };

    /** 許容するフォーマット */
    String allowFormat() default "yyyyMMdd";

    /** 複数指定用のアノテーション */
    @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
    @Retention(RUNTIME)
    @Documented
    public @interface List {
        /** YYYYMMDDの配列を取得する。 */
        YYYYMMDD[] value();
    }

    /**
     * 日付フォーマットバリデータの実装クラス。
     */
    class YYYYMMDDValidator implements ConstraintValidator<YYYYMMDD, String> {

        /** 許容するフォーマット */
        private String allowFormat;

        /**
         * 検証処理を初期化する。
         * @param constraintAnnotation 対象プロパティに付与されたアノテーション
         */
        @Override
        public void initialize(YYYYMMDD constraintAnnotation) {
            this.allowFormat = constraintAnnotation.allowFormat();
        }

        /**
         * 対象の値が {@code allowFormat} で指定するフォーマットに適合しているか検証する。
         * @param value 対象の値
         * @param context バリデーションコンテキスト
         * @return フォーマットに適合している場合 {@code true}
         */
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (StringUtil.isNullOrEmpty(value)) {
                return true;
            }
            try {
                return DateUtil.getParsedDate(value, allowFormat) != null;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }

}
