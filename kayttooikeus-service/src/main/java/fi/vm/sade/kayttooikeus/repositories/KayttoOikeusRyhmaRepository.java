package fi.vm.sade.kayttooikeus.repositories;

import com.querydsl.core.Tuple;
import fi.vm.sade.kayttooikeus.dto.KayttoOikeusRyhmaDto;
import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;

import java.util.List;
import java.util.Optional;

public interface KayttoOikeusRyhmaRepository extends BaseRepository<KayttoOikeusRyhma> {
    List<KayttoOikeusRyhmaDto> findByIdList(List<Long> idList);

    Optional<KayttoOikeusRyhma> findByRyhmaId(Long id);

    Boolean ryhmaNameFiExists(String ryhmaNameFi);

    List<KayttoOikeusRyhmaDto> listAll();

    List<Tuple> findOrganisaatioOidAndRyhmaIdByHenkiloOid(String oid);

    List<KayttoOikeusRyhmaDto> findKayttoOikeusRyhmasByKayttoOikeusIds(List<Long> kayttoOikeusIds);
}
