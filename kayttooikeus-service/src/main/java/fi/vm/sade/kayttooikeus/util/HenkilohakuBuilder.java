package fi.vm.sade.kayttooikeus.util;

import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.dto.HenkilohakuCriteriaDto;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioMinimalDto;
import fi.vm.sade.kayttooikeus.dto.PalveluRooliGroup;
import fi.vm.sade.kayttooikeus.enumeration.OrderByHenkilohaku;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.OrganisaatioHenkilo;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.HenkiloHibernateRepository;
import fi.vm.sade.kayttooikeus.repositories.OrganisaatioHenkiloRepository;
import fi.vm.sade.kayttooikeus.repositories.criteria.HenkiloCriteria;
import fi.vm.sade.kayttooikeus.repositories.dto.HenkilohakuResultDto;
import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioPerustieto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
public class HenkilohakuBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(HenkilohakuBuilder.class);
    private static final Long DEFAULT_LIMIT = 100L;

    private HenkilohakuCriteriaDto henkilohakuCriteriaDto;
    private LinkedHashSet<HenkilohakuResultDto> henkilohakuResultDtoList = new LinkedHashSet<>();
    private Long henkilohakuResultCount;

    private final HenkiloHibernateRepository henkiloHibernateRepository;
    private final OrikaBeanMapper mapper;
    private final PermissionCheckerService permissionCheckerService;
    private final HenkiloDataRepository henkiloDataRepository;
    private final OrganisaatioClient organisaatioClient;
    private final OrganisaatioHenkiloRepository organisaatioHenkiloRepository;
    private final CommonProperties commonProperties;

    public HenkilohakuBuilder builder(HenkilohakuCriteriaDto henkilohakuCriteriaDto) {
        this.henkilohakuCriteriaDto = henkilohakuCriteriaDto;
        this.henkilohakuResultDtoList = new LinkedHashSet<>();
        return this;
    }

    public Collection<HenkilohakuResultDto> build() {
        return this.henkilohakuResultDtoList;
    }
    public Long buildHakuResultCount() { return this.henkilohakuResultCount; }

    // Find nimi, kayttajatunnus and oidHenkilo
    public HenkilohakuBuilder search(Long offset, OrderByHenkilohaku orderBy) {
        return search(offset, DEFAULT_LIMIT, orderBy);
    }

    public HenkilohakuBuilder search(Long offset, Long limit, OrderByHenkilohaku orderBy) {
        // Because jpaquery limitations this can't be done with subqueries and union all.
        // This needs to be done in 2 queries because postgres query planner can't optimise it correctly because of
        // kayttajatiedot outer join and where or combination.
        this.henkilohakuResultDtoList = new LinkedHashSet<>();

        // 1) käyttäjätunnuksella haku
        this.henkilohakuResultDtoList.addAll(findByUsername(offset));

        // 2) nimellä haku
        this.henkilohakuResultDtoList.addAll(findByName(criteria -> this.henkiloHibernateRepository
                .findByCriteria(criteria,
                        offset,
                        limit,
                        orderBy != null ? orderBy.getValue() : null)));

        return this;
    }

    // Find count of result with criteria
    public HenkilohakuBuilder searchCount() {
        // 1) käyttäjätunnuksella haku
        List<HenkilohakuResultDto> henkiloCountByUsername = findByUsername(0L);

        // 2) nimellä haku
        List<String> henkiloOids = henkiloCountByUsername.stream().map( h -> h.getOidHenkilo()).collect(Collectors.toList());
        Long henkiloCountByCriteria = findByName(criteria -> this.henkiloHibernateRepository
                .findByCriteriaCount(criteria, henkiloOids));

        this.henkilohakuResultCount = henkiloCountByCriteria + henkiloCountByUsername.size();
        return this;
    }

    private List<HenkilohakuResultDto> findByUsername(Long offset) {
        HenkiloCriteria criteria = this.mapper.map(this.henkilohakuCriteriaDto, HenkiloCriteria.class);
        if (criteria.getKayttajatunnus() == null) {
            criteria.setKayttajatunnus(criteria.getNameQuery());
        }
        criteria.setSukunimi(null);
        criteria.setNameQuery(null);
        return henkiloHibernateRepository.findByUsername(criteria, offset);
    }

    private <T> T findByName(Function<HenkiloCriteria, T> findByFunction) {
        HenkiloCriteria criteria = this.mapper.map(this.henkilohakuCriteriaDto, HenkiloCriteria.class);
        criteria.setKayttajatunnus(null);
        return findByFunction.apply(criteria);
    }

    // Remove henkilos the user has no access (and who have organisation)
    public HenkilohakuBuilder exclusion() {
        // vain rekisterinpitäjä saa hakea henkilöitä, joilla ei ole organisaatiota
        if (!permissionCheckerService.isCurrentUserAdmin()
                && Boolean.TRUE.equals(henkilohakuCriteriaDto.getNoOrganisation())) {
            LOGGER.warn(String.format("Käyttäjällä %s ei ole oikeuksia hakea henkilöitä, joilla ei ole organisaatiota", permissionCheckerService.getCurrentUserOid()));
            henkilohakuCriteriaDto.setNoOrganisation(null);
        }

        List<String> currentUserOrganisaatioOids = this.organisaatioHenkiloRepository
                .findUsersOrganisaatioHenkilosByPalveluRoolis(this.permissionCheckerService.getCurrentUserOid(), PalveluRooliGroup.HENKILOHAKU);

        Set<String> criteriaOrganisaatioOids = henkilohakuCriteriaDto.getOrganisaatioOids() != null
                ? henkilohakuCriteriaDto.getOrganisaatioOids()
                : new HashSet<>(currentUserOrganisaatioOids);
        boolean juuriorganisaatioHaku = criteriaOrganisaatioOids.contains(commonProperties.getRootOrganizationOid());

        if (Boolean.TRUE.equals(henkilohakuCriteriaDto.getSubOrganisation()) && !juuriorganisaatioHaku) {
            // haetaan myös aliorganisaatioista
            criteriaOrganisaatioOids = criteriaOrganisaatioOids.stream()
                    .flatMap(organisaatioOid -> Stream.concat(Stream.of(organisaatioOid),
                            organisaatioClient.getChildOids(organisaatioOid).stream()))
                    .collect(toSet());
        }
        if (!this.permissionCheckerService.isCurrentUserMiniAdmin()) {
            if (henkilohakuCriteriaDto.getOrganisaatioOids() != null) {
                // suodatetaan käyttäjän organisaatioilla
                Set<String> kayttajaOrganisaatioOids = currentUserOrganisaatioOids.stream()
                        .flatMap(organisaatioOid -> Stream.concat(Stream.of(organisaatioOid),
                                organisaatioClient.getChildOids(organisaatioOid).stream()))
                        .collect(toSet());
                criteriaOrganisaatioOids.retainAll(kayttajaOrganisaatioOids);
            }
        } else {
            if (Boolean.TRUE.equals(henkilohakuCriteriaDto.getSubOrganisation()) && juuriorganisaatioHaku) {
                // oph-virkailija hakee kaikista organisaatioista
                criteriaOrganisaatioOids = null;
            }
        }
        henkilohakuCriteriaDto.setOrganisaatioOids(criteriaOrganisaatioOids);
        return this;
    }

    // Find organisaatioNimiList
    public HenkilohakuBuilder enrichment() {
        List<String> oidList = this.henkilohakuResultDtoList.stream().map(HenkilohakuResultDto::getOidHenkilo).collect(Collectors.toList());
        List<Henkilo> henkiloList = this.henkiloDataRepository.readByOidHenkiloIn(oidList);
        this.henkilohakuResultDtoList = this.henkilohakuResultDtoList.stream().map(henkilohakuResultDto -> {
            Henkilo henkilo = henkiloList.stream().filter(henkilo1 -> Objects.equals(henkilo1.getOidHenkilo(), henkilohakuResultDto.getOidHenkilo()))
                    .findFirst()
                    .orElseThrow(IllegalStateException::new);
            henkilohakuResultDto.setOrganisaatioNimiList(henkilo.getOrganisaatioHenkilos().stream()
                    .filter(((Predicate<OrganisaatioHenkilo>) OrganisaatioHenkilo::isPassivoitu).negate())
                    .map(OrganisaatioHenkilo::getOrganisaatioOid)
                    .map(organisaatioOid -> {
                        OrganisaatioPerustieto organisaatio = this.organisaatioClient.getOrganisaatioPerustiedotCached(organisaatioOid)
                                .orElseGet(() -> UserDetailsUtil.createUnknownOrganisation(organisaatioOid));
                        return new OrganisaatioMinimalDto(organisaatioOid, organisaatio.getTyypit(), organisaatio.getNimi());
                    })
                    .collect(Collectors.toList()));
            return henkilohakuResultDto;
        }).collect(Collectors.toCollection(LinkedHashSet::new));

        return this;
    }

}
