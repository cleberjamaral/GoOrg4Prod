package organisation;

/**
 * 0 UNITARY   : unitary cost (no function)
 * 1 TALLER    : Taller hierarchies are preferrable 
 * 2 FLATTER   : Flatter hierarchies are preferrable 
 * 3 SPECIALIST: More specilist roles are preferrable
 * 4 GENERALIST: More generalist roles are preferrable 
 */
public enum Cost {
	UNITARY, TALLER, FLATTER, SPECIALIST, GENERALIST;
}