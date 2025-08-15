/*
 * (c) 2025 Stéphane Cachat stephane@cachat.com. No reuse or distribution allowed. Réutilisation ou redistribution interdite.
 */
package nextapp.echo.app;

/**
 * les valeurs pour le tag css position
 *
 * @author scachat
 */
public enum Position {

    /**
     * The Positionable is a normal Positionable, laid out according to the
     * normal flow.
     */
    STATIC(1),
    /**
     * The Positionable's position (and possibly size) is specified with the
     * 'top', 'right', 'bottom', and 'left' properties. These properties specify
     * offsets with respect to the Positionable's containing Positionable.
     * Absolutely positioned Positionables are taken out of the normal flow.
     * This means they have no impact on the layout of later siblings.
     */
    ABSOLUTE(2),
    /**
     * The Positionable's position is calculated according to the normal flow.
     * Then the Positionable is offset relative to its normal position.
     */
    RELATIVE(4),
    /**
     * The Positionable's position is calculated according to the 'absolute'
     * model, but in addition, the Positionable is fixed with respect to the
     * viewport and doesn't move when scrolled.
     */
    FIXED(8);
    private int serialValue;

    private Position(int serialValue) {
        this.serialValue = serialValue;
    }

    public int getSerialValue() {
        return serialValue;
    }

}
