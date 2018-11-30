package fi.vm.sade.kayttooikeus.service;

public interface OrganisaatioService {

    void updateOrganisaatioCache();

    Long getClientCacheState();

    void throwIfActiveNotFound(String organisaatioOid);
}
