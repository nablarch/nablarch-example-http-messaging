package com.nablarch.example.client;

import com.nablarch.example.form.ProjectForm;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

/**
 * プロジェクトAPIのテスト起動用クライアント。
 *
 * @author Nabu Rakutaro
 */
public class ProjectClient {

    /** プロジェクト登録URL */
    private static final String TARGET_URL = "http://localhost:9080/ProjectSaveAction";

    public static void main(String[] args) throws Exception {

        // バリデーションエラーが発生するプロジェクト情報を登録
        postProject(getInvalidProject());

        // 正常なプロジェクト情報を登録
        postProject(getValidProject());
    }

    /**
     * プロジェクトをPOSTして登録する。
     * @param project 登録プロジェクト
     */
    private static void postProject(ProjectForm project) {

        int statusCode = ClientBuilder.newClient()
                .target(TARGET_URL)
                .request(MediaType.APPLICATION_JSON)
                .acceptEncoding("UTF-8")
                .acceptLanguage("ja")
                .header("X-Message-Id", "1")
                .post(Entity.json(project)).getStatus();

        System.out.println("####" + statusCode + "####");
    }

    /**
     * 正常に登録でききるプロジェクトを返す。
     * @return プロジェクト
     */
    private static ProjectForm getValidProject() {
        ProjectForm project = new ProjectForm();
        project.setProjectName("プロジェクト名");
        project.setProjectType("development");
        project.setProjectClass("a");
        project.setProjectStartDate("20140101");
        project.setProjectEndDate("20150331");
        project.setClientId("1");
        project.setProjectManager("佐藤");
        project.setProjectLeader("鈴木");
        project.setNote("備考８８８");
        project.setSales("20000");
        project.setCostOfGoodsSold("2000");
        project.setSga("3000");
        project.setAllocationOfCorpExpenses("4000");
        return project;
    }

    /**
     * バリデーションエラーが発生するプロジェクトを返す。
     * @return プロジェクト
     */
    private static ProjectForm getInvalidProject() {
        ProjectForm project = new ProjectForm();
        project.setProjectName("test");
        project.setProjectType("development");
        project.setProjectClass("a");
        project.setProjectStartDate("20150101");
        project.setProjectEndDate("20150331");
        project.setClientId("1");
        project.setProjectManager("佐藤");
        project.setProjectLeader("鈴木");
        project.setNote("備考８８８");
        project.setSales("20000");
        project.setCostOfGoodsSold("2000");
        project.setSga("3000");
        project.setAllocationOfCorpExpenses("4000");
        return project;
    }
}