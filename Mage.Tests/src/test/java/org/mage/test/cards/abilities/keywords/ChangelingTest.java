/*
 *  Copyright 2010 BetaSteward_at_googlemail.com. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY BetaSteward_at_googlemail.com ``AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL BetaSteward_at_googlemail.com OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and should not be interpreted as representing official policies, either expressed
 *  or implied, of BetaSteward_at_googlemail.com.
 */
package org.mage.test.cards.abilities.keywords;

import mage.abilities.Ability;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.game.permanent.Permanent;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author LevelX2
 */
public class ChangelingTest extends CardTestPlayerBase {

    /**
     * Casting changelings with a Long-Forgotten Gohei in play reduces its
     * casting cost by {1}.
     */
    @Test
    public void testLongForgottenGohei() {
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 1);
        addCard(Zone.HAND, playerA, "Woodland Changeling");

        addCard(Zone.BATTLEFIELD, playerA, "Long-Forgotten Gohei");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Woodland Changeling");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, "Woodland Changeling", 0); // Casting cost of spell is not reduced so not on the battlefield
        assertHandCount(playerA, "Woodland Changeling", 1);

    }

    /**
     * Another bug, was playing Slivers again. I had a Amoeboid Changeling, a
     * Hibernation Sliver and a Prophet of Kruphix. In response to a boardwipe,
     * I tapped my Changeling, giving my Prophet Changeling. However, it didn't
     * gain any Sliver abilities despite having all creature types, including
     * Sliver, so I couldn't save it with my Hibernation Sliver. I clicked the
     * Prophet and nothing happened at all.
     */
    @Test
    public void testGainingChangeling() {
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 3);
        // Untap all creatures and lands you control during each other player's untap step.
        // You may cast creature cards as though they had flash.
        addCard(Zone.HAND, playerA, "Prophet of Kruphix");// {3}{G}{U}
        // Changeling
        // {T}: Target creature gains all creature types until end of turn.
        // {T}: Target creature loses all creature types until end of turn.
        addCard(Zone.BATTLEFIELD, playerA, "Amoeboid Changeling");
        // All Slivers have "Pay 2 life: Return this permanent to its owner's hand."
        addCard(Zone.BATTLEFIELD, playerA, "Hibernation Sliver");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Prophet of Kruphix");
        activateAbility(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "{T}: Target creature gains", "Prophet of Kruphix");

        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertTapped("Amoeboid Changeling", true);

        Permanent prophet = getPermanent("Prophet of Kruphix", playerA);
        boolean abilityFound = false;
        for (Ability ability : prophet.getAbilities()) {
            if (ability.getRule().startsWith("Pay 2 life")) {
                abilityFound = true;
            }
        }
        Assert.assertTrue("Prophet of Kruphix has to have the 'Pay 2 life: Return this permanent to its owner's hand.' ability, but has not.", abilityFound);

    }
}
