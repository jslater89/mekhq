package mekhq.campaign.personnel.autoAwards;

import mekhq.MekHQ;
import mekhq.campaign.Campaign;
import mekhq.campaign.mission.AtBContract;
import mekhq.campaign.mission.Contract;
import mekhq.campaign.mission.Mission;
import mekhq.campaign.personnel.Award;
import mekhq.campaign.personnel.Person;
import mekhq.campaign.universe.Faction;
import mekhq.campaign.universe.Factions;
import org.apache.logging.log4j.LogManager;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

public class TheatreOfWarAwards {
    /**
     * This function loops through Theatre of War Awards, checking whether the person is eligible to receive each type of award
     * @param campaign the campaign to be processed
     * @param awards the awards to be processed (should only include awards where item == TheatreOfWar)
     * @param person the person to check award eligibility for
     */
    public TheatreOfWarAwards(Campaign campaign, Mission mission, List<Award> awards, Person person) {
        final ResourceBundle resource = ResourceBundle.getBundle("mekhq.resources.AutoAwards",
                MekHQ.getMHQOptions().getLocale());

        boolean isEligible;

        String employer = ((Contract) mission).getEmployer();

        int contractStartYear = ((Contract) mission).getStartDate().getYear();
        int currentYear = campaign.getGameYear();

        for (Award award : awards) {
            List<String> attackers = new ArrayList<>();
            List<String> defenders = new ArrayList<>();

            List<String> wartime = List.of(award.getSize()
                    .replaceAll("\\s","")
                    .split(","));

            if (wartime.size() != 2) {
                LogManager.getLogger().warn("Award {} from the {} set has invalid start/end date {}",
                        award.getName(), award.getSet(), award.getSize());
                continue;
            }

            List<String> belligerents = List.of(award.getRange().split(","));

            if (!belligerents.isEmpty()) {
                if (belligerents.size() > 1) {
                    for (String belligerent : belligerents) {
                        if (belligerent.replaceAll("[()]", "").contains("1")) {
                            attackers.add(belligerent.replaceAll("[^. A-Za-z]", ""));
                        } else if (belligerent.replaceAll("[()]", "").contains("2")) {
                            defenders.add(belligerent.replaceAll("[^. A-Za-z]", ""));
                        }
                    }

                    if ((attackers.isEmpty()) || (defenders.isEmpty())) {
                        LogManager.getLogger().warn("Award {} from the {} set has incorrectly formated belligerents {}",
                                award.getName(), award.getSet(), award.getRange());
                        continue;
                    }
                }
            } else {
                LogManager.getLogger().warn("Award {} from the {} set has no belligerents",
                        award.getName(), award.getSet());
                continue;
            }

            if (award.canBeAwarded(person)) {
                if (isDuringWartime(wartime, contractStartYear, currentYear)) {
                    isEligible = true;
                } else {
                    continue;
                }

                if (belligerents.size() == 1) {
                    if(!processFaction(belligerents.get(0), employer)) {
                        continue;
                    }
                } else if ((campaign.getCampaignOptions().isUseAtB()) && (mission instanceof AtBContract)) {
                    String enemy = ((AtBContract) mission).getEnemyName(campaign.getGameYear());

                    if (hasLoyalty(employer, attackers)) {
                        isEligible = hasLoyalty(enemy, defenders);
                    } else if (hasLoyalty(employer, defenders)) {
                        isEligible = hasLoyalty(enemy, attackers);
                    } else {
                        continue;
                    }
                }

                if (isEligible) {
                    // we have to include ' ' as hyperlinked names lose their hyperlink if used within resource.getString()
                    campaign.addReport(person.getHyperlinkedName() + ' ' +
                            MessageFormat.format(resource.getString("EligibleForAwardReport.format"),
                                    award.getName(), award.getSet()));
                }
            }
        }
    }

    /**
     * Streams through years covered by Contract, returns true if at least one is during wartime
     * @param wartime a list with two entries, war start year and war end year (can be identical)
     * @param contractStartYear the contract's start yet
     * @param currentYear the current campaign year
     */
    private boolean isDuringWartime (List<String> wartime, int contractStartYear, int currentYear) {
        int contractLength = currentYear - contractStartYear;

        return IntStream.rangeClosed(0, contractLength).map(year -> contractStartYear + year)
                .anyMatch(checkYear -> (checkYear >= Integer.parseInt(wartime.get(0)))
                        && (checkYear <= Integer.parseInt(wartime.get(1))));
    }

    /**
     * Streams through contents of factions and returns true if any match missionFaction
     * @param missionFaction a single faction (either employer or enemy)
     * @param factions a list of factions (either a list of attackers, or of defenders)
     */
    private boolean hasLoyalty(String missionFaction, List<String> factions) {
        return factions.stream().anyMatch(faction -> processFaction(missionFaction, faction));
    }

    /**
     * Checks whether missionFaction matches the requirements of belligerent
     * @param missionFaction a single faction (either employer or enemy)
     * @param belligerent the faction, or super-faction, to be matched against
     */
    private boolean processFaction(String missionFaction, String belligerent) {
        Faction faction = Factions.getInstance().getFaction(missionFaction);

        missionFaction = missionFaction.toLowerCase().replaceAll("\\s","");
        belligerent = belligerent.toLowerCase().replaceAll("\\s","");

        switch (belligerent) {
            case "majorpowers":
                return faction.isMajorOrSuperPower();
            case "innersphere":
                return faction.isInnerSphere();
            case "clans":
                return faction.isClan();
            case "periphery":
                return faction.isPeriphery();
            case "independent":
                return faction.isIndependent();
            case "deepperiphery":
                return faction.isDeepPeriphery();
            case "comstar":
                return faction.isComStar();
            case "wob":
                return faction.isWoB();
            case "comstarorwob":
                return faction.isComStarOrWoB();
            default:
                return missionFaction.equals(belligerent);
        }
    }
}
