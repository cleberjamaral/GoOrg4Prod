package organisation.search;

/**
 * Cost refers to different cost functions that can be used in order to find a more suitable
 * organisational structure. The chosen structure may be flatter or taller, the organisational
 * roles can be more specialist or more generalist and so on.
 * 
 * Currently, the available cost functions are:
 * 0 UNITARY   : unitary cost (no function)
 * 1 TALLER    : Taller hierarchies are preferable 
 * 2 FLATTER   : Flatter hierarchies are preferable 
 * 3 SPECIALIST: More specialist roles are preferable
 * 4 GENERALIST: More generalist roles are preferable 
 */
public enum Cost {
	UNITARY, TALLER, FLATTER, SPECIALIST, GENERALIST;
}