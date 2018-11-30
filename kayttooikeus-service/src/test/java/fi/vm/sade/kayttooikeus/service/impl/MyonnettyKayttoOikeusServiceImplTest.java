package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.HenkiloVarmentaja;
import fi.vm.sade.kayttooikeus.model.MyonnettyKayttoOikeusRyhmaTapahtuma;
import fi.vm.sade.kayttooikeus.model.OrganisaatioHenkilo;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRyhmaTapahtumaHistoriaDataRepository;
import fi.vm.sade.kayttooikeus.repositories.MyonnettyKayttoOikeusRyhmaTapahtumaRepository;
import fi.vm.sade.kayttooikeus.repositories.OrganisaatioHenkiloRepository;
import fi.vm.sade.kayttooikeus.service.LdapSynchronizationService;
import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import org.assertj.core.util.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static fi.vm.sade.kayttooikeus.util.CreateUtil.createHenkilo;
import static fi.vm.sade.kayttooikeus.util.CreateUtil.createMyonnettyKayttoOikeusRyhmaTapahtumaWithOrganisation;
import static fi.vm.sade.kayttooikeus.util.CreateUtil.createOrganisaatioHenkilo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
public class MyonnettyKayttoOikeusServiceImplTest {
    @InjectMocks
    private MyonnettyKayttoOikeusServiceImpl myonnettyKayttoOikeusService;

    @Mock
    private PermissionCheckerService permissionCheckerService;

    @Mock
    private HenkiloDataRepository henkiloDataRepository;

    @Mock
    private MyonnettyKayttoOikeusRyhmaTapahtumaRepository myonnettyKayttoOikeusRyhmaTapahtumaRepository;

    @Mock
    private KayttoOikeusRyhmaTapahtumaHistoriaDataRepository kayttoOikeusRyhmaTapahtumaHistoriaDataRepository;

    @Mock
    private OrganisaatioHenkiloRepository organisaatioHenkiloRepository;

    @Mock
    private LdapSynchronizationService ldapSynchronizationService;

    @Test
    public void varmentajallaOnYhaOikeuksiaSamaanOrganisaatioon() {
        Henkilo henkilo = Henkilo.builder().oidHenkilo("kasittelija").build();
        given(this.henkiloDataRepository.findByOidHenkilo(eq("kasittelija"))).willReturn(Optional.of(henkilo));

        Henkilo varmennettavaHenkilo = Henkilo.builder()
                .oidHenkilo("varmennettava")
                .build();
        HenkiloVarmentaja henkiloVarmentaja = new HenkiloVarmentaja();
        henkiloVarmentaja.setTila(true);
        henkiloVarmentaja.setVarmennettavaHenkilo(varmennettavaHenkilo);

        Henkilo varmentavaHenkilo = Henkilo.builder().oidHenkilo("varmentaja")
                .henkiloVarmennettavas(Collections.singleton(henkiloVarmentaja))
                .build();
        henkiloVarmentaja.setVarmentavaHenkilo(varmentavaHenkilo);
        OrganisaatioHenkilo poistuvanOikeudenOrganisaatioHenkilo = OrganisaatioHenkilo.builder()
                .organisaatioOid("1.2.0.0.1")
                .henkilo(varmentavaHenkilo)
                .build();
        MyonnettyKayttoOikeusRyhmaTapahtuma poistuvaKayttooikeus = MyonnettyKayttoOikeusRyhmaTapahtuma.builder()
                .organisaatioHenkilo(poistuvanOikeudenOrganisaatioHenkilo)
                .build();
        poistuvaKayttooikeus.setId(1L);

        List<MyonnettyKayttoOikeusRyhmaTapahtuma> kayttoOikeudet = new ArrayList<>();
        kayttoOikeudet.add(poistuvaKayttooikeus);
        given(this.myonnettyKayttoOikeusRyhmaTapahtumaRepository.findByVoimassaLoppuPvmBefore(any())).willReturn(kayttoOikeudet);

        OrganisaatioHenkilo yhaOlemassaOlevanOikeudenOrganisaatioHenkilo = OrganisaatioHenkilo.builder()
                .organisaatioOid("1.2.0.0.1")
                .build();
        MyonnettyKayttoOikeusRyhmaTapahtuma yhaOlemassaOlevaKayttooikeus = MyonnettyKayttoOikeusRyhmaTapahtuma.builder()
                .organisaatioHenkilo(yhaOlemassaOlevanOikeudenOrganisaatioHenkilo)
                .build();
        yhaOlemassaOlevaKayttooikeus.setId(2L);
        List<MyonnettyKayttoOikeusRyhmaTapahtuma> varmentajanOikeudet = new ArrayList<>();
        varmentajanOikeudet.add(poistuvaKayttooikeus);
        varmentajanOikeudet.add(yhaOlemassaOlevaKayttooikeus);
        given(this.myonnettyKayttoOikeusRyhmaTapahtumaRepository.findByOrganisaatioHenkiloHenkiloOidHenkilo(eq("varmentaja")))
                .willReturn(varmentajanOikeudet);

        this.myonnettyKayttoOikeusService.poistaVanhentuneet("kasittelija");

        assertThat(henkiloVarmentaja.isTila()).isTrue();
        verify(kayttoOikeusRyhmaTapahtumaHistoriaDataRepository, times(1)).save(any());
        verify(myonnettyKayttoOikeusRyhmaTapahtumaRepository, times(1)).delete(any());
        verify(ldapSynchronizationService, times(1)).updateHenkilo(any());
    }

