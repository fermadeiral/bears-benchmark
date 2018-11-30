package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.KayttoOikeusHistoriaDto;
import fi.vm.sade.kayttooikeus.dto.KayttooikeusPerustiedotDto;
import fi.vm.sade.kayttooikeus.dto.PalveluKayttoOikeusDto;
import fi.vm.sade.kayttooikeus.repositories.criteria.KayttooikeusCriteria;
import fi.vm.sade.kayttooikeus.service.KayttoOikeusService;
import fi.vm.sade.kayttooikeus.service.TaskExecutorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@RestController
@RequestMapping("/kayttooikeus")
@Api(tags = "Käyttöoikeuksien käsittelyyn liittyvät operaatiot.")
public class KayttoOikeusController {
    private KayttoOikeusService kayttoOikeusService;
    private TaskExecutorService taskExecutorService;

    @Autowired
    public KayttoOikeusController(KayttoOikeusService kayttoOikeusService, TaskExecutorService taskExecutorService) {
        this.kayttoOikeusService = kayttoOikeusService;
        this.taskExecutorService = taskExecutorService;
    }

    @PreAuthorize("hasAnyRole('ROLE_APP_HENKILONHALLINTA_READ',"
            + "'ROLE_APP_KAYTTOOIKEUS_READ',"
            + "'ROLE_APP_KAYTTOOIKEUS_CRUD',"
            + "'ROLE_APP_HENKILONHALLINTA_READ_UPDATE',"
            + "'ROLE_APP_HENKILONHALLINTA_CRUD',"
            + "'ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    @ApiOperation(value = "Hakee palveluun liittyvät käyttöoikeudet.",
            notes = "Listaa kaikki palveluun liitetyt käyttöoikeudet "
                    + "palvelu-käyttöoikeus DTO:n avulla, johon on asetettu "
                    + "roolinimi ja sen lokalisoidut tekstit.")
    @RequestMapping(value = "/{name}", method = RequestMethod.GET)
    public List<PalveluKayttoOikeusDto> listKayttoOikeusByPalvelu(
            @ApiParam("Palvelun tunnistenimi") @PathVariable("name") String name) {
        return kayttoOikeusService.listKayttoOikeusByPalvelu(name);
    }

    @PreAuthorize("hasAnyRole('ROLE_APP_HENKILONHALLINTA_READ',"
            + "'ROLE_APP_KAYTTOOIKEUS_READ',"
            + "'ROLE_APP_KAYTTOOIKEUS_CRUD',"
            + "'ROLE_APP_HENKILONHALLINTA_READ_UPDATE',"
            + "'ROLE_APP_HENKILONHALLINTA_CRUD',"
            + "'ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    @ApiOperation(value = "Hakee kirjautuneen käyttäjän käyttöoikeudet.",
            notes = "Listaa kaikki nykyisen sisäänkirjautuneen käyttäjän käyttöoikeudet, "
                    + "jossa on mukana myös vanhentuneet käyttöoikeudet.")
    @RequestMapping(value = "/kayttaja/current", method = RequestMethod.GET)
    public List<KayttoOikeusHistoriaDto> listKayttoOikeusCurrentUser() {
        return this.kayttoOikeusService.listMyonnettyKayttoOikeusHistoriaForCurrentUser();
    }

    @PreAuthorize("hasAnyRole('ROLE_APP_HENKILONHALLINTA_READ',"
            + "'ROLE_APP_KAYTTOOIKEUS_READ',"
            + "'ROLE_APP_KAYTTOOIKEUS_CRUD',"
            + "'ROLE_APP_HENKILONHALLINTA_READ_UPDATE',"
            + "'ROLE_APP_HENKILONHALLINTA_CRUD',"
            + "'ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    @ApiOperation(value = "Hakee käyttäjien käyttöoikeudet annetuilla hakukriteereillä. Haku rajoitettu 1000 kerralla.",
            notes = "Vastauksessa ei tule välttämättä 1000 henkilöä koska tulosjoukkoon tehdään yhdistämisiä.")
    @RequestMapping(value = "/kayttaja", method = RequestMethod.GET)
    public List<KayttooikeusPerustiedotDto> listKayttoOikeusByOid(KayttooikeusCriteria criteria,
                                                                  @RequestParam(required = false) Long offset) {
        long limit = 1000L;
        return this.kayttoOikeusService.listMyonnettyKayttoOikeusForUser(criteria, limit, offset);
    }

    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_SCHEDULE',"
            + "'ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    @ApiOperation(value = "Lähettää muistutusviestit henkilöille joilla on käyttöoikeus päättymässä.",
            notes = "Tämä on alustavasti vain automaattisen sähköpostimuistutuksen testausta varten.",
            authorizations = @Authorization("ROLE_APP_HENKILONHALLINTA_OPHREKISTERI"),
            response = Integer.class)
    @RequestMapping(value = "/expirationReminders", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON)
    public String sendExpirationReminders(@ApiParam(value = "Vuosi", required = true) @RequestParam("year") int year,
                                       @ApiParam(value = "Kuukausi", required = true) @RequestParam("month") int month,
                                       @ApiParam(value = "Päivä", required = true) @RequestParam("day") int day) {
        Period expireThreshold = Period.between(LocalDate.now(), LocalDate.of(year, month, day));
        return String.format("%d", taskExecutorService.sendExpirationReminders(expireThreshold));
    }
}
