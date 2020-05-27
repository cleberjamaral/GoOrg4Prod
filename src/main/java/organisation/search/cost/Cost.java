package organisation.search.cost;

/**
 * Cost refers to different cost functions that can be used in order to find a more suitable
 * organisational structure. The chosen structure may be flatter or taller, the organisational
 * positions can be more specialist or more generalist and so on.
 * 
 * Currently, the available cost functions are:
 * 0 UNITARY      : unitary cost (no function)
 * 1 TALLER       : Taller (more levels) hierarchies are preferable 
 * 2 FLATTER      : Flatter (fewer levels) hierarchies are preferable 
 * 3 SPECIALIST   : More specialist positions are preferable
 * 4 GENERALIST   : More generalist positions are preferable
 * 5 EFFICIENT    : Fewer idleness is preferable
 * 6 IDLE         : More idleness is preferable
 *  
 * @author cleber
 */
public enum Cost {
	UNITARY, TALLER, FLATTER, SPECIALIST, GENERALIST, EFFICIENT, IDLE;
}