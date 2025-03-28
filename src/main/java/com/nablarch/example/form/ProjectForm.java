package com.nablarch.example.form;

import java.util.Date;
import java.util.regex.Pattern;

import jakarta.validation.constraints.AssertTrue;

import nablarch.core.util.DateUtil;
import nablarch.core.util.StringUtil;
import nablarch.core.validation.ee.Domain;
import nablarch.core.validation.ee.Required;

/**
 * プロジェクト登録フォーム。
 *
 * @author Nabu Rakutaro
 */
public class ProjectForm {

    /** スラッシュを表す正規表現 */
    private static final Pattern SLASH_PATTERN = Pattern.compile("/", Pattern.LITERAL);

    /** プロジェクト名 */
    @Required
    @Domain("projectName")
    private String projectName;

    /** プロジェクト種別 */
    @Required
    @Domain("projectType")
    private String projectType;

    /** プロジェクト分類 */
    @Required
    @Domain("projectClass")
    private String projectClass;

    /** プロジェクトマネージャー名 */
    @Domain("userName")
    private String projectManager;

    /** プロジェクトリーダー名 */
    @Domain("userName")
    private String projectLeader;

    /** 顧客ID */
    @Domain("id")
    @Required
    private String clientId;

    /** 顧客名 */
    @Domain("clientName")
    private String clientName;

    /** プロジェクト開始日文字列 */
    @Domain("date")
    private String projectStartDate;

    /** プロジェクト終了日文字列 */
    @Domain("date")
    private String projectEndDate;

    /** 備考 */
    @Domain("note")
    private String note;

    /** 売上高 */
    @Domain("amountOfMoney")
    private String sales;

    /** 売上原価 */
    @Domain("amountOfMoney")
    private String costOfGoodsSold;

    /** 販管費 */
    @Domain("amountOfMoney")
    private String sga;

    /** 本社配賦 */
    @Domain("amountOfMoney")
    private String allocationOfCorpExpenses;

    /**
     * プロジェクト名を取得する。
     *
     * @return プロジェクト名
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * プロジェクト種別を取得する。
     *
     * @return プロジェクト種別
     */
    public String getProjectType() {
        return projectType;
    }

    /**
     * プロジェクト分類を取得する。
     *
     * @return プロジェクト分類
     */
    public String getProjectClass() {
        return projectClass;
    }

    /**
     * プロジェクトマネージャー名を取得する。
     *
     * @return プロジェクトマネージャー名
     */
    public String getProjectManager() {
        return projectManager;
    }

    /**
     * プロジェクトリーダー名を取得する。
     *
     * @return プロジェクトリーダー名
     */
    public String getProjectLeader() {
        return projectLeader;
    }

    /**
     * 顧客IDを取得する。
     *
     * @return 顧客ID
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * 顧客IDを保持しているか否かを返す。
     * @return trueの場合は、顧客IDを保持している。
     */
    public boolean hasClientId() {
        return clientId != null;
    }

    /**
     * 顧客名を取得する。
     *
     * @return 顧客名
     */
    public String getClientName() {
        return clientName;
    }


    /**
     * プロジェクト開始日を取得する。
     * @return プロジェクト開始日
     */
    public String getProjectStartDate() {
        return projectStartDate;
    }

    /**
     * プロジェクト終了日を取得する。
     * @return プロジェクト終了日
     */
    public String getProjectEndDate() {
        return projectEndDate;
    }

    /**
     * 備考を取得する。
     *
     * @return 備考
     */
    public String getNote() {
        return note;
    }

    /**
     * 売上高を取得する。
     *
     * @return 売上高
     */
    public String getSales() {
        return sales;
    }

    /**
     * 売上原価を取得する。
     *
     * @return 売上原価
     */
    public String getCostOfGoodsSold() {
        return costOfGoodsSold;
    }

    /**
     * 販管費を取得する。
     *
     * @return 販管費
     */
    public String getSga() {
        return sga;
    }

    /**
     * 本社配賦を取得する。
     *
     * @return 本社配賦
     */
    public String getAllocationOfCorpExpenses() {
        return allocationOfCorpExpenses;
    }

