package fi.vm.sade.kayttooikeus.config.mapper;

import fi.vm.sade.kayttooikeus.dto.IdentifiedHenkiloTypeDto;
import fi.vm.sade.kayttooikeus.dto.KayttajatiedotReadDto;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.Identification;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;
import org.springframework.stereotype.Component;

@Component
public class IdentificationConverter extends CustomConverter<Identification, IdentifiedHenkiloTypeDto> {

    @Override
    public IdentifiedHenkiloTypeDto convert(Identification identification, Type<? extends IdentifiedHenkiloTypeDto> destinationType, MappingContext mappingContext) {
        Henkilo henkilo = identification.getHenkilo();
        return IdentifiedHenkiloTypeDto.builder()
                .identifier(identification.getIdentifier())
                .idpEntityId(identification.getIdpEntityId())
                .oidHenkilo(henkilo.getOidHenkilo())
                .version(identification.getVersion())
                .id(henkilo.getId())
                .kayttajatiedot(henkilo.getKayttajatiedot() != null ? new KayttajatiedotReadDto(henkilo.getKayttajatiedot().getUsername()) : null)
                .build();
    }

}
