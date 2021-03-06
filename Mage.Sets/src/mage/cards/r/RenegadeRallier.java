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
package mage.cards.r;

import java.util.UUID;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.condition.common.RevoltCondition;
import mage.abilities.decorator.ConditionalOneShotEffect;
import mage.abilities.effects.common.ReturnFromGraveyardToBattlefieldTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.filter.Filter;
import mage.filter.common.FilterPermanentCard;
import mage.filter.predicate.mageobject.ConvertedManaCostPredicate;
import mage.target.common.TargetCardInYourGraveyard;
import mage.watchers.common.RevoltWatcher;

/**
 *
 * @author fireshoes
 */
public class RenegadeRallier extends CardImpl {

    private static final FilterPermanentCard filter = new FilterPermanentCard("permanent card with converted mana cost 2 or less from your graveyard");

    static {
        filter.add(new ConvertedManaCostPredicate(Filter.ComparisonType.LessThan, 3));
    }

    public RenegadeRallier(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{G}{W}");

        this.subtype.add("Human");
        this.subtype.add("Warrior");
        this.power = new MageInt(3);
        this.toughness = new MageInt(2);

        // <i>Revolt</i> &mdash; When Renegade Rallier enters the battlefield, if a permanent you controlled left the battlefield this turn,
        // return target permanent card with converted mana cost 2 or less from your graveyard to your battlefield.
        Ability ability = new EntersBattlefieldTriggeredAbility(
                new ConditionalOneShotEffect(new ReturnFromGraveyardToBattlefieldTargetEffect(), RevoltCondition.getInstance()),
                false, "<i>Revolt</i> &mdash; ");
        ability.addTarget(new TargetCardInYourGraveyard(filter));
        ability.addWatcher(new RevoltWatcher());
        this.addAbility(ability);
    }

    public RenegadeRallier(final RenegadeRallier card) {
        super(card);
    }

    @Override
    public RenegadeRallier copy() {
        return new RenegadeRallier(this);
    }
}