    /**
     * プロジェクト名を設定する。
     *
     * @param projectName 設定するプロジェクト名
     *
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     * プロジェクト種別を設定する。
     *
     * @param projectType 設定するプロジェクト種別
     */
    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    /**
     * プロジェクト分類を設定する。
     *
     * @param projectClass 設定するプロジェクト分類
     */
    public void setProjectClass(String projectClass) {
        this.projectClass = projectClass;
    }

    /**
     * プロジェクトマネージャー名を設定する。
     *
     * @param projectManager 設定するプロジェクトマネージャー名
     */
    public void setProjectManager(String projectManager) {
        this.projectManager = projectManager;
    }

    /**
     * プロジェクトリーダー名を設定する。
     *
     * @param projectLeader 設定するプロジェクトリーダー名
     *
     */
    public void setProjectLeader(String projectLeader) {
        this.projectLeader = projectLeader;
    }

    /**
     * 顧客IDを設定する。
     *
     * @param clientId 設定する顧客ID
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * 顧客名を設定する。
     *
     * @param clientName 設定する顧客名
     *
     */
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    /**
     * プロジェクト開始日を設定する。
     *
     * @param projectStartDate プロジェクト開始日
     */
    public void setProjectStartDate(String projectStartDate) {
        this.projectStartDate = StringUtil.isNullOrEmpty(projectStartDate) ?
                null : SLASH_PATTERN.matcher(projectStartDate).replaceAll("");
    }

    /**
     * プロジェクト終了日を設定する。
     *
     * @param projectEndDate プロジェクト終了日
     */
    public void setProjectEndDate(String projectEndDate) {
        this.projectEndDate = StringUtil.isNullOrEmpty(projectEndDate) ?
                null : SLASH_PATTERN.matcher(projectEndDate).replaceAll("");
    }


    /**
     * 備考を設定する。
     *
     * @param note 設定する備考
     *
     */
    public void setNote(String note) {
        this.note = note;
    }

    /**
     * 売上高を設定する。
     *
     * @param sales 売上高
     */
    public void setSales(String sales) {
        this.sales = StringUtil.isNullOrEmpty(sales) ? null : sales;
    }

    /**
     * 売上原価を設定する。
     *
     * @param costOfGoodsSold 売上原価
     */
    public void setCostOfGoodsSold(String costOfGoodsSold) {
        this.costOfGoodsSold = StringUtil.isNullOrEmpty(costOfGoodsSold) ? null : costOfGoodsSold;
    }

    /**
     * 販管費を設定する。
     *
     * @param sga 販管費
     */
    public void setSga(String sga) {
        this.sga = StringUtil.isNullOrEmpty(sga) ? null : sga;
    }

    /**
     * 本社配賦を設定する。
     *
     * @param allocationOfCorpExpenses 本社配賦
     */
    public void setAllocationOfCorpExpenses(String allocationOfCorpExpenses) {
        this.allocationOfCorpExpenses = StringUtil.isNullOrEmpty(allocationOfCorpExpenses) ? null : allocationOfCorpExpenses;
    }


    /**
     * プロジェクト期間（プロジェクト開始日～プロジェクト終了日）正しく設定されているかを判定します。
     * 開始日に終了日より後の日付が設定されていた場合はプロジェクト期間が正しくないと判定します。
     * それ以外の場合は問題なしとします。
     *
     * @return 開始日に終了日より後の日付が設定されていた場合は false それ以外（開始日、終了日の両方又はいずれかが未定の場合も含む）は true
     */
    @AssertTrue(message = "プロジェクト終了日はプロジェクト開始日より後の日付を入力して下さい。")
    private boolean isValidProjectPeriod() {
        if (StringUtil.hasValue(projectStartDate) && StringUtil.hasValue(projectEndDate)
                && DateUtil.isValid(projectStartDate, "yyyyMMdd") && DateUtil.isValid(projectEndDate, "yyyyMMdd")) {
            if (toDate(projectStartDate).compareTo(toDate(projectEndDate)) > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * yyyyMMdd形式の文字列をjava.sql.Date型に変換する。
     *
     * @param dateStr yyyyMMdd形式の文字列
     * @return java.sql.Dateオブジェクト
     */
    private static Date toDate(String dateStr) {
        return DateUtil.getDate(SLASH_PATTERN.matcher(dateStr).replaceAll(""));
    }

}
