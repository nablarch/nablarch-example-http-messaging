package com.nablarch.example.domain;

import nablarch.core.validation.ee.DomainManager;

/**
 * ドメイン定義を返すドメインマネージャクラス。
 *
 * @author Nabu Rakutaro
 */
public class HttpMessagingExampleDomainManager implements DomainManager<HttpMessagingExampleDomain> {

    @Override
    public Class<HttpMessagingExampleDomain> getDomainBean() {
        return HttpMessagingExampleDomain.class;
    }
}