    @Test
    public void varmentajallaEiOleEnaaOikeuksiaSamaanOrganisaatioon() {
        Henkilo henkilo = Henkilo.builder().oidHenkilo("kasittelija").build();
        given(this.henkiloDataRepository.findByOidHenkilo(eq("kasittelija"))).willReturn(Optional.of(henkilo));

        Henkilo varmennettavaHenkilo = Henkilo.builder()
                .oidHenkilo("varmennettava")
                .build();
        HenkiloVarmentaja henkiloVarmentaja = new HenkiloVarmentaja();
        henkiloVarmentaja.setTila(true);
        henkiloVarmentaja.setVarmennettavaHenkilo(varmennettavaHenkilo);

        Henkilo varmentavaHenkilo = Henkilo.builder().oidHenkilo("varmentaja")
                .henkiloVarmennettavas(Collections.singleton(henkiloVarmentaja))
                .build();
        henkiloVarmentaja.setVarmentavaHenkilo(varmentavaHenkilo);
        OrganisaatioHenkilo poistuvanOikeudenOrganisaatioHenkilo = OrganisaatioHenkilo.builder()
                .organisaatioOid("1.2.0.0.1")
                .henkilo(varmentavaHenkilo)
                .build();
        MyonnettyKayttoOikeusRyhmaTapahtuma poistuvaKayttooikeus = MyonnettyKayttoOikeusRyhmaTapahtuma.builder()
                .organisaatioHenkilo(poistuvanOikeudenOrganisaatioHenkilo)
                .build();
        poistuvaKayttooikeus.setId(1L);

        List<MyonnettyKayttoOikeusRyhmaTapahtuma> kayttoOikeudet = new ArrayList<>();
        kayttoOikeudet.add(poistuvaKayttooikeus);
        given(this.myonnettyKayttoOikeusRyhmaTapahtumaRepository.findByVoimassaLoppuPvmBefore(any())).willReturn(kayttoOikeudet);

        List<MyonnettyKayttoOikeusRyhmaTapahtuma> varmentajanOikeudet = new ArrayList<>();
        varmentajanOikeudet.add(poistuvaKayttooikeus);
        given(this.myonnettyKayttoOikeusRyhmaTapahtumaRepository.findByOrganisaatioHenkiloHenkiloOidHenkilo(eq("varmentaja")))
                .willReturn(varmentajanOikeudet);

        this.myonnettyKayttoOikeusService.poistaVanhentuneet("kasittelija");

        assertThat(henkiloVarmentaja.isTila()).isFalse();
        verify(kayttoOikeusRyhmaTapahtumaHistoriaDataRepository, times(1)).save(any());
        verify(myonnettyKayttoOikeusRyhmaTapahtumaRepository, times(1)).delete(any());
        verify(ldapSynchronizationService, times(1)).updateHenkilo(any());
    }

}
