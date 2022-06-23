/*
 * Copyright (c) 2022 - The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MekHQ.
 *
 * MekHQ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MekHQ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MekHQ. If not, see <http://www.gnu.org/licenses/>.
 */
package mekhq.campaign.personnel.death;

import megamek.common.enums.Gender;
import mekhq.campaign.CampaignOptions;
import mekhq.campaign.personnel.enums.TenYearAgeRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@ExtendWith(value = MockitoExtension.class)
public class AgeRangeRandomDeathTest {
    @Mock
    private CampaignOptions mockOptions;

    @BeforeEach
    public void beforeEach() {
        when(mockOptions.getEnabledRandomDeathAgeGroups()).thenReturn(new HashMap<>());
        when(mockOptions.isUseRandomClanPersonnelDeath()).thenReturn(false);
        when(mockOptions.isUseRandomPrisonerDeath()).thenReturn(false);
        when(mockOptions.isUseRandomDeathSuicideCause()).thenReturn(false);

        final Map<TenYearAgeRange, Double> ageRangeMap = new HashMap<>();
        for (final TenYearAgeRange range : TenYearAgeRange.values()) {
            ageRangeMap.put(range, 1d);
        }
        when(mockOptions.getAgeRangeRandomDeathMaleValues()).thenReturn(ageRangeMap);
        when(mockOptions.getAgeRangeRandomDeathFemaleValues()).thenReturn(ageRangeMap);
    }
/*
    @Test
    public void testRandomlyDies() {
        assertFalse(new DisabledRandomDeath(mockOptions, false).randomlyDies(0, Gender.MALE));
    }
 */
}
