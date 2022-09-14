package it.polimi.ingsw.models.components.characters.effects;

import it.polimi.ingsw.models.exceptions.IllegalMoveException;

import java.util.logging.Logger;

/**
 * Abstract class to create an Effect.
 *
 * - effect() must be overridden and should define the behaviour
 * of the effect.
 *
 * By default, only GameState object is required. If you need to add
 * other mandatory arguments, you must override getRequiredArgs() by
 * returning a RequiredEffectArgs object.
 */
public abstract class Effect {

    private final Logger logger = Logger.getGlobal();

    private final RequiredEffectArgs requiredArgs;
    protected EffectArgs args;

    protected Effect() {
        requiredArgs = getRequiredArgs();
        if (requiredArgs == null)
            throw new IllegalArgumentException("Required Arguments cannot be null");
    }

    public RequiredEffectArgs getRequiredArgs() {
        return RequiredEffectArgs.noOptionalArgsRequired();
    }

    /**
     * Performs an effect. Effects can be performed only during expert matches.
     *
     * @param effectArgs contains the parameters necessary for performing the effect.
     * @throws IllegalMoveException     if the effect cannot be performed
     *                                  or an error occurs during the effect.
     * @throws IllegalArgumentException if some required parameters are missing.
     */
    public final void performEffect(EffectArgs effectArgs) {
        if (!areValidEffectArgs(effectArgs))
            throw new IllegalArgumentException("Some effect arguments are missing");

        this.args = effectArgs;
        effect();
    }

    /**
     * Should be overridden to define the behaviour of the effect.
     * Must use "args" attribute to get effect parameters.
     */
    protected abstract void effect();

    private boolean areValidEffectArgs(EffectArgs effectArgs) {
        if (effectArgs.getGameState() == null) {
            logger.warning("argument GameState is missing");
            return false;
        }
        if (!effectArgs.getGameState().isExpertMatch()) {
            logger.warning("cannot perform effect in non-expert matches");
            return false;
        }
        if (requiredArgs.isIslandRequired() && effectArgs.getIsland() == null) {
            logger.warning("argument Island is missing");
            return false;
        }
        if (requiredArgs.isStudentRequired() && effectArgs.getStudent() == null) {
            logger.warning("argument Student is missing");
            return false;
        }
        if (requiredArgs.isCharacterRequired() && effectArgs.getCharacter() == null) {
            logger.warning("argument character is missing");
            return false;
        }
        if (requiredArgs.isToExchangeFromRequired() && effectArgs.getSourceStudents() == null) {
            logger.warning("argument ToExchangeFrom is missing");
            return false;
        }
        if (requiredArgs.isToExchangeToRequired() && effectArgs.getDestStudents() == null) {
            logger.warning("argument ToExchangeTo is missing");
            return false;
        }
        return true;
    }

}
